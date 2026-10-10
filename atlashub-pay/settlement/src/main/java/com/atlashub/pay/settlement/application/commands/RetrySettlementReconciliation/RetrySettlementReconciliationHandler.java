package com.atlashub.pay.settlement.application.commands.RetrySettlementReconciliation;
import com.atlashub.pay.settlement.domain.exceptions.SettlementNotFoundException;
import com.atlashub.pay.settlement.domain.repositories.SettlementRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
@Component public class RetrySettlementReconciliationHandler extends Command<RetrySettlementReconciliationCommand,Void>{
    private final SettlementRepository repository;
    public RetrySettlementReconciliationHandler(SettlementRepository repository){this.repository=repository;}
    @Override @PreAuthorize("hasAuthority('pay:settlement:manage')") public Void execute(RetrySettlementReconciliationCommand command){
        var settlement=repository.findById(command.settlementId()).filter(s->s.getOrganizationId().equals(command.organizationId())&&s.getEnvironment()==command.environment())
                .orElseThrow(()->new SettlementNotFoundException(command.settlementId()));
        settlement.retryReconciliation();repository.save(settlement);return null;}
}
