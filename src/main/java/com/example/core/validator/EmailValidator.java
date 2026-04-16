package com.example.core.validator;

public interface EmailValidator {
    boolean validate(String email);
    String getDomain();
}