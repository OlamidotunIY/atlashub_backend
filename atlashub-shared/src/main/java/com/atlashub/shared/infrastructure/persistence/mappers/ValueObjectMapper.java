package com.atlashub.shared.infrastructure.persistence.mappers;

import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.NUBAN;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.springframework.stereotype.Component;

@Component
public class ValueObjectMapper {

    // ==========================================
    // EmailAddress
    // ==========================================
    public EmailAddress toEmail(String value) {
        return value != null && !value.isBlank() ? new EmailAddress(value) : null;
    }
    public String fromEmail(EmailAddress email) {
        return email != null ? email.value() : null;
    }

    // ==========================================
    // PhoneNumber
    // ==========================================
    public PhoneNumber toPhone(String value) {
        return value != null && !value.isBlank() ? new PhoneNumber(value) : null;
    }
    public String fromPhone(PhoneNumber phone) {
        return phone != null ? phone.value() : null;
    }

    // ==========================================
    // Country
    // ==========================================
    public Country toCountry(String code) {
        return code != null && !code.isBlank() ? new Country(code) : null;
    }
    public String fromCountry(Country country) {
        return country != null ? country.code() : null;
    }

    // ==========================================
    // NUBAN
    // ==========================================
    public NUBAN toNuban(String value) {
        return value != null && !value.isBlank() ? new NUBAN(value) : null;
    }
    public String fromNuban(NUBAN nuban) {
        return nuban != null ? nuban.value() : null;
    }

    // ==========================================
    // CurrencyCode (Enum)
    // ==========================================
    // MapStruct handles Enums natively, but having it here guarantees consistency
    public CurrencyCode toCurrencyCode(String code) {
        return code != null && !code.isBlank() ? CurrencyCode.valueOf(code) : null;
    }
    public String fromCurrencyCode(CurrencyCode currency) {
        return currency != null ? currency.name() : null;
    }
}
