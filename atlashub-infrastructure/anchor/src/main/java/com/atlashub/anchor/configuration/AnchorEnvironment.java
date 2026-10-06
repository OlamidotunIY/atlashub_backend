package com.atlashub.anchor.configuration;

/**
 * The Anchor environment selected by an AtlasHub banking resource.
 *
 * <p>This is deliberately an infrastructure type. Module domain models map their own environment
 * value to it at the Anchor adapter boundary.</p>
 */
public enum AnchorEnvironment {
    SANDBOX,
    LIVE;

    public static AnchorEnvironment fromCallbackPath(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Anchor environment is required");
        }
        return switch (value.trim().toLowerCase()) {
            case "sandbox" -> SANDBOX;
            case "live" -> LIVE;
            default -> throw new IllegalArgumentException("Unsupported Anchor environment");
        };
    }
}
