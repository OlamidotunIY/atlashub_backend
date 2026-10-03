package com.atlashub.pay.splits.application.commands.UpdateSplitRule;

import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.pay.splits.domain.entities.SplitSubaccount;
import com.atlashub.pay.splits.domain.exceptions.SplitRuleNotFoundException;
import com.atlashub.pay.splits.domain.repositories.SplitRuleRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UpdateSplitRuleHandler extends Command<UpdateSplitRuleCommand, Void> {

    private final SplitRuleRepository splitRuleRepository;

    public UpdateSplitRuleHandler(SplitRuleRepository splitRuleRepository) {
        this.splitRuleRepository = splitRuleRepository;
    }

    @PreAuthorize("hasAuthority('pay:splits:manage')")
    @Override
    public Void execute(UpdateSplitRuleCommand command) {
        SplitRule rule = splitRuleRepository.findByIdAndOrganizationId(command.splitRuleId(), command.organizationId()).orElseThrow(SplitRuleNotFoundException::new);
        List<SplitSubaccount> subaccountEntities = command.subaccounts().stream().map(item -> new SplitSubaccount(null, command.splitRuleId(), item.recipientType(), item.recipientId(), item.share(), item.description())).collect(Collectors.toList());
        rule.update(command.name(), command.type(), command.platformFeePercentage(), subaccountEntities);
        splitRuleRepository.save(rule);
        return null;
    }
}
