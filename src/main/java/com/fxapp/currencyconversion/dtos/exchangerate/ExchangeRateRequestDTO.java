package com.fxapp.currencyconversion.dtos.exchangerate;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class ExchangeRateRequestDTO {
  private String sourceCurrency;
  private String targetCurrency;
}
