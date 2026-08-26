package com.atlashub.identity.domain.repository;

import com.atlashub.identity.domain.model.Merchant;

import java.util.Optional;

public interface MerchantRepository {
    Long nextIdentity();
    Merchant save(Merchant merchant);
    Optional<Merchant> findById(Long id);
    Optional<Merchant> findByEmail(String email);
}
