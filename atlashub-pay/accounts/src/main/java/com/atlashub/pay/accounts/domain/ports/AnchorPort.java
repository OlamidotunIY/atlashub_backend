package com.atlashub.pay.accounts.domain.ports;

import com.atlashub.shared.domain.valueobject.Anchor;

public interface AnchorPort {
    /**
     * Calls Anchor API to create a virtual account linked to AtlasHub's pool account.
     * Returns Anchor's internal account reference ID immediately.
     * The NUBAN itself arrives asynchronously via Anchor's webhook.
     */
    Anchor.ReserveAccountResponse issueVirtualAccount(String anchorCustomerId, String anchorAccountType);

    /**
     * Issues a dedicated customer virtual account linked to the organization's pool.
     */
    String issueCustomerVirtualAccount(String customerName, String email, String organizationId, String customerId);
}
