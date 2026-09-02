package com.atlashub.identity.adapter.out.persistence.query;

import com.atlashub.identity.application.result.SplitRecipientDto;
import com.atlashub.identity.application.port.SplitRecipientQueryService;
import com.atlashub.identity.adapter.out.persistence.repository.SpringDataSplitRecipientRepository;
import com.atlashub.shared.application.util.PageResult;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class SplitRecipientQueryServiceImpl implements SplitRecipientQueryService {

    private final SpringDataSplitRecipientRepository repository;

    public SplitRecipientQueryServiceImpl(SpringDataSplitRecipientRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<SplitRecipientDto> findById(Long OrganizationId, Long SplitRecipientId) {
        return repository.findById(SplitRecipientId)
                .filter(s -> s.getIntegration().equals(OrganizationId))
                .map(s -> new SplitRecipientDto(
                        s.getId(),
                        s.getIntegration(),
                        s.getBankCode(),
                        s.getAccountNumber(),
                        s.getAccountName(),
                        s.getDescription(),
                        s.isActive(),
                        s.getCreatedAt()
                ));
    }

    @Override
    public PageResult<SplitRecipientDto> findAllByOrganizationId(Long OrganizationId, int page, int size) {
        return new PageResult<>(Collections.emptyList(), page, size, 0L, 0);
    }
}
