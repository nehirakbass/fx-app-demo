package com.fxapp.currencyconversion.constants;

import org.springframework.http.HttpStatus;

public enum ResultCode {
  SUCCESS("00", "Operation completed successfully", HttpStatus.OK),
  INVALID_CURRENCY("01", "One or both currency codes are invalid", HttpStatus.BAD_REQUEST),
  INVALID_AMOUNT("02", "Amount must be greater than 0", HttpStatus.BAD_REQUEST),
  EXTERNAL_API_ERROR(
      "03", "Failed to fetch exchange rate from provider", HttpStatus.SERVICE_UNAVAILABLE),
  INTERNAL_SERVER_ERROR("99", "Unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
  NULL_CURRENCY_CODE("04", "Null currency code", HttpStatus.BAD_REQUEST),
  AMOUNT_MUST_BE_BIGGER_THAN_ZERO(
      "05", "Amount value must be bigger than zero", HttpStatus.BAD_REQUEST),
  NULL_TRANSACTION_ID("06", "Null transaction id", HttpStatus.BAD_REQUEST),
  CONVERSION_NOT_FOUND("07", "Conversion history not found", HttpStatus.NOT_FOUND),
  AT_LEAST_ONE_FILTER_REQUIRED(
      "08",
      "At least one filter (transactionId or transactionDate) must be provided",
      HttpStatus.BAD_REQUEST),
  FILE_PARSING_FAILED(
      "09", "Parsing the bulk request file failed", HttpStatus.INTERNAL_SERVER_ERROR),
  FILE_FORMAT_NOT_SUPPORTED("10", "Given file format is not supported", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;

  ResultCode(String code, String message, HttpStatus httpStatus) {
    this.code = code;
    this.message = message;
    this.httpStatus = httpStatus;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }
}
