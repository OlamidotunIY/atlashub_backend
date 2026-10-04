package com.atlashub.compliance.domain.valueobject;

public enum SupportedBusinessIndustry {
    PHYSICAL_GOODS("Commerce_PhysicalGoods"), DIGITAL_SERVICES("Commerce_DigitalServices"),
    PHYSICAL_SERVICES("Commerce_PhysicalServices"), PROFESSIONAL_SERVICES("Commerce_ProfessionalServices"),
    HOTELS("Hospitality_Hotels"), RESTAURANTS("Hospitality_Restaurants"),
    COURIER_SERVICES("Logistics_CourierServices"), FREIGHT_SERVICES("Logistics_FreightServices"),
    RETAIL("Retail"), WHOLESALE("Wholesale");
    private final String anchorCode;
    SupportedBusinessIndustry(String anchorCode) { this.anchorCode = anchorCode; }
    public String anchorCode() { return anchorCode; }
}
