package com.atlashub.anchor.infrastructure.external.anchor.configuration;

import com.atlashub.anchor.infrastructure.external.anchor.client.AnchorClientRegistry;
import com.atlashub.anchor.infrastructure.external.anchor.client.AnchorClients;
import com.atlashub.anchor.infrastructure.external.anchor.client.AnchorDepositAccountClient;
import com.atlashub.anchor.infrastructure.external.anchor.client.AnchorReservedAccountClient;
import com.atlashub.anchor.infrastructure.external.anchor.client.AnchorSubAccountClient;
import com.atlashub.anchor.infrastructure.external.anchor.client.AnchorWebhookClient;
import com.atlashub.anchor.infrastructure.external.anchor.client.AnchorBusinessCustomerClient;
import com.atlashub.anchor.infrastructure.external.anchor.client.AnchorDocumentClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.http.HttpClient;
import java.util.Map;

/**
 * Creates isolated, environment-specific declarative Anchor clients. Provider credentials never
 * leave this configuration or the underlying HTTP client.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AnchorProperties.class)
@ConditionalOnProperty(prefix = "atlashub.integrations.anchor", name = "enabled", havingValue = "true")
public class AnchorClientConfiguration {

    @Bean
    AnchorClientRegistry anchorClientRegistry(RestClient.Builder restClientBuilder, AnchorProperties properties) {
        return new AnchorClientRegistry(Map.of(
                AnchorEnvironment.SANDBOX, createClients(restClientBuilder, properties.sandbox()),
                AnchorEnvironment.LIVE, createClients(restClientBuilder, properties.live())
        ));
    }

    private AnchorClients createClients(
            RestClient.Builder restClientBuilder,
            AnchorProperties.EnvironmentProperties properties
    ) {
        RestClient restClient = restClientBuilder.clone()
                .baseUrl(removeTrailingSlash(properties.baseUrl().toString()))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-anchor-key", properties.apiKey())
                .requestFactory(requestFactory(properties))
                .build();

        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return new AnchorClients(
                proxyFactory.createClient(AnchorDepositAccountClient.class),
                proxyFactory.createClient(AnchorSubAccountClient.class),
                proxyFactory.createClient(AnchorReservedAccountClient.class),
                proxyFactory.createClient(AnchorBusinessCustomerClient.class),
                proxyFactory.createClient(AnchorDocumentClient.class),
                proxyFactory.createClient(AnchorWebhookClient.class)
        );
    }

    private JdkClientHttpRequestFactory requestFactory(AnchorProperties.EnvironmentProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());
        return requestFactory;
    }

    private String removeTrailingSlash(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
