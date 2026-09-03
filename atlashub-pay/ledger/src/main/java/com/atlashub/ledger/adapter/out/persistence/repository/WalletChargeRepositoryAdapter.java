package com.atlashub.ledger.adapter.out.persistence.repository;

import com.atlashub.ledger.adapter.out.persistence.mapper.WalletChargeMapper;
import com.atlashub.ledger.domain.model.WalletCharge;
import com.atlashub.ledger.domain.repository.WalletChargeRepository;
import com.atlashub.shared.adapter.out.external.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

@Component
public class WalletChargeRepositoryAdapter implements WalletChargeRepository {
    private final SpringDataWalletChargeRepository jpaRepository;
    private final WalletChargeMapper mapper;
    private final DomainSequenceGenerator sequenceGenerator;

    public WalletChargeRepositoryAdapter(
            SpringDataWalletChargeRepository jpaRepository,
            WalletChargeMapper mapper,
            DomainSequenceGenerator sequenceGenerator) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("wallet_charge_seq");
    }

    @Override
    public WalletCharge save(WalletCharge charge) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(charge)));
    }
}
