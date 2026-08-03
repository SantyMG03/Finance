package com.santy.finances.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinnhubQuoteDTO {

    // c represents current price in Finnhub
    @JsonProperty("c")
    private BigDecimal currentPrice;

    // Other values will be ignored
}
