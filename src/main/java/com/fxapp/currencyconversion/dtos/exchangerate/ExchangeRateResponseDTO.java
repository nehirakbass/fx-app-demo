package com.fxapp.currencyconversion.dtos.exchangerate;

import lombok.*;

@Builder
@Getter
@Setter
@Data
@AllArgsConstructor
public class ExchangeRateResponseDTO {
  private String sourceCurrency;
  private String targetCurrency;
  private Double rate;
}
