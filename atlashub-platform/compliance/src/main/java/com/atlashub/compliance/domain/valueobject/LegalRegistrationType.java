package com.atlashub.compliance.domain.valueobject;

public enum LegalRegistrationType {
    SOLE_PROPRIETORSHIP("Business_Name"),
    PRIVATE_LIMITED_COMPANY("Private_Incorporated");
    private final String anchorCode;
    LegalRegistrationType(String anchorCode) { this.anchorCode = anchorCode; }
    public String anchorCode() { return anchorCode; }
}
