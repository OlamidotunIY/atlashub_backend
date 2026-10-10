package com.atlashub.pay.charges.domain.exceptions;
import com.atlashub.shared.domain.exception.BusinessRuleException;
public class ChargeProviderException extends BusinessRuleException{public ChargeProviderException(String message){super(message);}public ChargeProviderException(String message,Throwable cause){super(message,cause);}}
