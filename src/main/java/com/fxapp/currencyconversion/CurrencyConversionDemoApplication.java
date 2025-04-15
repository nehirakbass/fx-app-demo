package com.fxapp.currencyconversion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CurrencyConversionDemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(CurrencyConversionDemoApplication.class, args);
  }
}
