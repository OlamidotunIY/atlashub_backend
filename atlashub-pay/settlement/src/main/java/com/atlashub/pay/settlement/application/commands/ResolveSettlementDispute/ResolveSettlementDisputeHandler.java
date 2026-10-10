package com.atlashub.pay.settlement.application.commands.ResolveSettlementDispute;
import com.atlashub.pay.settlement.domain.exceptions.SettlementNotFoundException;
import com.atlashub.pay.settlement.domain.repositories.SettlementRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
@Component public class ResolveSettlementDisputeHandler extends Command<ResolveSettlementDisputeCommand,Void>{
    private final SettlementRepository repository;
    public ResolveSettlementDisputeHandler(SettlementRepository repository){this.repository=repository;}
    @Override @PreAuthorize("hasAuthority('pay:settlement:manage')") public Void execute(ResolveSettlementDisputeCommand command){
        var settlement=repository.findById(command.settlementId()).filter(s->s.getOrganizationId().equals(command.organizationId())&&s.getEnvironment()==command.environment())
                .orElseThrow(()->new SettlementNotFoundException(command.settlementId()));
        settlement.resolveDispute();repository.save(settlement);return null;}
}
