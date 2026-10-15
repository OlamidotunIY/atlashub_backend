package com.atlashub.accounting.assets.domain.exceptions;

import com.atlashub.shared.domain.exception.ConflictException;

public class AssetAlreadyDisposedException extends ConflictException {

    public AssetAlreadyDisposedException(Long id) {
        super("Asset " + id + " has already been disposed");
    }
}
