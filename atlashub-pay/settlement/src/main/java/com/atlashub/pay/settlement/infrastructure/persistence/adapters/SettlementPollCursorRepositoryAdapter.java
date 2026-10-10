package com.atlashub.pay.settlement.infrastructure.persistence.adapters;

import com.atlashub.pay.settlement.domain.entities.SettlementPollCursor;
import com.atlashub.pay.settlement.domain.repositories.SettlementPollCursorRepository;
import com.atlashub.pay.settlement.infrastructure.persistence.entities.SettlementPollCursorJpa;
import com.atlashub.pay.settlement.infrastructure.persistence.mappers.SettlementPollCursorMapper;
import com.atlashub.pay.settlement.infrastructure.persistence.repositories.SpringDataSettlementPollCursorRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class SettlementPollCursorRepositoryAdapter extends JpaBaseRepository<SettlementPollCursor,SettlementPollCursorJpa>
        implements SettlementPollCursorRepository {
    private final SpringDataSettlementPollCursorRepository repository;
    public SettlementPollCursorRepositoryAdapter(SpringDataSettlementPollCursorRepository repository,
            SettlementPollCursorMapper mapper,DomainSequenceGenerator sequence,DomainEventPublisher publisher){
        super(repository,mapper,sequence,publisher);this.repository=repository;
    }
    @Override protected String getSequenceName(){return "pay_settlement_poll_cursor_seq";}
    @Override public Optional<SettlementPollCursor> findByEnvironmentAndProviderAndSubaccountCode(
            ApiEnvironment env,String provider,String code){return repository
            .findByEnvironmentAndProviderAndSubaccountCode(env,provider,code).map(mapper::toDomain);}
}
