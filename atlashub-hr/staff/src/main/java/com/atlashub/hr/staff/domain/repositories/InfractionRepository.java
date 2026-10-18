package com.atlashub.hr.staff.domain.repositories;

import com.atlashub.hr.staff.domain.entities.Infraction;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;

public interface InfractionRepository extends Repository<Infraction> {

    List<Infraction> findByOrganizationId(Long organizationId);

    List<Infraction> findByEmployeeId(Long employeeId);
}
