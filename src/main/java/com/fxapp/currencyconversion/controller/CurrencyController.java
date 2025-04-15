package com.fxapp.currencyconversion.controller;

import com.fxapp.currencyconversion.dtos.PageResponse;
import com.fxapp.currencyconversion.dtos.conversionhistory.ConversionHistoryRequestDTO;
import com.fxapp.currencyconversion.dtos.conversionhistory.ConversionHistoryResponseDTO;
import com.fxapp.currencyconversion.dtos.currencychange.CurrencyChangeRequestDTO;
import com.fxapp.currencyconversion.dtos.currencychange.CurrencyChangeResponseDTO;
import com.fxapp.currencyconversion.dtos.exchangerate.ExchangeRateRequestDTO;
import com.fxapp.currencyconversion.dtos.exchangerate.ExchangeRateResponseDTO;
import com.fxapp.currencyconversion.service.ExchangeService;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class CurrencyController {

  private final ExchangeService exchangeService;

  public CurrencyController(ExchangeService exchangeService) {
    this.exchangeService = exchangeService;
  }

  @GetMapping("/check-rate")
  public ResponseEntity<ExchangeRateResponseDTO> exchangeRate(
      @RequestParam ExchangeRateRequestDTO request) {
    return ResponseEntity.ok(exchangeService.checkRate(request));
  }

  @PostMapping("/currency-change")
  public ResponseEntity<CurrencyChangeResponseDTO> currencyChange(
      @RequestBody CurrencyChangeRequestDTO request) {
    return ResponseEntity.ok(exchangeService.currencyChange(request));
  }

  @GetMapping("/conversion-history")
  public ResponseEntity<PageResponse<ConversionHistoryResponseDTO>> conversionHistory(
      @RequestParam ConversionHistoryRequestDTO request,
      @PageableDefault(size = 10, page = 0) Pageable pageable) {
    return ResponseEntity.ok(exchangeService.getConversionHistory(request, pageable));
  }

  @PostMapping("/bulk-convert")
  public ResponseEntity<List<CurrencyChangeResponseDTO>> uploadFile(
      @RequestParam("file") MultipartFile file) {
    return ResponseEntity.ok(exchangeService.uploadFile(file));
  }
}
