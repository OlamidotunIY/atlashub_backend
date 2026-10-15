package com.atlashub.accounting.assets.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class AssetNotFoundException extends NotFoundException {

    public AssetNotFoundException(Long id) {
        super("Asset not found: " + id);
    }

    public AssetNotFoundException(String tagNumber) {
        super("Asset not found with tag: " + tagNumber);
    }
}
