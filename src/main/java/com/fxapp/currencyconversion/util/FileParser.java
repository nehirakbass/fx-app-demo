package com.fxapp.currencyconversion.util;

import com.fxapp.currencyconversion.dtos.currencychange.CurrencyChangeRequestDTO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

public class FileParser {
  private static final List<String> EXPECTED_HEADERS =
      List.of("SOURCE_CURRENCY", "TARGET_CURRENCY", "AMOUNT", "USERNAME");

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
                  .username(parts[3].trim())
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
          String username = row.getCell(3).getStringCellValue().trim().toUpperCase();

          CurrencyChangeRequestDTO dto =
              CurrencyChangeRequestDTO.builder()
                  .sourceCurrency(source)
                  .targetCurrency(target)
                  .amount(amount)
                  .username(username)
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

    for (int i = 0; i < EXPECTED_HEADERS.size(); i++) {
      if (!headers[i].trim().equalsIgnoreCase(EXPECTED_HEADERS.get(i))) {
        throw new IllegalArgumentException(
            "Invalid header. Expected: " + String.join(", ", EXPECTED_HEADERS));
      }
    }
  }

  private static void validateExcelHeader(Row headerRow) {
    for (int i = 0; i < EXPECTED_HEADERS.size(); i++) {
      Cell cell = headerRow.getCell(i);
      String actual =
          (cell != null && cell.getCellType() == CellType.STRING)
              ? cell.getStringCellValue().trim()
              : "";

      if (!EXPECTED_HEADERS.get(i).equalsIgnoreCase(actual)) {
        throw new IllegalArgumentException(
            "Invalid XLSX header. Expected: " + String.join(", ", EXPECTED_HEADERS));
      }
    }
  }
}
