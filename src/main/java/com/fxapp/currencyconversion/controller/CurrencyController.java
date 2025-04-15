package com.fxapp.currencyconversion.controller;

import com.fxapp.currencyconversion.dtos.PageResponse;
import com.fxapp.currencyconversion.dtos.conversionhistory.ConversionHistoryRequestDTO;
import com.fxapp.currencyconversion.dtos.conversionhistory.ConversionHistoryResponseDTO;
import com.fxapp.currencyconversion.dtos.currencychange.CurrencyChangeRequestDTO;
import com.fxapp.currencyconversion.dtos.currencychange.CurrencyChangeResponseDTO;
import com.fxapp.currencyconversion.dtos.exchangerate.ExchangeRateRequestDTO;
import com.fxapp.currencyconversion.dtos.exchangerate.ExchangeRateResponseDTO;
import com.fxapp.currencyconversion.service.ExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.*;
import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

  private final ExchangeService exchangeService;

  public CurrencyController(ExchangeService exchangeService) {
    this.exchangeService = exchangeService;
  }

  @PostMapping("/check-rate")
  @Operation(summary = "Retrieves current rate for the given source and target currencies")
  public ResponseEntity<ExchangeRateResponseDTO> exchangeRate(
      @RequestBody ExchangeRateRequestDTO request) {
    return ResponseEntity.ok(exchangeService.checkRate(request));
  }

  @PostMapping("/currency-change")
  @Operation(summary = "Converts given amount in source currency to the target currency")
  public ResponseEntity<CurrencyChangeResponseDTO> currencyChange(
      @RequestBody CurrencyChangeRequestDTO request) {
    return ResponseEntity.ok(exchangeService.currencyChange(request));
  }

  @PostMapping("/conversion-history")
  @Operation(
      summary =
          "Retrieves a paged list of conversions with given parameters(at least one must be provided)")
  public ResponseEntity<PageResponse<ConversionHistoryResponseDTO>> conversionHistory(
      @RequestBody ConversionHistoryRequestDTO request,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(exchangeService.getConversionHistory(request, pageable));
  }

  @PostMapping(value = "/bulk-convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Upload a file for bulk currency conversion")
  public ResponseEntity<List<CurrencyChangeResponseDTO>> uploadFile(
      @Parameter(
              description = "CSV or XLSX file",
              required = true,
              content =
                  @Content(
                      mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                      schema = @Schema(type = "string", format = "binary")))
          @RequestParam("file")
          MultipartFile file)
      throws IOException {
    return ResponseEntity.ok(exchangeService.uploadFile(file));
  }
}
