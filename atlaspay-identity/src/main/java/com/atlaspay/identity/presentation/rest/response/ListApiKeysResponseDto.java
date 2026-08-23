package com.atlaspay.identity.presentation.rest.response;

import com.atlaspay.identity.application.dto.ApiKeyDto;
import java.util.List;

public record ListApiKeysResponseDto(List<ApiKeyDto> keys) {}
