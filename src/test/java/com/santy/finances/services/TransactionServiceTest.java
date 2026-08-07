package com.santy.finances.services;

import com.santy.finances.DTOs.PortfolioDTO;
import com.santy.finances.clients.FinnhubClient;
import com.santy.finances.exceptions.ResourceNotFoundException;
import com.santy.finances.models.Transaction;
import com.santy.finances.models.User;
import com.santy.finances.models.enums.TransactionType;
import com.santy.finances.repositories.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FinnhubClient finnhubClient;

    @InjectMocks
    private TransactionService transactionService;

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("santy");
        return user;
    }

    private Transaction buildTransaction(Long id, String ticker, String asset, TransactionType type,
                                         BigDecimal shares, BigDecimal totalPrice) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setDate(LocalDate.of(2026, 1, 1));
        transaction.setTicker(ticker);
        transaction.setAsset(asset);
        transaction.setType(type);
        transaction.setBroker("Broker");
        transaction.setIsin("ISIN00000001");
        transaction.setShares(shares);
        transaction.setPrice(totalPrice.divide(shares, 4, RoundingMode.HALF_UP));
        transaction.setCommission(BigDecimal.ZERO);
        transaction.setTotalPrice(totalPrice);
        return transaction;
    }

    @Test
    void getAllTransactions_returnsAll() {
        List<Transaction> transactions = List.of(
                buildTransaction(1L, "AAPL", "Apple", TransactionType.BUY, new BigDecimal("10"), new BigDecimal("150.00")));
        when(transactionRepository.findAll()).thenReturn(transactions);

        List<Transaction> result = transactionService.getAllTransactions();

        assertThat(result).isEqualTo(transactions);
    }

    @Test
    void getUserTransactions_returnsTransactionsOfUser() {
        User user = buildUser();
        List<Transaction> transactions = List.of(
                buildTransaction(1L, "AAPL", "Apple", TransactionType.BUY, new BigDecimal("10"), new BigDecimal("150.00")));
        when(transactionRepository.findByUser(user)).thenReturn(transactions);

        List<Transaction> result = transactionService.getUserTransactions(user);

        assertThat(result).isEqualTo(transactions);
        verify(transactionRepository).findByUser(user);
    }

    @Test
    void registerTransaction_savesAndReturns() {
        Transaction transaction = buildTransaction(null, "AAPL", "Apple", TransactionType.BUY,
                new BigDecimal("10"), new BigDecimal("150.00"));
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        Transaction result = transactionService.registerTransaction(transaction);

        assertThat(result).isSameAs(transaction);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void updateTransaction_success_updatesAllFields() {
        Transaction existing = buildTransaction(1L, "AAPL", "Apple", TransactionType.BUY,
                new BigDecimal("10"), new BigDecimal("150.00"));
        Transaction updatedData = buildTransaction(1L, "MSFT", "Microsoft", TransactionType.SELL,
                new BigDecimal("5"), new BigDecimal("100.00"));
        updatedData.setBroker("NewBroker");
        updatedData.setIsin("ISIN99999999");
        updatedData.setCommission(new BigDecimal("1.50"));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(existing)).thenReturn(existing);

        Transaction result = transactionService.updateTransaction(1L, updatedData);

        assertThat(result).isSameAs(existing);
        assertThat(existing.getTicker()).isEqualTo("MSFT");
        assertThat(existing.getAsset()).isEqualTo("Microsoft");
        assertThat(existing.getType()).isEqualTo(TransactionType.SELL);
        assertThat(existing.getBroker()).isEqualTo("NewBroker");
        assertThat(existing.getIsin()).isEqualTo("ISIN99999999");
        assertThat(existing.getShares()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(existing.getCommission()).isEqualByComparingTo(new BigDecimal("1.50"));
        assertThat(existing.getTotalPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        verify(transactionRepository).save(existing);
    }

    @Test
    void updateTransaction_throwsWhenNotFound() {
        Transaction updatedData = buildTransaction(1L, "MSFT", "Microsoft", TransactionType.BUY,
                new BigDecimal("5"), new BigDecimal("100.00"));
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.updateTransaction(99L, updatedData))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Transaction not found with ID: 99");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void deleteTransaction_success_deletesById() {
        when(transactionRepository.existsById(1L)).thenReturn(true);

        transactionService.deleteTransaction(1L);

        verify(transactionRepository).deleteById(1L);
    }

    @Test
    void deleteTransaction_throwsWhenNotFound() {
        when(transactionRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.deleteTransaction(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Transaction not found with ID: 99");
        verify(transactionRepository, never()).deleteById(99L);
    }

    @Test
    void getPortfolioAnalysis_scopesTransactionsByUser() {
        User user = buildUser();
        when(transactionRepository.findByUser(user)).thenReturn(List.of());

        List<PortfolioDTO> result = transactionService.getPortfolioAnalysis(user);

        assertThat(result).isEmpty();
        verify(transactionRepository).findByUser(user);
        verify(transactionRepository, never()).findAll();
    }

    @Test
    void getPortfolioAnalysis_aggregatesBuysAndSellsAndCalculatesMetrics() {
        User user = buildUser();
        List<Transaction> transactions = List.of(
                buildTransaction(1L, "AAPL", "Apple", TransactionType.BUY,
                        new BigDecimal("10"), new BigDecimal("150.00")),
                buildTransaction(2L, "AAPL", "Apple", TransactionType.BUY,
                        new BigDecimal("10"), new BigDecimal("250.00")),
                buildTransaction(3L, "AAPL", "Apple", TransactionType.SELL,
                        new BigDecimal("5"), new BigDecimal("300.00")));
        when(transactionRepository.findByUser(user)).thenReturn(transactions);
        when(finnhubClient.getCurrentPrice("AAPL")).thenReturn(new BigDecimal("24.00"));

        List<PortfolioDTO> result = transactionService.getPortfolioAnalysis(user);

        assertThat(result).hasSize(1);
        PortfolioDTO dto = result.get(0);
        assertThat(dto.getTicker()).isEqualTo("AAPL");
        assertThat(dto.getAsset()).isEqualTo("Apple");
        assertThat(dto.getTotalShares()).isEqualByComparingTo(new BigDecimal("15"));
        assertThat(dto.getMeanPrice()).isEqualByComparingTo(new BigDecimal("20.0000"));
        assertThat(dto.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("24.00"));
        assertThat(dto.getMarketValue()).isEqualByComparingTo(new BigDecimal("360.00"));
        assertThat(dto.getProfitLossEuros()).isEqualByComparingTo(new BigDecimal("60.00"));
        assertThat(dto.getProfitLossPercent()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(dto.getPortfolioWeight()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void getPortfolioAnalysis_usesMeanPriceWhenFinnhubReturnsZero() {
        User user = buildUser();
        List<Transaction> transactions = List.of(
                buildTransaction(1L, "TSLA", "Tesla", TransactionType.BUY,
                        new BigDecimal("10"), new BigDecimal("100.00")));
        when(transactionRepository.findByUser(user)).thenReturn(transactions);
        when(finnhubClient.getCurrentPrice("TSLA")).thenReturn(BigDecimal.ZERO);

        List<PortfolioDTO> result = transactionService.getPortfolioAnalysis(user);

        assertThat(result).hasSize(1);
        PortfolioDTO dto = result.get(0);
        assertThat(dto.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(dto.getMarketValue()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(dto.getProfitLossEuros()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2));
        assertThat(dto.getProfitLossPercent()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2));
    }

    @Test
    void getPortfolioAnalysis_usesMeanPriceWhenFinnhubReturnsNull() {
        User user = buildUser();
        List<Transaction> transactions = List.of(
                buildTransaction(1L, "TSLA", "Tesla", TransactionType.BUY,
                        new BigDecimal("10"), new BigDecimal("100.00")));
        when(transactionRepository.findByUser(user)).thenReturn(transactions);
        when(finnhubClient.getCurrentPrice("TSLA")).thenReturn(null);

        List<PortfolioDTO> result = transactionService.getPortfolioAnalysis(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCurrentPrice()).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    void getPortfolioAnalysis_filtersOutClosedPositions() {
        User user = buildUser();
        List<Transaction> transactions = List.of(
                buildTransaction(1L, "MSFT", "Microsoft", TransactionType.BUY,
                        new BigDecimal("5"), new BigDecimal("100.00")),
                buildTransaction(2L, "MSFT", "Microsoft", TransactionType.SELL,
                        new BigDecimal("5"), new BigDecimal("120.00")),
                buildTransaction(3L, "AAPL", "Apple", TransactionType.BUY,
                        new BigDecimal("2"), new BigDecimal("40.00")));
        when(transactionRepository.findByUser(user)).thenReturn(transactions);
        when(finnhubClient.getCurrentPrice("AAPL")).thenReturn(new BigDecimal("30.00"));

        List<PortfolioDTO> result = transactionService.getPortfolioAnalysis(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("AAPL");
        verify(finnhubClient, never()).getCurrentPrice("MSFT");
    }

    @Test
    void getPortfolioAnalysis_calculatesWeightAcrossMultiplePositions() {
        User user = buildUser();
        List<Transaction> transactions = List.of(
                buildTransaction(1L, "AAPL", "Apple", TransactionType.BUY,
                        new BigDecimal("10"), new BigDecimal("150.00")),
                buildTransaction(2L, "MSFT", "Microsoft", TransactionType.BUY,
                        new BigDecimal("5"), new BigDecimal("250.00")));
        when(transactionRepository.findByUser(user)).thenReturn(transactions);
        when(finnhubClient.getCurrentPrice("AAPL")).thenReturn(new BigDecimal("30.00"));
        when(finnhubClient.getCurrentPrice("MSFT")).thenReturn(new BigDecimal("50.00"));

        List<PortfolioDTO> result = transactionService.getPortfolioAnalysis(user);

        assertThat(result).hasSize(2);
        // AAPL: 10 * 30 = 300; MSFT: 5 * 50 = 250; total = 550
        PortfolioDTO aapl = result.stream().filter(dto -> dto.getTicker().equals("AAPL")).findFirst().orElseThrow();
        PortfolioDTO msft = result.stream().filter(dto -> dto.getTicker().equals("MSFT")).findFirst().orElseThrow();
        assertThat(aapl.getPortfolioWeight()).isEqualByComparingTo(new BigDecimal("54.55"));
        assertThat(msft.getPortfolioWeight()).isEqualByComparingTo(new BigDecimal("45.45"));
    }
}
