package com.atlaspay.admin.application.port;

public interface CloudflareEmailPort {
    void createEmailRoutingRule(String aliasEmail, String destinationEmail);
}
