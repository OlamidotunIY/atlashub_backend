package com.atlashub.pay.txquery.application.commands.ProjectAccountFunding;

import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;
import java.util.Map;

public record ProjectAccountFundingCommand(Long organizationId, String environment, String reference, Money amount,
                                           Long accountId, String partyType, String partyReferenceId,
                                           String provider, String description, Map<String, String> metadata,
                                           ZonedDateTime occurredAt) {

    public ProjectAccountFundingCommand(Long organizationId, String environment, String reference, Money amount,
                                        Long accountId, String partyType, String partyReferenceId,
                                        String provider, ZonedDateTime occurredAt) {
        this(organizationId, environment, reference, amount, accountId, partyType, partyReferenceId,
                provider, null, Map.of(), occurredAt);
    }
}
