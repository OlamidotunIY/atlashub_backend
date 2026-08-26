package com.atlashub.identity.adapter.in.web.response;

import com.atlashub.identity.application.dto.ApiKeyDto;
import java.util.List;

public record ListApiKeysResponseDto(List<ApiKeyDto> keys) {}
