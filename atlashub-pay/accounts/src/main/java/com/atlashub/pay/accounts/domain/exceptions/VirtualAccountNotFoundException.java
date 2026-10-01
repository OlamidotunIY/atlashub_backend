package com.atlashub.pay.accounts.domain.exceptions;

public class VirtualAccountNotFoundException extends RuntimeException {
  public VirtualAccountNotFoundException(String message) {
    super(message);
  }
}
