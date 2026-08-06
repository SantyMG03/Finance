package com.santy.finances.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "bank_accounts", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "user_id"}))
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "The account name cannot be empty")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "The initial balance cannot be null")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal initialBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
}
