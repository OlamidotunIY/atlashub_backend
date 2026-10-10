package com.atlashub.pay.ledger.domain.exceptions;
import com.atlashub.shared.domain.exception.ConflictException;
public class DuplicateLedgerAccountException extends ConflictException {public DuplicateLedgerAccountException(String name){super("Ledger account already exists: "+name);}}
