package com.atlashub.pay.accounts.domain.exceptions;

public class VirtualAccountInactiveException extends RuntimeException {
  public VirtualAccountInactiveException(String message) {
    super(message);
  }
}
