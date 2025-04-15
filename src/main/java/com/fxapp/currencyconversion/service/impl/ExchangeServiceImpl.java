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
import com.fxapp.currencyconversion.exception.FxException;
import com.fxapp.currencyconversion.repos.ConversionHistoryRepository;
import com.fxapp.currencyconversion.service.ExchangeRateCacheService;
import com.fxapp.currencyconversion.service.ExchangeService;
import com.fxapp.currencyconversion.service.mapper.ConversionMapper;
import com.fxapp.currencyconversion.util.FileParser;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class ExchangeServiceImpl implements ExchangeService {
  private final ExchangeRateCacheService exchangeRateCacheService;
  private final ConversionHistoryRepository conversionHistoryRepository;
  private final CacheManager cacheManager;

  public ExchangeServiceImpl(
      ExchangeRateCacheService exchangeRateCacheService,
      ConversionHistoryRepository conversionHistoryRepository,
      CacheManager cacheManager) {
    this.exchangeRateCacheService = exchangeRateCacheService;
    this.conversionHistoryRepository = conversionHistoryRepository;
    this.cacheManager = cacheManager;
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
  public CurrencyChangeResponseDTO currencyChange(CurrencyChangeRequestDTO request) {
    checkCurrencyCodes(request.getSourceCurrency(), request.getTargetCurrency());

    if (request.getAmount() <= 0) {
      throw new FxException(ResultCode.AMOUNT_MUST_BE_BIGGER_THAN_ZERO);
    }

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
            .build();

    conversion = conversionHistoryRepository.saveAndFlush(conversion);

    return CurrencyChangeResponseDTO.builder()
        .convertedAmount(convertedAmount)
        .transactionId(conversion.getTransactionId())
        .build();
  }

  @Override
  public PageResponse<ConversionHistoryResponseDTO> getConversionHistory(
      ConversionHistoryRequestDTO request, Pageable pageable) {
    if (request.getTransactionId() == null && request.getTransactionDate() == null) {
      throw new FxException(ResultCode.AT_LEAST_ONE_FILTER_REQUIRED);
    }

    Page<Conversion> pagedConversionHistoryList;

    if (request.getTransactionId() != null && request.getTransactionDate() != null) {
      pagedConversionHistoryList =
          conversionHistoryRepository.findByTransactionIdAndTimestampBetween(
              request.getTransactionId(),
              request.getTransactionDate(),
              LocalDateTime.now(),
              pageable);
    } else if (request.getTransactionId() != null) {
      pagedConversionHistoryList =
          conversionHistoryRepository.findByTransactionId(request.getTransactionId(), pageable);
    } else {
      pagedConversionHistoryList =
          conversionHistoryRepository.findByTimestampBetween(
              request.getTransactionDate(), LocalDateTime.now(), pageable);
    }
    List<ConversionHistoryResponseDTO> list =
        pagedConversionHistoryList.getContent().stream()
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
  public List<CurrencyChangeResponseDTO> uploadFile(MultipartFile file) {
    try {
      List<CurrencyChangeRequestDTO> requestDTOList;
      String filename = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
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

  private static void checkCurrencyCodes(String sourceCurrency, String targetCurrency) {
    if (StringUtils.isEmpty(sourceCurrency) || StringUtils.isEmpty(targetCurrency)) {
      throw new FxException(ResultCode.NULL_CURRENCY_CODE);
    }
  }

  public double getRate(String sourceCurrency, String targetCurrency) {
    String cacheKey = sourceCurrency + "_" + targetCurrency;
    Cache cache = cacheManager.getCache("exchangeRates");
    Double cachedRate = cache != null ? cache.get(cacheKey, Double.class) : null;

    if (cachedRate != null) {
      log.info(
          "Found in cache – Returning cached rate for: {} -> {} = {}",
          sourceCurrency,
          targetCurrency,
          cachedRate);
      return cachedRate;
    }

    return exchangeRateCacheService.getRate(sourceCurrency, targetCurrency);
  }
}
