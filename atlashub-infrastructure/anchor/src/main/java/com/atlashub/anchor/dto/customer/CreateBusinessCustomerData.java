package com.atlashub.anchor.dto.customer;

import java.math.BigDecimal;
import java.util.List;

public record CreateBusinessCustomerData(String type, Attributes attributes) {
    public CreateBusinessCustomerData(Attributes attributes) { this("BusinessCustomer", attributes); }
    public record Attributes(CountryState address, BasicDetail basicDetail, Contact contact, List<Officer> officers) {}
    public record CountryState(String country, String state) {}
    public record BasicDetail(String industry, String registrationType, String country, String businessName,
            String businessBvn, String dateOfRegistration, String description, String website) {}
    public record Contact(Emails email, Addresses address, String phoneNumber) {}
    public record Emails(String general, String support, String dispute) {}
    public record Addresses(Address main, Address registered) {}
    public record Address(String country, String state, String addressLine_1, String addressLine_2,
            String city, String postalCode) {}
    public record FullName(String firstName, String lastName, String middleName, String maidenName) {}
    public record Officer(String role, FullName fullName, String nationality, Address address,
            String dateOfBirth, String email, String phoneNumber, String bvn, String title, BigDecimal percentageOwned) {}
}
