package com.fxapp.currencyconversion.service.mapper;

import com.fxapp.currencyconversion.dtos.conversionhistory.ConversionHistoryResponseDTO;
import com.fxapp.currencyconversion.entities.Conversion;

public class ConversionMapper {
  public static ConversionHistoryResponseDTO mapConversionHistoryToDTO(Conversion conversion) {
    return ConversionHistoryResponseDTO.builder()
        .transactionId(conversion.getTransactionId())
        .sourceCurrency(conversion.getSourceCurrency())
        .targetCurrency(conversion.getTargetCurrency())
        .amount(conversion.getAmount())
        .convertedAmount(conversion.getConvertedAmount())
        .rate(conversion.getRate())
        .timestamp(conversion.getTimestamp())
        .build();
  }
}
