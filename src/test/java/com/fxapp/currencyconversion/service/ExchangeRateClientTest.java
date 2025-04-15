package com.fxapp.currencyconversion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fxapp.currencyconversion.TestRestTemplateConfig;
import com.fxapp.currencyconversion.service.client.impl.ExchangeRateClientImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles("test")
@SpringBootTest(classes = {ExchangeRateClientImpl.class, TestRestTemplateConfig.class})
class ExchangeRateClientTest {

  @Autowired private RestTemplate restTemplate;

  @Autowired private ExchangeRateClientImpl client;

  @Test
  void testGetRate_successfulResponse() {
    JsonNode mockResponse =
        new ObjectMapper()
            .createObjectNode()
            .put("success", true)
            .set("quotes", new ObjectMapper().createObjectNode().put("USDTRY", 38.5));

    when(restTemplate.getForObject(anyString(), eq(JsonNode.class))).thenReturn(mockResponse);

    double rate = client.getRate("USD", "TRY");
    assertEquals(38.5, rate);
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    public RestTemplate restTemplate() {
      return Mockito.mock(RestTemplate.class);
    }
  }
}
