package com.atlashub.accounts.application.command.RegisterUser;

import com.atlashub.shared.domain.valueobject.Country;

public record RegisterUserCommand(String firstName, String lastName, String email,
                                  String password, Country country) {}
