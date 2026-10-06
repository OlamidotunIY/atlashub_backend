package com.atlashub.pay.splits.application.queries.ListSplitRules;

import com.atlashub.pay.splits.application.queries.GetSplitRule.SplitRuleResult;
import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.pay.splits.domain.repositories.SplitRuleRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListSplitRulesHandler extends Query<ListSplitRulesQuery, List<SplitRuleResult>> {

    private final SplitRuleRepository splitRuleRepository;

    public ListSplitRulesHandler(SplitRuleRepository splitRuleRepository) {
        this.splitRuleRepository = splitRuleRepository;
    }

    @PreAuthorize("hasAuthority('pay:splits:read')")
    @Override
    public List<SplitRuleResult> execute(ListSplitRulesQuery query) {
        List<SplitRule> rules = splitRuleRepository.findAllByOrganizationId(query.organizationId());

        return rules.stream().map(rule -> new SplitRuleResult(rule.getId(), rule.getOrganizationId(), rule.getName(), rule.getType().name(), rule.getPlatformFeePercentage(), rule.getSubaccounts().stream().map(s -> new SplitRuleResult.SplitSubaccountResult(s.getId(), s.getRecipientType().name(), s.getRecipientId(), s.getShare(), s.getDescription())).collect(Collectors.toList()), rule.isActive(), rule.getCreatedAt(), rule.getUpdatedAt())).collect(Collectors.toList());
    }
}
