package com.santy.finances.controllers;

import com.santy.finances.DTOs.PortfolioDTO;
import com.santy.finances.models.Transaction;
import com.santy.finances.models.User;
import com.santy.finances.services.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * POST Request: Saves a new stock or ETF purchase/sale transaction.
     *
     * @param newTransaction The transaction data to save.
     * @return HTTP 201 (Created) and the saved transaction data.
     */
    @PostMapping
    public ResponseEntity<Transaction> registerTransaction(@Valid @RequestBody Transaction newTransaction) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        newTransaction.setUser(currentUser);

        Transaction savedTransaction = transactionService.registerTransaction(newTransaction);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);
    }

    /**
     * GET Request: Returns the complete portfolio analysis.
     *
     * @return HTTP 200 (OK) and a list of PortfolioDTOs.
     */
    @GetMapping("/portfolio")
    public ResponseEntity<List<PortfolioDTO>> getPortfolioAnalysis() {
        List<PortfolioDTO> portfolio = transactionService.getPortfolioAnalysis();
        return ResponseEntity.ok(portfolio);
    }

    /**
     * GET Request: Retrieves all transactions from an authenticated user.
     *
     * @return HTTP 200 (OK) and a list of all transactions.
     */
    @GetMapping
    public ResponseEntity<List<Transaction>> getMyTransactions() {

        // Get the current user from the authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        List<Transaction> userTransactions = transactionService.getUserTransactions(currentUser);

        return ResponseEntity.ok(userTransactions);
    }

    /**
     * PUT Request: Updates an existing transaction.
     *
     * @param id The ID of the transaction to update.
     * @param transaction The new transaction data to overwrite.
     * @return HTTP 200 (OK) and the updated transaction data.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody Transaction transaction) {
        Transaction updated = transactionService.updateTransaction(id, transaction);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE Request: Deletes a transaction by its ID.
     *
     * @param id The ID of the transaction to be removed.
     * @return HTTP 204 (No Content) upon successful deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}