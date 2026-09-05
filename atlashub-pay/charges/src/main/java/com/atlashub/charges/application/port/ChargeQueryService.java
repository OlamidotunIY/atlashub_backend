package com.atlashub.charges.application.port;

import com.atlashub.charges.application.result.ChargeResult;
import com.atlashub.charges.application.result.ChargeHistoryResult;
import com.atlashub.shared.application.util.PageResult;
import java.util.Optional;

public interface ChargeQueryService {
    Optional<ChargeResult> getChargeByReference(String reference);
    PageResult<ChargeHistoryResult> getChargeHistory(Long organizationId, int page, int size);
}
