package com.atlashub.pay.splits.application.commands.DeactivateSplitRule;

import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.pay.splits.domain.exceptions.SplitRuleNotFoundException;
import com.atlashub.pay.splits.domain.repositories.SplitRuleRepository;
import org.springframework.stereotype.Component;

@Component
public class DeactivateSplitRuleHandler extends Command<DeactivateSplitRuleCommand, Void> {

    private final SplitRuleRepository splitRuleRepository;

    public DeactivateSplitRuleHandler(SplitRuleRepository splitRuleRepository) {
        this.splitRuleRepository = splitRuleRepository;
    }

    @PreAuthorize("hasAuthority('pay:splits:manage')")
    @Override
    public Void execute(DeactivateSplitRuleCommand command) {
        SplitRule rule = splitRuleRepository.findByIdAndOrganizationId(command.splitRuleId(), command.organizationId())
                .orElseThrow(SplitRuleNotFoundException::new);
        rule.deactivate();
        splitRuleRepository.save(rule);
        return null;
    }
}
