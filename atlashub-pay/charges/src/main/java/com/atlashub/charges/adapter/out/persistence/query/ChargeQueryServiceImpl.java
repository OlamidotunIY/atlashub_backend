package com.atlashub.charges.adapter.out.persistence.query;

import com.atlashub.charges.adapter.out.persistence.repository.SpringDataExternalChargeRepository;
import com.atlashub.charges.adapter.out.persistence.entity.ExternalChargeJpaEntity;
import com.atlashub.charges.application.port.ChargeQueryService;
import com.atlashub.charges.application.result.ChargeResult;
import com.atlashub.charges.application.result.ChargeHistoryResult;
import com.atlashub.shared.application.util.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChargeQueryServiceImpl implements ChargeQueryService {

    private final SpringDataExternalChargeRepository repository;

    @Override
    public Optional<ChargeResult> getChargeByReference(String reference) {
        return repository.findByReference(reference)
                .map(charge -> new ChargeResult(
                        charge.getId(),
                        charge.getOrganizationId(),
                        charge.getReference(),
                        charge.getAmount(),
                        charge.getCurrency().name(),
                        charge.getStatus().name(),
                        charge.getProvider().name(),
                        charge.getCheckoutUrl(),
                        charge.getCompletedAt()
                ));
    }

    @Override
    public PageResult<ChargeHistoryResult> getChargeHistory(Long organizationId, int page, int size) {
        Page<ExternalChargeJpaEntity> p = repository.findByOrganizationId(
            organizationId, 
            PageRequest.of(page, size)
        );

        return new PageResult<>(
            p.getContent().stream()
                .map(charge -> new ChargeHistoryResult(
                    charge.getReference(),
                    charge.getAmount(),
                    charge.getCurrency().name(),
                    charge.getStatus().name(),
                    charge.getCompletedAt()
                ))
                .collect(Collectors.toList()),
            p.getTotalElements(),
            p.getTotalPages()
        );
    }
}
