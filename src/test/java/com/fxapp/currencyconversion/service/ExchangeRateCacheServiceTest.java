package com.fxapp.currencyconversion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.fxapp.currencyconversion.service.client.ExchangeRateClient;
import com.fxapp.currencyconversion.service.impl.ExchangeRateCacheServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@EnableCaching
@Import(ExchangeRateCacheServiceTest.TestConfig.class)
class ExchangeRateCacheServiceTest {

  @Autowired private ExchangeRateCacheService cacheService;

  @Autowired private ExchangeRateClient exchangeRateClient;

  @Test
  void testCaching_worksCorrectly() {
    String source = "USD";
    String target = "TRY";

    when(exchangeRateClient.getRate(source, target)).thenReturn(38.0).thenReturn(999.0);

    double firstCall = cacheService.getRate(source, target);
    double secondCall = cacheService.getRate(source, target);

    assertEquals(38.0, firstCall);
    assertEquals(38.0, secondCall);

    verify(exchangeRateClient, times(1)).getRate(source, target);
  }

  @TestConfiguration
  static class TestConfig {

    @Bean
    @Primary
    public ExchangeRateClient exchangeRateClient() {
      return mock(ExchangeRateClient.class);
    }

    @Bean
    @Primary
    public ExchangeRateCacheService cacheService(ExchangeRateClient exchangeRateClient) {
      return new ExchangeRateCacheServiceImpl(exchangeRateClient);
    }
  }
}
