package com.example.validator;

public class GmailValidator implements EmailValidator {
    private final String domain = "@gmail.com";

    @Override
    public boolean validate(String email) {
        return email != null && email.toLowerCase().endsWith(domain);
    }

    @Override
    public String getDomain() {
        return domain;
    }
}
