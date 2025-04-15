package com.fxapp.currencyconversion.service.client;

public interface ExchangeRateClient {
  double getRate(String srcCurr, String targetCurr);
}
