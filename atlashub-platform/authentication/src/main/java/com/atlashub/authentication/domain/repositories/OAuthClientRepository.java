package com.atlashub.authentication.domain.repositories;

import com.atlashub.authentication.domain.entities.OAuthClient;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface OAuthClientRepository extends Repository<OAuthClient> {
    Optional<OAuthClient> findByClientId(String clientId);
}
