package com.fxapp.currencyconversion.util;

import com.fxapp.currencyconversion.dtos.currencychange.CurrencyChangeRequestDTO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

public class FileParser {
  private static final List<String> EXPECTED_HEADERS =
      List.of("SOURCE_CURRENCY", "TARGET_CURRENCY", "AMOUNT");

  public static List<CurrencyChangeRequestDTO> parseBulkCurrencyChangeRequestCsv(MultipartFile file)
      throws IOException {
    List<CurrencyChangeRequestDTO> requests = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
      String headerLine = reader.readLine();

      validateCsvHeader(headerLine);

      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.trim().split(",");

        if (parts.length != 3) continue;

        try {
          CurrencyChangeRequestDTO dto =
              CurrencyChangeRequestDTO.builder()
                  .sourceCurrency(parts[0].trim().toUpperCase())
                  .targetCurrency(parts[1].trim().toUpperCase())
                  .amount(Double.parseDouble(parts[2].trim()))
                  .build();

          requests.add(dto);
        } catch (Exception e) {
          System.err.println("Skipping invalid line: " + line + " - " + e.getMessage());
        }
      }
    }

    return requests;
  }

  public static List<CurrencyChangeRequestDTO> parseBulkCurrencyChangeRequestXlsx(
      MultipartFile file) throws IOException {
    List<CurrencyChangeRequestDTO> list = new ArrayList<>();

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);

      boolean isHeader = true;
      for (Row row : sheet) {
        if (isHeader) {
          validateExcelHeader(row);
          isHeader = false;
          continue;
        }

        try {
          String source = row.getCell(0).getStringCellValue().trim().toUpperCase();
          String target = row.getCell(1).getStringCellValue().trim().toUpperCase();
          double amount = row.getCell(2).getNumericCellValue();

          CurrencyChangeRequestDTO dto =
              CurrencyChangeRequestDTO.builder()
                  .sourceCurrency(source)
                  .targetCurrency(target)
                  .amount(amount)
                  .build();

          list.add(dto);
        } catch (Exception e) {
          System.err.println("Skipping row: " + row.getRowNum() + " -> " + e.getMessage());
        }
      }
    }

    return list;
  }

  private static void validateCsvHeader(String headerLine) {
    if (headerLine == null) {
      throw new IllegalArgumentException("Header is missing.");
    }

    String[] headers = headerLine.trim().split(",");

    if (headers.length != 3
        || !headers[0].trim().equalsIgnoreCase(EXPECTED_HEADERS.get(0))
        || !headers[1].trim().equalsIgnoreCase(EXPECTED_HEADERS.get(1))
        || !headers[2].trim().equalsIgnoreCase(EXPECTED_HEADERS.get(2))) {
      throw new IllegalArgumentException(
          "Invalid header. Expected: " + String.join(", ", EXPECTED_HEADERS));
    }
  }

  private static void validateExcelHeader(Row headerRow) {
    if (!"SOURCE_CURRENCY".equalsIgnoreCase(headerRow.getCell(0).getStringCellValue().trim())
        || !"TARGET_CURRENCY".equalsIgnoreCase(headerRow.getCell(1).getStringCellValue().trim())
        || !"AMOUNT".equalsIgnoreCase(headerRow.getCell(2).getStringCellValue().trim())) {
      throw new IllegalArgumentException(
          "Invalid XLSX header. Expected: SOURCE_CURRENCY, TARGET_CURRENCY, AMOUNT");
    }
  }
}
