package com.fxapp.currencyconversion.dtos.conversionhistory;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ConversionHistoryRequestDTO {
  private UUID transactionId;
  private LocalDateTime transactionDate;
  private String username;
}
