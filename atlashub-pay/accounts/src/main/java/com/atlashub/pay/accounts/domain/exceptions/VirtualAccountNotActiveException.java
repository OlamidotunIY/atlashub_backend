package com.atlashub.pay.accounts.domain.exceptions;

public class VirtualAccountNotActiveException extends RuntimeException {
  public VirtualAccountNotActiveException(String message) {
    super(message);
  }
}
