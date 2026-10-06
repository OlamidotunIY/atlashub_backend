package com.atlashub.anchor.dto.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BusinessCustomerResource(String id, String type, Attributes attributes) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attributes(Detail detail, Verification verification, List<Officer> officers, String status) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Detail(String businessName) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Verification(String status) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Officer(String officerId, String role, FullName fullName) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FullName(String firstName, String lastName, String middleName) {}
}
