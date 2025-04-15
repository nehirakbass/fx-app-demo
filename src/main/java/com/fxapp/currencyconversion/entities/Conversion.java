package com.fxapp.currencyconversion.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
}
