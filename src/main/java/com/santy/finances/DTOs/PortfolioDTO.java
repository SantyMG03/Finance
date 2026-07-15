package com.santy.finances.DTOs;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PortfolioDTO {
    // Identifiers
    private String ticker;
    private String asset;

    // Calculated adding transaction data
    private BigDecimal totalShares;
    private BigDecimal meanPrice;

    // Check internet (Google Finance/Yahoo Finance)
    private BigDecimal currentPrice;

    // Calculated using the data above
    private BigDecimal marketValue;
    private BigDecimal profitLossEuros;
    private BigDecimal profitLossPercent;
    private BigDecimal portfolioWeight;
}
