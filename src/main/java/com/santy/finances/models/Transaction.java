package com.santy.finances.models;

import com.santy.finances.models.enums.TransactionType;
import jakarta.persistence.*;
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

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String asset;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false)
    private String broker;

    @Column(length = 15, nullable = false)
    private String ticker;

    @Column(length = 12, nullable = false)
    private String isin;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal shares;

    // Cost of the operation
    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal price;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal commission;

    // Total price of the operation (price + commission)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;
}
