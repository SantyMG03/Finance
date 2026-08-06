package com.santy.finances.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.santy.finances.models.enums.DiaryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "diary_entries") // Estandarizado a minúsculas y plural
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "The diary date cannot be null")
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @NotNull(message = "The diary bank account cannot be null")
    @ManyToOne
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @NotBlank(message = "The diary concept cannot be null")
    @Column(name = "concept", nullable = false)
    private String concept;

    @NotNull(message = "The diary category cannot be null")
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotNull(message = "The diary type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DiaryType diaryType;

    @NotNull(message = "The diary amount cannot be null")
    @Positive
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "extra_info", length = 500)
    private String info;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
}