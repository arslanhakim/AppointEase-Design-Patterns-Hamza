package com.appointease.domain.model;

public record Customer(CustomerId id, String name) {
    public Customer {
        Validation.required(id, "customer ID");
        name = Validation.text(name, "customer name");
    }
}
