package com.atlashub.integrations.application.query.ListAvailableProviders;

import com.atlashub.integrations.application.port.ProviderCatalogPort;
import com.atlashub.integrations.domain.valueobject.ProviderOffering;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Locale;

@Component
public class ListAvailableProvidersHandler extends Query<ListAvailableProvidersQuery, List<ProviderOffering>> {
    private final ProviderCatalogPort catalog;
    public ListAvailableProvidersHandler(ProviderCatalogPort catalog) { this.catalog = catalog; }
    @Override public List<ProviderOffering> execute(ListAvailableProvidersQuery query) {
        return catalog.availableIn(query.merchantCountry().toUpperCase(Locale.ROOT), query.environment());
    }
}
