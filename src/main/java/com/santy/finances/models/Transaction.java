package com.santy.finances.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.santy.finances.models.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "Transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "The transaction date cannot be null")
    @Column(nullable = false)
    private LocalDate date;

    @NotBlank(message = "The transaction asset cannot be null")
    @Column(nullable = false)
    private String asset;

    @NotNull(message = "The transaction type cannot be null")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @NotBlank(message = "The transaction broker cannot be null")
    @Column(nullable = false)
    private String broker;

    @NotBlank(message = "The transaction ticker cannot be null")
    @Column(length = 15, nullable = false)
    private String ticker;

    @NotBlank(message = "The transaction ISIN cannot be null")
    @Column(length = 12, nullable = false)
    private String isin;

    @NotNull(message = "The transaction shares cannot be null")
    @Positive
    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal shares;

    // Cost of the operation
    @NotNull(message = "The transaction price cannot be null")
    @Positive
    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal price;

    @NotNull(message = "The transaction commission cannot be null")
    @PositiveOrZero
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal commission;

    // Total price of the operation (price + commission)
    @NotNull(message = "The transaction total price cannot be null")
    @Positive
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
}
