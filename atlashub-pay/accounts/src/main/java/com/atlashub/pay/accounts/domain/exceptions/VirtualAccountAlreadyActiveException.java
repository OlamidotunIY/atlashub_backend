package com.atlashub.pay.accounts.domain.exceptions;

public class VirtualAccountAlreadyActiveException extends RuntimeException {
  public VirtualAccountAlreadyActiveException(String message) {
    super(message);
  }
}
