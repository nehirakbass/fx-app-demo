package com.fxapp.currencyconversion.dtos.currencychange;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyChangeRequestDTO {
  private String sourceCurrency;
  private String targetCurrency;
  private Double amount;
}
