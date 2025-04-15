package com.fxapp.currencyconversion.service.impl;

import com.fxapp.currencyconversion.service.ExchangeRateCacheService;
import com.fxapp.currencyconversion.service.client.ExchangeRateClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExchangeRateCacheServiceImpl implements ExchangeRateCacheService {

  private final ExchangeRateClient exchangeRateClient;

  public ExchangeRateCacheServiceImpl(ExchangeRateClient exchangeRateClient) {
    this.exchangeRateClient = exchangeRateClient;
  }

  @Override
  @Cacheable(value = "exchangeRates", key = "#sourceCurrency + '_' + #targetCurrency")
  public double getRate(String sourceCurrency, String targetCurrency) {
    log.info(
        "Not found in cache – Fetching rate from API: {} -> {}", sourceCurrency, targetCurrency);
    return exchangeRateClient.getRate(sourceCurrency, targetCurrency);
  }
}
