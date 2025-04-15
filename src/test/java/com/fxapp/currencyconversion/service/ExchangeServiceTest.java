package com.fxapp.currencyconversion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
import com.fxapp.currencyconversion.service.impl.ExchangeServiceImpl;
import com.fxapp.currencyconversion.util.FileParser;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceTest {

  @Mock private ExchangeRateCacheService exchangeRateCacheService;

  @Mock private ConversionHistoryRepository conversionHistoryRepository;

  @Mock private CacheManager cacheManager;

  @Mock private UserRepository userRepository;

  @InjectMocks @Spy private ExchangeServiceImpl exchangeService;

  Conversion mockConversion;

  @BeforeEach
  void setup() {
    User user = User.builder().id(UUID.randomUUID()).username("admin").build();

    mockConversion =
        Conversion.builder()
            .transactionId(UUID.randomUUID())
            .sourceCurrency("USD")
            .targetCurrency("TRY")
            .amount(100)
            .rate(30)
            .convertedAmount(3000)
            .timestamp(LocalDateTime.now())
            .user(user)
            .build();
  }

  @Test
  void testCheckRate_shouldReturnCorrectRate() {
    ExchangeRateRequestDTO request = new ExchangeRateRequestDTO("USD", "TRY");

    when(exchangeRateCacheService.getRate("USD", "TRY")).thenReturn(38.095);

    ExchangeRateResponseDTO result = exchangeService.checkRate(request);

    assertEquals("USD", result.getSourceCurrency());
    assertEquals("TRY", result.getTargetCurrency());
    assertEquals(38.095, result.getRate());
  }

  @Test
  void testCheckRate_nullCurrencyCode() {
    ExchangeRateRequestDTO request = new ExchangeRateRequestDTO("USD", "");
    FxException ex = assertThrows(FxException.class, () -> exchangeService.checkRate(request));

    assertEquals(ResultCode.NULL_CURRENCY_CODE, ex.getResultCode());
  }

  @Test
  void testCurrencyChange_shouldThrow_whenAmountIsZero() {
    CurrencyChangeRequestDTO request = new CurrencyChangeRequestDTO("USD", "TRY", 0.0, "admin");

    FxException ex = assertThrows(FxException.class, () -> exchangeService.currencyChange(request));

    assertEquals(ResultCode.AMOUNT_MUST_BE_BIGGER_THAN_ZERO, ex.getResultCode());
  }

  @Test
  void testCurrencyChange_shouldThrow_whenCurrencyCodeIsNull() {
    CurrencyChangeRequestDTO request = new CurrencyChangeRequestDTO(null, "TRY", 10.0, "admin");

    FxException ex = assertThrows(FxException.class, () -> exchangeService.currencyChange(request));

    assertEquals(ResultCode.NULL_CURRENCY_CODE, ex.getResultCode());
  }

  @Test
  void testCurrencyChange_success() {
    CurrencyChangeRequestDTO request = new CurrencyChangeRequestDTO("USD", "TRY", 100.0, "admin");
    double mockRate = 30.0;
    double expectedConverted = 3000.0;

    Conversion savedConversion =
        Conversion.builder()
            .transactionId(UUID.randomUUID())
            .sourceCurrency("USD")
            .targetCurrency("TRY")
            .amount(100.0)
            .convertedAmount(expectedConverted)
            .rate(mockRate)
            .timestamp(LocalDateTime.now())
            .build();

    User user = User.builder().id(UUID.randomUUID()).username("admin").build();
    Optional<User> mockUser = Optional.of(user);

    when(exchangeRateCacheService.getRate("USD", "TRY")).thenReturn(mockRate);
    when(conversionHistoryRepository.saveAndFlush(any(Conversion.class)))
        .thenReturn(savedConversion);
    when(userRepository.findByUsername(anyString())).thenReturn(mockUser);

    CurrencyChangeResponseDTO response = exchangeService.currencyChange(request);

    assertNotNull(response.getTransactionId());
    assertEquals(expectedConverted, response.getConvertedAmount());
  }

  @Test
  void testGetConversionHistory_shouldThrow_whenNoFilterProvided() {
    ConversionHistoryRequestDTO request = new ConversionHistoryRequestDTO();
    Pageable pageable = PageRequest.of(0, 10);

    FxException exception =
        assertThrows(
            FxException.class, () -> exchangeService.getConversionHistory(request, pageable));

    assertEquals(ResultCode.AT_LEAST_ONE_FILTER_REQUIRED, exception.getResultCode());
  }

  @Test
  void testGetConversionHistory_byTransactionIdOnly() {
    UUID id = UUID.randomUUID();
    ConversionHistoryRequestDTO request = new ConversionHistoryRequestDTO();
    request.setTransactionId(id);
    request.setUsername("admin");

    Pageable pageable = PageRequest.of(0, 10);

    Page<Conversion> mockPage = new PageImpl<>(List.of(mockConversion));

    when(conversionHistoryRepository.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(mockPage);

    PageResponse<ConversionHistoryResponseDTO> result =
        exchangeService.getConversionHistory(request, pageable);

    assertEquals(1, result.getContent().size());
    verify(conversionHistoryRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  void testGetConversionHistory_byTransactionDateOnly() {
    LocalDateTime date = LocalDateTime.now().minusDays(1);
    ConversionHistoryRequestDTO request = new ConversionHistoryRequestDTO();
    request.setTransactionDate(date);

    Pageable pageable = PageRequest.of(0, 10);

    Page<Conversion> mockPage = new PageImpl<>(List.of(mockConversion));
    when(conversionHistoryRepository.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(mockPage);

    PageResponse<ConversionHistoryResponseDTO> result =
        exchangeService.getConversionHistory(request, pageable);

    assertEquals(1, result.getContent().size());
    verify(conversionHistoryRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  void testGetConversionHistory_byBothFilters() {
    UUID id = UUID.randomUUID();
    LocalDateTime date = LocalDateTime.now().minusDays(1);
    ConversionHistoryRequestDTO request = new ConversionHistoryRequestDTO();
    request.setTransactionId(id);
    request.setTransactionDate(date);

    Pageable pageable = PageRequest.of(0, 10);

    Page<Conversion> mockPage = new PageImpl<>(List.of(mockConversion));
    when(conversionHistoryRepository.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(mockPage);

    PageResponse<ConversionHistoryResponseDTO> result =
        exchangeService.getConversionHistory(request, pageable);

    assertEquals(1, result.getContent().size());
    verify(conversionHistoryRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  void testUploadFile_shouldThrow_whenUnsupportedFileType() {
    MultipartFile mockFile = mock(MultipartFile.class);
    when(mockFile.getOriginalFilename()).thenReturn("rates.txt");

    FxException ex =
        assertThrows(
            FxException.class,
            () -> {
              exchangeService.uploadFile(mockFile);
            });

    assertEquals(ResultCode.FILE_FORMAT_NOT_SUPPORTED, ex.getResultCode());
  }

  @Test
  void testUploadFile_shouldThrow_whenParsingFails() throws IOException {
    MultipartFile mockFile = mock(MultipartFile.class);
    when(mockFile.getOriginalFilename()).thenReturn("rates.csv");

    try (MockedStatic<FileParser> mockedParser = mockStatic(FileParser.class)) {
      mockedParser
          .when(() -> FileParser.parseBulkCurrencyChangeRequestCsv(mockFile))
          .thenThrow(new IOException("parse failed"));

      FxException ex =
          assertThrows(
              FxException.class,
              () -> {
                exchangeService.uploadFile(mockFile);
              });

      assertEquals(ResultCode.FILE_PARSING_FAILED, ex.getResultCode());
    }
  }

  @Test
  void testUploadFile_shouldCallCurrencyChange_forEachRequest() throws IOException {
    MultipartFile mockFile = mock(MultipartFile.class);
    when(mockFile.getOriginalFilename()).thenReturn("bulk.csv");

    List<CurrencyChangeRequestDTO> mockRequestList =
        List.of(
            new CurrencyChangeRequestDTO("USD", "TRY", 100.0, "admin"),
            new CurrencyChangeRequestDTO("EUR", "TRY", 50.0, "admin"));

    try (MockedStatic<FileParser> mockedParser = mockStatic(FileParser.class)) {
      mockedParser
          .when(() -> FileParser.parseBulkCurrencyChangeRequestCsv(mockFile))
          .thenReturn(mockRequestList);

      doReturn(new CurrencyChangeResponseDTO(UUID.randomUUID(), 3000.0))
          .when(exchangeService)
          .currencyChange(any());

      List<CurrencyChangeResponseDTO> result = exchangeService.uploadFile(mockFile);

      assertEquals(2, result.size());
      verify(exchangeService, times(2)).currencyChange(any());
    }
  }
}
