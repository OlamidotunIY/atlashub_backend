package com.atlashub.accounts.domain.repositories;

import com.atlashub.accounts.domain.entities.Outlet;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface OutletRepository extends Repository<Outlet> {
    Optional<Outlet> findById(Long id);
    List<Outlet> findAllByOrganizationId(Long organizationId);
}
