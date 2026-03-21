package com.example.validator;

public class AdminValidator implements EmailValidator {
    private final String domain = "@escuelaing.edu.co";

    @Override
    public boolean validate(String email) {
        return email != null && email.toLowerCase().endsWith(domain);
    }

    @Override
    public String getDomain() {
        return domain;
    }
}