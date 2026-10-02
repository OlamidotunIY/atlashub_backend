package com.atlashub.accounts.domain.repositories;

import com.atlashub.accounts.domain.entities.Outlet;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;

public interface OutletRepository extends Repository<Outlet> {
    List<Outlet> findAllByOrganizationId(Long organizationId);
}
