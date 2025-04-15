package com.fxapp.currencyconversion;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
@Profile("test")
public class TestRestTemplateConfig {

  @Bean
  public RestTemplate restTemplate() {
    return Mockito.mock(RestTemplate.class);
  }
}
