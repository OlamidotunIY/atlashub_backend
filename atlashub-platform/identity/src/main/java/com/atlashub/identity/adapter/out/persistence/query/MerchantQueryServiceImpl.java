package com.atlashub.identity.adapter.out.persistence.query;

import com.atlashub.identity.application.dto.MerchantProfileDto;
import com.atlashub.identity.application.port.MerchantQueryService;
import com.atlashub.identity.adapter.out.persistence.repository.SpringDataMerchantRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MerchantQueryServiceImpl implements MerchantQueryService {

    private final SpringDataMerchantRepository repository;

    public MerchantQueryServiceImpl(SpringDataMerchantRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<MerchantProfileDto> findProfileById(Long merchantId) {
        return repository.findById(merchantId)
                .map(entity -> new MerchantProfileDto(
                        entity.getId(),
                        entity.getBusinessName(),
                        entity.getEmail(),
                        entity.getPhone(),
                        entity.getComplianceStatus().name(),
                        entity.getCreatedAt()
                ));
    }
}
