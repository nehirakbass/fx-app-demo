package com.fxapp.currencyconversion.service;

import com.fxapp.currencyconversion.dtos.PageResponse;
import com.fxapp.currencyconversion.dtos.conversionhistory.ConversionHistoryRequestDTO;
import com.fxapp.currencyconversion.dtos.conversionhistory.ConversionHistoryResponseDTO;
import com.fxapp.currencyconversion.dtos.currencychange.CurrencyChangeRequestDTO;
import com.fxapp.currencyconversion.dtos.currencychange.CurrencyChangeResponseDTO;
import com.fxapp.currencyconversion.dtos.exchangerate.ExchangeRateRequestDTO;
import com.fxapp.currencyconversion.dtos.exchangerate.ExchangeRateResponseDTO;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ExchangeService {
  ExchangeRateResponseDTO checkRate(ExchangeRateRequestDTO request);

  CurrencyChangeResponseDTO currencyChange(CurrencyChangeRequestDTO request);

  PageResponse<ConversionHistoryResponseDTO> getConversionHistory(
      ConversionHistoryRequestDTO request, Pageable pageable);

  List<CurrencyChangeResponseDTO> uploadFile(MultipartFile file);
}
