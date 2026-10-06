package com.atlashub.pay.accounts.domain.valueobject;

import com.atlashub.pay.accounts.domain.exceptions.InvalidBankingAccountDataException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfirmedBankingDetailsTest {
    @Test
    void permits_a_subaccount_without_virtual_nuban_details() {
        ConfirmedBankingDetails details = new ConfirmedBankingDetails(null, null, null, null, null);
        assertFalse(details.hasAccountNumberDetails());
    }

    @Test
    void rejects_partial_account_number_details() {
        assertThrows(InvalidBankingAccountDataException.class,
                () -> new ConfirmedBankingDetails("Tolu Store", null, null, null, null));
    }
}
