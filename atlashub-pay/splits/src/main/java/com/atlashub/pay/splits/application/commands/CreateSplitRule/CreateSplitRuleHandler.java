package com.atlashub.pay.splits.application.commands.CreateSplitRule;

import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.pay.splits.domain.entities.SplitSubaccount;
import com.atlashub.pay.splits.domain.repositories.SplitRuleRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CreateSplitRuleHandler extends Command<CreateSplitRuleCommand, CreateSplitRuleResponse> {

    private final SplitRuleRepository splitRuleRepository;

    public CreateSplitRuleHandler(SplitRuleRepository splitRuleRepository) {
        this.splitRuleRepository = splitRuleRepository;
    }

    @PreAuthorize("hasAuthority('pay:splits:manage')")
    @Override
    public CreateSplitRuleResponse execute(CreateSplitRuleCommand command) {
        Long ruleId = splitRuleRepository.nextIdentity();
        List<SplitSubaccount> subaccountEntities = command.subaccounts().stream()
                .map(item -> new SplitSubaccount(null, ruleId, item.recipientType(), item.recipientId(), item.share(), item.description()))
                .collect(Collectors.toList());
        SplitRule rule = SplitRule.create(ruleId, command.organizationId(), command.name(), command.type(), command.platformFeePercentage(), subaccountEntities);
        splitRuleRepository.save(rule);
        return new CreateSplitRuleResponse(ruleId);
    }
}
