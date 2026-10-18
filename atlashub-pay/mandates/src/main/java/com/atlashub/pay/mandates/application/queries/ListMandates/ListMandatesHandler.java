package com.atlashub.pay.mandates.application.queries.ListMandates;

import com.atlashub.pay.mandates.application.queries.GetMandate.GetMandateHandler;
import com.atlashub.pay.mandates.application.queries.GetMandate.MandateResult;
import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.repositories.PaymentMandateRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListMandatesHandler extends Query<ListMandatesQuery, List<MandateResult>> {

    private final PaymentMandateRepository mandateRepository;

    public ListMandatesHandler(PaymentMandateRepository mandateRepository) {
        this.mandateRepository = mandateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MandateResult> execute(ListMandatesQuery query) {
        List<PaymentMandate> mandates;

        if (query.customerId() != null && query.status() != null) {
            mandates = mandateRepository.findByOrganizationIdAndCustomerIdAndStatus(
                    query.organizationId(), query.customerId(), query.status()
            );
        } else if (query.customerId() != null) {
            mandates = mandateRepository.findByOrganizationIdAndCustomerId(
                    query.organizationId(), query.customerId()
            );
        } else if (query.status() != null) {
            mandates = mandateRepository.findByOrganizationIdAndStatus(
                    query.organizationId(), query.status()
            );
        } else {
            mandates = mandateRepository.findByOrganizationId(query.organizationId());
        }

        return mandates.stream()
                .map(GetMandateHandler::toResult)
                .collect(Collectors.toList());
    }
}
