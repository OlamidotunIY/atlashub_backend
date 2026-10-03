package com.atlashub.pay.splits.application.commands.AddSplitSubaccount;

import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.pay.splits.domain.entities.SplitSubaccount;
import com.atlashub.pay.splits.domain.exceptions.SplitRuleNotFoundException;
import com.atlashub.pay.splits.domain.repositories.SplitRuleRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class AddSplitSubaccountHandler extends Command<AddSplitSubaccountCommand, Void> {

    private final SplitRuleRepository splitRuleRepository;

    public AddSplitSubaccountHandler(SplitRuleRepository splitRuleRepository) {
        this.splitRuleRepository = splitRuleRepository;
    }

    @PreAuthorize("hasAuthority('pay:splits:manage')")
    @Override
    public Void execute(AddSplitSubaccountCommand command) {
        SplitRule rule = splitRuleRepository.findByIdAndOrganizationId(command.splitRuleId(), command.organizationId()).orElseThrow(SplitRuleNotFoundException::new);
        SplitSubaccount subaccount = new SplitSubaccount(null, command.splitRuleId(), command.recipientType(), command.recipientId(), command.share(), command.description());
        rule.addSubaccount(subaccount);
        splitRuleRepository.save(rule);
        return null;
    }
}
