package com.fxapp.currencyconversion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.fxapp.currencyconversion.service.client.ExchangeRateClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest
@EnableCaching
class ExchangeRateCacheServiceTest {

  @MockitoBean private ExchangeRateClient exchangeRateClient;

  @Autowired private ExchangeRateCacheService cacheService;

  @Test
  void testCaching_worksCorrectly() {
    String source = "USD";
    String target = "TRY";

    when(exchangeRateClient.getRate(source, target)).thenReturn(38.0).thenReturn(999.0);

    double first = cacheService.getRate(source, target);
    double second = cacheService.getRate(source, target);

    assertEquals(38.0, first);
    assertEquals(38.0, second);

    verify(exchangeRateClient, times(1)).getRate(source, target);
  }
}
