package com.atlashub.commerce.catalog.application.queries.ListSuppliers;

import com.atlashub.commerce.catalog.domain.entities.Supplier;
import com.atlashub.commerce.catalog.domain.repositories.SupplierRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ListSuppliersHandler extends Query<ListSuppliersQuery, List<SupplierResult>> {

    private final SupplierRepository supplierRepository;

    public ListSuppliersHandler(SupplierRepository supplierRepository) {
        this.supplierRepository = Objects.requireNonNull(supplierRepository, "SupplierRepository must not be null");
    }

    @Override
    public List<SupplierResult> execute(ListSuppliersQuery query) {
        List<Supplier> suppliers = query.status() != null ?
                supplierRepository.findByOrganizationIdAndStatus(query.organizationId(), query.status()) :
                supplierRepository.findByOrganizationId(query.organizationId());

        return suppliers.stream()
                .map(this::toResult)
                .toList();
    }

    private SupplierResult toResult(Supplier supplier) {
        return new SupplierResult(
                supplier.getId(),
                supplier.getOrganizationId(),
                supplier.getName(),
                supplier.getEmail() != null ? supplier.getEmail().value() : null,
                supplier.getPhone() != null ? supplier.getPhone().value() : null,
                supplier.getAddress(),
                supplier.getStatus(),
                supplier.getCreatedAt()
        );
    }
}
