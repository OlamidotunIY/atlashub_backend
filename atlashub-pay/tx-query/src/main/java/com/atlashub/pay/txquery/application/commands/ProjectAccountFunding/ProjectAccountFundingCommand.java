package com.atlashub.pay.txquery.application.commands.ProjectAccountFunding;

import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;

public record ProjectAccountFundingCommand(Long organizationId, String environment, String reference, Money amount,
                                           Long accountId, String partyType, String partyReferenceId,
                                           String provider, ZonedDateTime occurredAt) {
}
