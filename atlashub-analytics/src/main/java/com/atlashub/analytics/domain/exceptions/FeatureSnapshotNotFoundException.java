package com.atlashub.analytics.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class FeatureSnapshotNotFoundException extends NotFoundException {
    public FeatureSnapshotNotFoundException(Long snapshotId) {
        super("Feature snapshot not found: " + snapshotId);
    }
}
