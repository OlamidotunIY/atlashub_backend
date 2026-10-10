package com.atlashub.pay.charges.domain.exceptions;
import com.atlashub.shared.domain.exception.NotFoundException;
public class ChargeNotFoundException extends NotFoundException { public ChargeNotFoundException(String reference){super("Charge not found: "+reference);} }
