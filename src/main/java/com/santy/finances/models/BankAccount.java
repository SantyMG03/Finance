package com.santy.finances.models;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal initialBalance;
}
