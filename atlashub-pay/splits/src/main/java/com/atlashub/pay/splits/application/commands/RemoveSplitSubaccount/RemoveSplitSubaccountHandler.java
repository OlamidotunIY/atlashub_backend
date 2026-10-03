package com.atlashub.pay.splits.application.commands.RemoveSplitSubaccount;

import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.pay.splits.domain.exceptions.SplitRuleNotFoundException;
import com.atlashub.pay.splits.domain.repositories.SplitRuleRepository;
import org.springframework.stereotype.Component;

@Component
public class RemoveSplitSubaccountHandler extends Command<RemoveSplitSubaccountCommand, Void> {

    private final SplitRuleRepository splitRuleRepository;

    public RemoveSplitSubaccountHandler(SplitRuleRepository splitRuleRepository) {
        this.splitRuleRepository = splitRuleRepository;
    }

    @PreAuthorize("hasAuthority('pay:splits:manage')")
    @Override
    public Void execute(RemoveSplitSubaccountCommand command) {
        SplitRule rule = splitRuleRepository.findByIdAndOrganizationId(command.splitRuleId(), command.organizationId())
                .orElseThrow(SplitRuleNotFoundException::new);
        rule.removeSubaccount(command.subaccountId());
        splitRuleRepository.save(rule);
        return null;
    }
}
