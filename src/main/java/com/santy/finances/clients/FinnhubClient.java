package com.santy.finances.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class FinnhubClient {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl = "https://finnhub.io/api/v1/quote";

    public FinnhubClient(RestTemplate restTemplate, @Value("${finnhub.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    /**
     * Connects to Finnhub API to get the real-time price of a ticker.
     *
     * @param ticker The stock symbol (e.g., AAPL).
     * @return The current market price.
     */
    public BigDecimal getCurrentPrice(String ticker) {
        try {
            String url = baseUrl + "?symbol=" + ticker + "&token=" + apiKey;

            // GET request
            FinnhubQuoteDTO response = restTemplate.getForObject(url, FinnhubQuoteDTO.class);

            if (response != null && response.getCurrentPrice() != null) {
                // If the price is 0, then it does not exist.
                return response.getCurrentPrice();
            }
        } catch (Exception e) {
            System.err.println("Error fetching price for " + ticker + ": " + e.getMessage());
        }

        // If anything goes wrong, return 0
        return BigDecimal.ZERO;
    }
}
