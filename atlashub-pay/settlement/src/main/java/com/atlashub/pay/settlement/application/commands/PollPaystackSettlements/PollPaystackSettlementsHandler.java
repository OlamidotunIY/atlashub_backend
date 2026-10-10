package com.atlashub.pay.settlement.application.commands.PollPaystackSettlements;

import com.atlashub.pay.settlement.application.commands.RecordSettlement.RecordSettlementCommand;
import com.atlashub.pay.settlement.application.commands.RecordSettlement.RecordSettlementHandler;
import com.atlashub.pay.settlement.domain.entities.SettlementPollCursor;
import com.atlashub.shared.application.port.SettlementProviderPort;
import com.atlashub.pay.settlement.domain.repositories.SettlementPollCursorRepository;
import com.atlashub.pay.settlement.domain.valueobject.PaymentProvider;
import com.atlashub.shared.application.port.SettlementRouteQueryPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class PollPaystackSettlementsHandler extends Command<PollPaystackSettlementsCommand,Integer> {
    private final SettlementRouteQueryPort routes;
    private final SettlementProviderPort provider;
    private final SettlementPollCursorRepository cursors;
    private final RecordSettlementHandler recordHandler;
    public PollPaystackSettlementsHandler(SettlementRouteQueryPort routes,SettlementProviderPort provider,
            SettlementPollCursorRepository cursors,RecordSettlementHandler recordHandler){
        this.routes=routes;this.provider=provider;this.cursors=cursors;this.recordHandler=recordHandler;
    }
    @Override public Integer execute(PollPaystackSettlementsCommand command){int count=0;
        for(var route:routes.findActiveRoutes(ApiEnvironment.LIVE)){
            SettlementPollCursor cursor=cursors.findByEnvironmentAndProviderAndSubaccountCode(route.environment(),
                    route.provider(),route.providerSubaccountCode()).orElseGet(()->cursors.save(
                    SettlementPollCursor.start(cursors.nextIdentity(),route.environment(),route.provider(),
                            route.providerSubaccountCode())));
            var batches=provider.fetchSuccessfulSettlements(route.environment(),route.providerSubaccountCode(),
                    cursor.getLastProviderSettlementId());
            for(var batch:batches){recordHandler.execute(new RecordSettlementCommand(route.organizationId(),
                    route.environment(),PaymentProvider.PAYSTACK,batch.providerSettlementId(),
                    route.providerSubaccountCode(),route.anchorDepositAccountId(),batch.grossAmount(),batch.netAmount(),
                    batch.providerFeeAmount(),batch.settledAt(),batch.transactionReferences()));
                cursor.advance(batch.providerSettlementId());cursors.save(cursor);count++;}
        }return count;}
}
