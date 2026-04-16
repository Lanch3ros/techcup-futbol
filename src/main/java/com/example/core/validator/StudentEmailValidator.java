package com.example.core.validator;

public class StudentEmailValidator implements EmailValidator {
    private final String domain = "@mail.escuelaing.edu.co";

    @Override
    public boolean validate(String email) {
        return email != null && email.toLowerCase().endsWith(domain);
    }

    @Override
    public String getDomain() {
        return domain;
    }
}