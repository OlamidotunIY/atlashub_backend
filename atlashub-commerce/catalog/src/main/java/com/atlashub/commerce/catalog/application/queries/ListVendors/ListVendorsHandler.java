package com.atlashub.commerce.catalog.application.queries.ListVendors;

import com.atlashub.commerce.catalog.domain.entities.Vendor;
import com.atlashub.commerce.catalog.domain.repositories.VendorRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ListVendorsHandler extends Query<ListVendorsQuery, List<VendorResult>> {

    private final VendorRepository vendorRepository;

    public ListVendorsHandler(VendorRepository vendorRepository) {
        this.vendorRepository = Objects.requireNonNull(vendorRepository, "VendorRepository must not be null");
    }

    @Override
    public List<VendorResult> execute(ListVendorsQuery query) {
        List<Vendor> vendors = query.status() != null ?
                vendorRepository.findByOrganizationIdAndStatus(query.organizationId(), query.status()) :
                vendorRepository.findByOrganizationId(query.organizationId());

        return vendors.stream()
                .map(this::toResult)
                .toList();
    }

    private VendorResult toResult(Vendor vendor) {
        return new VendorResult(
                vendor.getId(),
                vendor.getOrganizationId(),
                vendor.getUserId(),
                vendor.getBusinessName(),
                vendor.getEmail() != null ? vendor.getEmail().value() : null,
                vendor.getPhone() != null ? vendor.getPhone().value() : null,
                vendor.getSettlementBankCode(),
                vendor.getSettlementAccountNumber(),
                vendor.getSettlementAccountName(),
                vendor.getCommissionRate(),
                vendor.getDisbursementSchedule(),
                vendor.getStatus(),
                vendor.getCreatedAt()
        );
    }
}
