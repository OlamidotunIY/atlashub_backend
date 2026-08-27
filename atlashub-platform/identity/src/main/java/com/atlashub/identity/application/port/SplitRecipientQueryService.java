package com.atlashub.identity.application.port;

import com.atlashub.identity.application.result.SplitRecipientDto;
import com.atlashub.shared.util.PageResult;

import java.util.Optional;

public interface SplitRecipientQueryService {
    Optional<SplitRecipientDto> findById(Long OrganizationId, Long SplitRecipientId);
    PageResult<SplitRecipientDto> findAllByOrganizationId(Long OrganizationId, int page, int size);
}
