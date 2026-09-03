package com.atlashub.ledger.domain.repository;

import com.atlashub.ledger.domain.model.WalletCharge;

public interface WalletChargeRepository {
    Long nextIdentity();
    WalletCharge save(WalletCharge charge);
}
