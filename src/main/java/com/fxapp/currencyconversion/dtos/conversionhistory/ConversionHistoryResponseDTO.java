package com.fxapp.currencyconversion.dtos.conversionhistory;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ConversionHistoryResponseDTO {
  private UUID transactionId;

  private String sourceCurrency;
  private String targetCurrency;
  private double amount;
  private double convertedAmount;
  private double rate;
  private LocalDateTime timestamp;
}
