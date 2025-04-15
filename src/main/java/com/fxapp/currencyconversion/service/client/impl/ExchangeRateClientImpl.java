package com.fxapp.currencyconversion.service.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fxapp.currencyconversion.constants.ResultCode;
import com.fxapp.currencyconversion.exception.FxException;
import com.fxapp.currencyconversion.service.client.ExchangeRateClient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ExchangeRateClientImpl implements ExchangeRateClient {

  private final RestTemplate restTemplate;

  @Value("${fx.api-key}")
  private String apiKey;

  public ExchangeRateClientImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public double getRate(String srcCurr, String targetCurr) {
    if (srcCurr.equalsIgnoreCase(targetCurr)) {
      return 1.0;
    }

    String qryKey = srcCurr.toUpperCase() + targetCurr.toUpperCase();
    String url =
        String.format(
            "http://api.currencylayer.com/live?access_key=%s&"
                + "currencies=%s&"
                + "source=%s&"
                + "format=1",
            apiKey, targetCurr, srcCurr);
    JsonNode response = restTemplate.getForObject(url, JsonNode.class);

    if ((response == null || !response.has("success")) || !response.get("success").asBoolean()) {
      throw new FxException(ResultCode.EXTERNAL_API_ERROR);
    }

    JsonNode quote = response.get("quotes").get(qryKey);
    if (quote == null) {
      throw new FxException(ResultCode.INVALID_CURRENCY);
    }

    return BigDecimal.valueOf(quote.asDouble()).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }
}
