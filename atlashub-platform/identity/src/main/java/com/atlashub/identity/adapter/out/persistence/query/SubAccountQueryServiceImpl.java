package com.atlashub.identity.adapter.out.persistence.query;

import com.atlashub.identity.application.dto.SubAccountDto;
import com.atlashub.identity.application.port.SubAccountQueryService;
import com.atlashub.identity.adapter.out.persistence.repository.SpringDataSubAccountRepository;
import com.atlashub.shared.util.PageResult;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class SubAccountQueryServiceImpl implements SubAccountQueryService {

    private final SpringDataSubAccountRepository repository;

    public SubAccountQueryServiceImpl(SpringDataSubAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<SubAccountDto> findById(Long merchantId, Long subAccountId) {
        return repository.findById(subAccountId)
                .filter(s -> s.getIntegration().equals(merchantId))
                .map(s -> new SubAccountDto(
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
    public PageResult<SubAccountDto> findAllByMerchantId(Long merchantId, int page, int size) {
        return new PageResult<>(Collections.emptyList(), page, size, 0L, 0);
    }
}
