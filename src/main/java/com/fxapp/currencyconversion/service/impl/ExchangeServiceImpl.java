package com.fxapp.currencyconversion.service.impl;

import com.fxapp.currencyconversion.constants.ResultCode;
import com.fxapp.currencyconversion.dtos.PageResponse;
import com.fxapp.currencyconversion.dtos.conversionhistory.ConversionHistoryRequestDTO;
import com.fxapp.currencyconversion.dtos.conversionhistory.ConversionHistoryResponseDTO;
import com.fxapp.currencyconversion.dtos.currencychange.CurrencyChangeRequestDTO;
import com.fxapp.currencyconversion.dtos.currencychange.CurrencyChangeResponseDTO;
import com.fxapp.currencyconversion.dtos.exchangerate.ExchangeRateRequestDTO;
import com.fxapp.currencyconversion.dtos.exchangerate.ExchangeRateResponseDTO;
import com.fxapp.currencyconversion.entities.Conversion;
import com.fxapp.currencyconversion.entities.User;
import com.fxapp.currencyconversion.exception.FxException;
import com.fxapp.currencyconversion.repos.ConversionHistoryRepository;
import com.fxapp.currencyconversion.repos.UserRepository;
import com.fxapp.currencyconversion.service.ExchangeRateCacheService;
import com.fxapp.currencyconversion.service.ExchangeService;
import com.fxapp.currencyconversion.service.mapper.ConversionMapper;
import com.fxapp.currencyconversion.util.ConversionSpecificationCreator;
import com.fxapp.currencyconversion.util.FileParser;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class ExchangeServiceImpl implements ExchangeService {
  private final ExchangeRateCacheService exchangeRateCacheService;
  private final ConversionHistoryRepository conversionHistoryRepository;
  private final CacheManager cacheManager;
  private final UserRepository userRepository;

  public ExchangeServiceImpl(
      ExchangeRateCacheService exchangeRateCacheService,
      ConversionHistoryRepository conversionHistoryRepository,
      CacheManager cacheManager,
      UserRepository userRepository) {
    this.exchangeRateCacheService = exchangeRateCacheService;
    this.conversionHistoryRepository = conversionHistoryRepository;
    this.cacheManager = cacheManager;
    this.userRepository = userRepository;
  }

  @Override
  public ExchangeRateResponseDTO checkRate(ExchangeRateRequestDTO request) {
    checkCurrencyCodes(request.getSourceCurrency(), request.getTargetCurrency());

    return ExchangeRateResponseDTO.builder()
        .sourceCurrency(request.getSourceCurrency())
        .targetCurrency(request.getTargetCurrency())
        .rate(getRate(request.getSourceCurrency(), request.getTargetCurrency()))
        .build();
  }

  @Override
  @Transactional
  public CurrencyChangeResponseDTO currencyChange(final CurrencyChangeRequestDTO request) {
    checkCurrencyCodes(request.getSourceCurrency(), request.getTargetCurrency());

    if (request.getAmount() <= 0) {
      throw new FxException(ResultCode.AMOUNT_MUST_BE_BIGGER_THAN_ZERO);
    }

    User user = checkUser(request.getUsername());

    double rate = getRate(request.getSourceCurrency(), request.getTargetCurrency());
    double convertedAmount = request.getAmount() * rate;
    Conversion conversion =
        Conversion.builder()
            .sourceCurrency(request.getSourceCurrency())
            .targetCurrency(request.getTargetCurrency())
            .amount(request.getAmount())
            .convertedAmount(convertedAmount)
            .rate(rate)
            .timestamp(LocalDateTime.now())
            .user(user)
            .build();

    conversion = conversionHistoryRepository.saveAndFlush(conversion);

    return CurrencyChangeResponseDTO.builder()
        .convertedAmount(convertedAmount)
        .transactionId(conversion.getTransactionId())
        .build();
  }

  @Override
  public PageResponse<ConversionHistoryResponseDTO> getConversionHistory(
      final ConversionHistoryRequestDTO request, final Pageable pageable) {
    if (request.getTransactionId() == null
        && request.getTransactionDate() == null
        && request.getUsername() == null) {
      throw new FxException(ResultCode.AT_LEAST_ONE_FILTER_REQUIRED);
    }

    Specification<Conversion> spec = ConversionSpecificationCreator.create(request);

    Page<Conversion> pagedConversionHistoryList =
        conversionHistoryRepository.findAll(spec, pageable);

    List<ConversionHistoryResponseDTO> list =
        Optional.ofNullable(pagedConversionHistoryList.getContent())
            .orElse(Collections.emptyList())
            .stream()
            .map(ConversionMapper::mapConversionHistoryToDTO)
            .toList();

    return PageResponse.<ConversionHistoryResponseDTO>builder()
        .content(list)
        .totalElements(pagedConversionHistoryList.getTotalElements())
        .totalPages(pagedConversionHistoryList.getTotalPages())
        .currentPage(pagedConversionHistoryList.getNumber())
        .pageSize(pagedConversionHistoryList.getSize())
        .build();
  }

  @Override
  public List<CurrencyChangeResponseDTO> uploadFile(final MultipartFile file) {
    try {
      List<CurrencyChangeRequestDTO> requestDTOList;
      String filename =
          Optional.ofNullable(file.getOriginalFilename())
              .orElseThrow(() -> new FxException(ResultCode.FILE_FORMAT_NOT_SUPPORTED))
              .toLowerCase();
      if (filename.endsWith(".xlsx")) {
        requestDTOList = FileParser.parseBulkCurrencyChangeRequestXlsx(file);
      } else if (filename.endsWith(".csv")) {
        requestDTOList = FileParser.parseBulkCurrencyChangeRequestCsv(file);
      } else {
        throw new FxException(ResultCode.FILE_FORMAT_NOT_SUPPORTED);
      }
      log.info("Received bulk conversion request {} ", requestDTOList.size());
      return requestDTOList.stream().map(this::currencyChange).toList();
    } catch (IOException e) {
      log.error("Failed to parse file: {}", e.getMessage());
      throw new FxException(ResultCode.FILE_PARSING_FAILED);
    }
  }

  private static void checkCurrencyCodes(final String sourceCurrency, final String targetCurrency) {
    if (StringUtils.isEmpty(sourceCurrency) || StringUtils.isEmpty(targetCurrency)) {
      throw new FxException(ResultCode.NULL_CURRENCY_CODE);
    }
  }

  public double getRate(final String sourceCurrency, final String targetCurrency) {
    String cacheKey = sourceCurrency + "_" + targetCurrency;
    Cache cache = cacheManager.getCache("exchangeRates");
    Double cachedRate = cache != null ? cache.get(cacheKey, Double.class) : null;

    if (cachedRate != null) {
      log.info(
          "Found in cache – Returning cached rate for: {} -> {} = {}",
          sourceCurrency,
          targetCurrency,
          cachedRate);

      return BigDecimal.valueOf(cachedRate).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    return exchangeRateCacheService.getRate(sourceCurrency, targetCurrency);
  }

  private User checkUser(String username) {
    isUsernameEmpty(username);

    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new FxException(ResultCode.USER_NOT_FOUND));
  }

  private void isUsernameEmpty(String username) {
    if (StringUtils.isEmpty(username)) {
      throw new FxException(ResultCode.NULL_USER_NAME);
    }
  }
}
