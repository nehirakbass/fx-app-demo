package com.fxapp.currencyconversion.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Conversion {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID transactionId;

  private String sourceCurrency;
  private String targetCurrency;
  private double amount;
  private double convertedAmount;
  private double rate;
  private LocalDateTime timestamp;

  @ManyToOne(fetch = FetchType.LAZY)
  private User user;
}
