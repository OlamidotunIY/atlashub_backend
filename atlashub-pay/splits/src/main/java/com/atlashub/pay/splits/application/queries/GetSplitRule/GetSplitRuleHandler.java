package com.atlashub.pay.splits.application.queries.GetSplitRule;

import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.pay.splits.domain.exceptions.SplitRuleNotFoundException;
import com.atlashub.pay.splits.domain.repositories.SplitRuleRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class GetSplitRuleHandler extends Query<GetSplitRuleQuery, SplitRuleResult> {

    private final SplitRuleRepository splitRuleRepository;

    public GetSplitRuleHandler(SplitRuleRepository splitRuleRepository) {
        this.splitRuleRepository = splitRuleRepository;
    }

    @PreAuthorize("hasAuthority('pay:splits:read')")
    @Override
    public SplitRuleResult execute(GetSplitRuleQuery query) {
        SplitRule rule = splitRuleRepository.findByIdAndOrganizationId(query.splitRuleId(), query.organizationId()).orElseThrow(SplitRuleNotFoundException::new);

        return new SplitRuleResult(rule.getId(), rule.getOrganizationId(), rule.getName(), rule.getType().name(), rule.getPlatformFeePercentage(), rule.getSubaccounts().stream().map(s -> new SplitRuleResult.SplitSubaccountResult(s.getId(), s.getRecipientType().name(), s.getRecipientId(), s.getShare(), s.getDescription())).collect(Collectors.toList()), rule.isActive(), rule.getCreatedAt(), rule.getUpdatedAt());
    }
}
