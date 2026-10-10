package com.atlashub.paystack.infrastructure.external.paystack.configuration;

import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackBankClient;
import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackClientRegistry;
import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackClients;
import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackSettlementClient;
import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackSubaccountClient;
import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackTransactionClient;
import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackRefundClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PaystackProperties.class)
@ConditionalOnProperty(prefix = "atlashub.integrations.paystack", name = "enabled", havingValue = "true")
public class PaystackClientConfiguration {
    @Bean
    PaystackClientRegistry paystackClientRegistry(RestClient.Builder builder, PaystackProperties properties) {
        return new PaystackClientRegistry(
                Map.of(PaystackEnvironment.TEST, clients(builder, properties.test()), PaystackEnvironment.LIVE,
                        clients(builder, properties.live())));
    }

    private PaystackClients clients(RestClient.Builder builder, PaystackProperties.EnvironmentProperties properties) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(properties.connectTimeout()).build());
        factory.setReadTimeout(properties.readTimeout());
        RestClient client = builder.clone().baseUrl(properties.baseUrl().toString())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.secretKey())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).requestFactory(factory).build();
        HttpServiceProxyFactory proxy = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client)).build();
        return new PaystackClients(proxy.createClient(PaystackBankClient.class),
                proxy.createClient(PaystackSubaccountClient.class), proxy.createClient(PaystackTransactionClient.class),
                proxy.createClient(PaystackSettlementClient.class), proxy.createClient(PaystackRefundClient.class));
    }
}
