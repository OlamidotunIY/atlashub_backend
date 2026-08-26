package com.atlashub.identity.adapter.out.persistence.query;

import com.atlashub.identity.application.dto.ApiKeyDto;
import com.atlashub.identity.application.port.ApiKeyQueryService;
import com.atlashub.identity.adapter.out.persistence.repository.SpringDataApiKeyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApiKeyQueryServiceImpl implements ApiKeyQueryService {

    private final SpringDataApiKeyRepository repository;

    public ApiKeyQueryServiceImpl(SpringDataApiKeyRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ApiKeyDto> findAllByIntegration(Long merchantId) {
        return repository.findAllByIntegration(merchantId)
                .stream()
                .map(k -> new ApiKeyDto(
                        k.getId(),
                        k.getKeyType().name(),
                        k.getEnvironment().name(),
                        k.getDisplayValue(),
                        k.isActive(),
                        k.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
