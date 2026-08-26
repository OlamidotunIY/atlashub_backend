package com.atlashub.identity.presentation.rest.response;

import com.atlashub.identity.application.dto.ApiKeyDto;
import java.util.List;

public record ListApiKeysResponseDto(List<ApiKeyDto> keys) {}
