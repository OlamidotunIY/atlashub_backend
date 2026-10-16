package com.atlashub.accounting.assets.domain.exceptions;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public class AssetAlreadyFullyDepreciatedException extends BusinessRuleException {

    public AssetAlreadyFullyDepreciatedException() {
        super("Asset is fully depreciated — no further depreciation can be posted");
    }
}
