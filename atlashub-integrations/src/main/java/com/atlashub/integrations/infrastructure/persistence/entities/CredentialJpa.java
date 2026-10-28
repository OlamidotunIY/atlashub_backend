package com.atlashub.integrations.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "integration_credentials")
public class CredentialJpa {
    @Id
    public String reference;
    @Column(nullable = false, length = 24)
    public String nonce;
    @Column(nullable = false, length = 8192)
    public String ciphertext;
}
