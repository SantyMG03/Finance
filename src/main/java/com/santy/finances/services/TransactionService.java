package com.santy.finances.services;

import com.santy.finances.DTOs.PortfolioDTO;
import com.santy.finances.models.Transaction;
import com.santy.finances.models.enums.TransactionType;
import com.santy.finances.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;

    /**
     * Saves a new transaction into de DB
     * @param t Transaction to save
     * @return saved entity
     */
    public Transaction registerTransaction (Transaction t) {
        return transactionRepository.save(t);
    }

    /**
     * This method looks for a transaction ID.
     * If found, it updates the transaction with new data.
     * Otherwise, throws an error.
     * @param id ID we are looking for to update
     * @param updatedData New data to overwrite
     * @return saved entity
     */
    public Transaction updateTransaction(Long id, Transaction updatedData) {
        return transactionRepository.findById(id).map(existingTransaction -> {
            // If it exists, then it is updated with the new transaction data
            existingTransaction.setDate(updatedData.getDate());
            existingTransaction.setTicker(updatedData.getTicker());
            existingTransaction.setAsset(updatedData.getAsset());
            existingTransaction.setIsin(updatedData.getIsin());
            existingTransaction.setBroker(updatedData.getBroker());
            existingTransaction.setType(updatedData.getType());
            existingTransaction.setShares(updatedData.getShares());
            existingTransaction.setPrice(updatedData.getPrice());
            existingTransaction.setCommission(updatedData.getCommission());
            existingTransaction.setTotalPrice(updatedData.getTotalPrice());

            return transactionRepository.save(existingTransaction);
        }).orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + id));
    }

    public List<PortfolioDTO> getPortfolioAnalysis() {
        List<Transaction> allTransactions = transactionRepository.findAll();
        Map<String, PortfolioDTO> portfolioMap = new HashMap<>();

        for (Transaction t: allTransactions) {
            PortfolioDTO dto = portfolioMap.computeIfAbsent(t.getTicker(), ticker -> {
                PortfolioDTO newDto = new PortfolioDTO();
                newDto.setTicker(ticker);
                newDto.setAsset(t.getAsset());
                newDto.setTotalShares(BigDecimal.ZERO);
                newDto.setMeanPrice(BigDecimal.ZERO);
                return newDto;
            });

            if (t.getType() == TransactionType.BUY) {
                BigDecimal currentCost = dto.getTotalShares().multiply(dto.getMeanPrice());

                // totalPrice includes price + commission
                BigDecimal newCost = t.getTotalPrice();

                BigDecimal newTotalShares = dto.getTotalShares().add(t.getShares());
                dto.setTotalShares(newTotalShares);

                // Recalculate the weighted average price
                if (newTotalShares.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal newMeanPrice = currentCost.add(newCost)
                            .divide(newTotalShares, 4, RoundingMode.HALF_UP);
                    dto.setMeanPrice(newMeanPrice);
                }
            } else if (t.getType() == TransactionType.SELL) {
                // Sales reduce the number of titles but do not alter the average base price.
                dto.setTotalShares(dto.getTotalShares().subtract(t.getShares()));
            }
        }

        // 2. Calcular valores de mercado, P/L y filtrar posiciones cerradas
        List<PortfolioDTO> activePositions = portfolioMap.values().stream()
                .filter(dto -> dto.getTotalShares().compareTo(BigDecimal.ZERO) > 0)
                .peek(dto -> {
                    // TODO: Connect API Yahoo Finance/Google Finance to get real price.
                    // MOCK: We simulated a 5% increase over the average price in order to test it.
                    BigDecimal currentPriceMock = dto.getMeanPrice().multiply(new BigDecimal("1.05"));
                    dto.setCurrentPrice(currentPriceMock.setScale(2, RoundingMode.HALF_UP));

                    // Market Value = Total Shares * Current Price
                    BigDecimal marketValue = dto.getTotalShares().multiply(dto.getCurrentPrice());
                    dto.setMarketValue(marketValue.setScale(2, RoundingMode.HALF_UP));

                    // Profit/Loss (€) = Market Value - Total Invested
                    BigDecimal totalInvested = dto.getTotalShares().multiply(dto.getMeanPrice());
                    BigDecimal profitLossEuros = marketValue.subtract(totalInvested);
                    dto.setProfitLossEuros(profitLossEuros.setScale(2, RoundingMode.HALF_UP));

                    // Profit/Loss (%) = ((Current Price / Mean Price) - 1) * 100
                    if (dto.getMeanPrice().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal profitLossPercent = dto.getCurrentPrice()
                                .divide(dto.getMeanPrice(), 4, RoundingMode.HALF_UP)
                                .subtract(BigDecimal.ONE)
                                .multiply(new BigDecimal("100"));
                        dto.setProfitLossPercent(profitLossPercent.setScale(2, RoundingMode.HALF_UP));
                    }
                })
                .collect(Collectors.toList());

        // Portfolio Weight %
        BigDecimal totalPortfolioValue = activePositions.stream()
                .map(PortfolioDTO::getMarketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPortfolioValue.compareTo(BigDecimal.ZERO) > 0) {
            activePositions.forEach(dto -> {
                BigDecimal weight = dto.getMarketValue()
                        .divide(totalPortfolioValue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                dto.setPortfolioWeight(weight.setScale(2, RoundingMode.HALF_UP));
            });
        }

        return activePositions;
    }
}
