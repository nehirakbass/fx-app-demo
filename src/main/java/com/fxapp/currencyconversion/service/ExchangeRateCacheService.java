package com.fxapp.currencyconversion.service;

public interface ExchangeRateCacheService {
  public double getRate(String sourceCurrency, String targetCurrency);
}
