package com.fxapp.currencyconversion.exception;

import com.fxapp.currencyconversion.constants.ResultCode;
import com.fxapp.currencyconversion.dtos.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(FxException.class)
  public ResponseEntity<ErrorResponseDTO> handleAppException(FxException ex) {
    ResultCode code = ex.getResultCode();
    return ResponseEntity.status(code.getHttpStatus())
        .body(new ErrorResponseDTO(code.getCode(), ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex) {
    log.error("Exception: ", ex);
    ResultCode resultCode = ResultCode.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(resultCode.getHttpStatus())
        .body(new ErrorResponseDTO(resultCode.getCode(), resultCode.getMessage()));
  }
}
