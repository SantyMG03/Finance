package com.santy.finances.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "bank_accounts")
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "The account name cannot be empty")
    @Column(nullable = false, unique = true)
    private String name;

    @NotNull(message = "The initial balance cannot be null")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal initialBalance;
}
