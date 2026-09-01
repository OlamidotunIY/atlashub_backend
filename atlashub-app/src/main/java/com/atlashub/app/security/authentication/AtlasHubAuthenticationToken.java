package com.atlashub.app.security.authentication;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

public class AtlasHubAuthenticationToken extends AbstractAuthenticationToken {

    private final String OrganizationId;
    private final Object credentials;
    @Getter
    private final AuthType authType;

    public enum AuthType {
        JWT, API_KEY
    }

    public AtlasHubAuthenticationToken(String OrganizationId, Object credentials, AuthType authType) {
        super(Collections.singletonList(new SimpleGrantedAuthority("ROLE_Organization")));
        this.OrganizationId = OrganizationId;
        this.credentials = credentials;
        this.authType = authType;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return OrganizationId;
    }

}
