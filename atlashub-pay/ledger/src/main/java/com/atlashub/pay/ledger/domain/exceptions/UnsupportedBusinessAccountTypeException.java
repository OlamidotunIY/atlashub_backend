package com.atlashub.pay.ledger.domain.exceptions;
import com.atlashub.shared.domain.exception.BusinessRuleException;
public class UnsupportedBusinessAccountTypeException extends BusinessRuleException {public UnsupportedBusinessAccountTypeException(String type){super("Account type cannot be created by a business: "+type);}}
