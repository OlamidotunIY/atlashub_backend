package com.atlashub.pay.charges.domain.exceptions;
import com.atlashub.shared.domain.exception.BusinessRuleException;
public class InvalidChargeException extends BusinessRuleException { public InvalidChargeException(String message){super(message);} }
