package com.santy.finances.services;

import com.santy.finances.DTOs.PortfolioDTO;
import com.santy.finances.clients.FinnhubClient;
import com.santy.finances.exceptions.ResourceNotFoundException;
import com.santy.finances.models.Transaction;
import com.santy.finances.models.enums.TransactionType;
import com.santy.finances.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final FinnhubClient finnhubClient;

    /**
     * Retrieves all transactions from the database.
     *
     * @return A list containing all stored transactions.
     */
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * Saves a new transaction into the database.
     *
     * @param t The transaction entity to save.
     * @return The saved transaction entity.
     */
    @Transactional
    public Transaction registerTransaction(Transaction t) {
        return transactionRepository.save(t);
    }

    /**
     * Searches for a transaction by its ID and updates it with new data.
     *
     * @param id The ID of the transaction to update.
     * @param updatedData The new transaction data to overwrite the existing one.
     * @return The updated and saved transaction entity.
     * @throws ResourceNotFoundException if the transaction ID is not found.
     */
    @Transactional
    public Transaction updateTransaction(Long id, Transaction updatedData) {
        return transactionRepository.findById(id).map(existingTransaction -> {
            // If it exists, update it with the new transaction data
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
        }).orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));
    }

    /**
     * Deletes a transaction from the database by its ID.
     *
     * @param id The ID of the transaction to be removed.
     * @throws ResourceNotFoundException if the transaction ID is not found.
     */
    @Transactional
    public void deleteTransaction(Long id) {
        if(!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction not found with ID: " + id);
        }
        transactionRepository.deleteById(id);
    }

    /**
     * Generates a complete analysis of the portfolio based on all active transactions.
     * Obtains real-time market data from Finnhub API.
     *
     * @return A list of PortfolioDTO representing the calculated portfolio.
     */
    @Transactional(readOnly = true)
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
                BigDecimal newCost = t.getTotalPrice();
                BigDecimal newTotalShares = dto.getTotalShares().add(t.getShares());
                dto.setTotalShares(newTotalShares);

                if (newTotalShares.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal newMeanPrice = currentCost.add(newCost)
                            .divide(newTotalShares, 4, RoundingMode.HALF_UP);
                    dto.setMeanPrice(newMeanPrice);
                }
            } else if (t.getType() == TransactionType.SELL) {
                dto.setTotalShares(dto.getTotalShares().subtract(t.getShares()));
            }
        }

        List<PortfolioDTO> activePositions = portfolioMap.values().stream()
                .filter(dto -> dto.getTotalShares().compareTo(BigDecimal.ZERO) > 0)
                .peek(dto -> {
                    // Request to external API to get current price
                    BigDecimal realPrice = finnhubClient.getCurrentPrice(dto.getTicker());

                    // If anything goes wrong, it uses mean price so as not to disrupt the calculations.
                    if (realPrice == null || realPrice.compareTo(BigDecimal.ZERO) == 0) {
                        realPrice = dto.getMeanPrice();
                    }

                    dto.setCurrentPrice(realPrice.setScale(2, RoundingMode.HALF_UP));

                    BigDecimal marketValue = dto.getTotalShares().multiply(dto.getCurrentPrice());
                    dto.setMarketValue(marketValue.setScale(2, RoundingMode.HALF_UP));

                    BigDecimal totalInvested = dto.getTotalShares().multiply(dto.getMeanPrice());
                    BigDecimal profitLossEuros = marketValue.subtract(totalInvested);
                    dto.setProfitLossEuros(profitLossEuros.setScale(2, RoundingMode.HALF_UP));

                    if (dto.getMeanPrice().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal profitLossPercent = dto.getCurrentPrice()
                                .divide(dto.getMeanPrice(), 4, RoundingMode.HALF_UP)
                                .subtract(BigDecimal.ONE)
                                .multiply(new BigDecimal("100"));
                        dto.setProfitLossPercent(profitLossPercent.setScale(2, RoundingMode.HALF_UP));
                    }
                })
                .collect(Collectors.toList());

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