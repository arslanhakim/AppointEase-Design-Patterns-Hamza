package com.appointease.domain.model;

public record CustomerId(String value) {
    public CustomerId { value = Validation.text(value, "customer ID"); }
}
