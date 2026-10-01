package com.atlashub.pay.accounts.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class VirtualAccountInactiveException extends BusinessRuleException {
  public VirtualAccountInactiveException() { super("Virtual account is not active"); }
}
