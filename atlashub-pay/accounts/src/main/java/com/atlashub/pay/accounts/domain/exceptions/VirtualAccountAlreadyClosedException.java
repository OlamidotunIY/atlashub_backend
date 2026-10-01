package com.atlashub.pay.accounts.domain.exceptions;

public class VirtualAccountAlreadyClosedException extends RuntimeException {
  public VirtualAccountAlreadyClosedException(String message) {
    super(message);
  }
}
