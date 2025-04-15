package com.fxapp.currencyconversion.exception;

import com.fxapp.currencyconversion.constants.ResultCode;

public class FxException extends RuntimeException {
  private final ResultCode resultCode;

  public FxException(ResultCode resultCode) {
    super(resultCode.getMessage());
    this.resultCode = resultCode;
  }

  public ResultCode getResultCode() {
    return resultCode;
  }
}
