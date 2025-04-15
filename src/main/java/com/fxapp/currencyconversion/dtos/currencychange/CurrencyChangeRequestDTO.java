package com.fxapp.currencyconversion.dtos.currencychange;

import lombok.*;

@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrencyChangeRequestDTO {
  private String sourceCurrency;
  private String targetCurrency;
  private Double amount;
  private String username;
}
