package com.fxapp.currencyconversion.dtos.currencychange;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@Builder
public class CurrencyChangeResponseDTO {
  private UUID transactionId;
  private Double convertedAmount;
}
