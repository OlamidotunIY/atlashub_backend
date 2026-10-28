package com.atlashub.integrations.infrastructure.configuration;

import com.atlashub.integrations.domain.valueobject.Capability;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.integrations.domain.valueobject.ProviderOffering;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reviewed provider matrix. File edits require a documentation and capability review.
 */
@Component
public class CsvProviderCatalogAdapter implements com.atlashub.integrations.application.port.ProviderCatalogPort {
    private final List<ProviderOffering> offerings;

    public CsvProviderCatalogAdapter() {
        try (var stream = CsvProviderCatalogAdapter.class.getResourceAsStream("/provider-offerings.csv")) {
            if (stream == null) throw new IllegalStateException("Provider matrix is missing");
            try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                offerings = reader.lines().filter(line -> !line.isBlank() && !line.startsWith("#")).map(this::parse)
                        .toList();
            }
        } catch (IOException error) {
            throw new IllegalStateException("Provider matrix is unreadable", error);
        }
    }

    private ProviderOffering parse(String line) {
        String[] fields = line.split(",", -1);
        if (fields.length != 6) throw new IllegalStateException("Invalid provider matrix row: " + line);
        Set<Capability> capabilities =
                Arrays.stream(fields[4].split("\\|")).map(Capability::valueOf).collect(Collectors.toUnmodifiableSet());
        new Country(fields[1]);
        CurrencyCode.valueOf(fields[3]);
        return new ProviderOffering(Provider.valueOf(fields[0]), fields[1], ApiEnvironment.valueOf(fields[2]),
                Set.of(fields[3]), capabilities, fields[5]);
    }

    public List<ProviderOffering> availableIn(String merchantCountry, ApiEnvironment environment) {
        return offerings.stream()
                .filter(o -> o.merchantCountry().equals(merchantCountry) && o.environment() == environment).toList();
    }

    public boolean supports(Provider provider, String country, ApiEnvironment environment, String currency,
                            Capability capability) {
        return offerings.stream().anyMatch(o -> o.provider() == provider && o.merchantCountry().equals(country) &&
                o.environment() == environment && o.offers(capability, currency));
    }
}
