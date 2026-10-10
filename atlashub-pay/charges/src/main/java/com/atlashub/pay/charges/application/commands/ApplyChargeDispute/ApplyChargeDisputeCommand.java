package com.atlashub.pay.charges.application.commands.ApplyChargeDispute;

import com.atlashub.shared.application.security.ApiEnvironment;

public record ApplyChargeDisputeCommand(ApiEnvironment environment,String transactionReference,
                                        String disputeReference,String eventType,String reason){}
