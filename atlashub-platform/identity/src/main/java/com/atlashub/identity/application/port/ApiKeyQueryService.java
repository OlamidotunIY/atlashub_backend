package com.atlashub.identity.application.port;

import com.atlashub.identity.application.dto.ApiKeyDto;

import java.util.List;

public interface ApiKeyQueryService {
    List<ApiKeyDto> findAllByIntegration(Long merchantId);
}
