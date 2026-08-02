package com.santy.finances.controllers;

import com.santy.finances.DTOs.PortfolioDTO;
import com.santy.finances.models.Transaction;
import com.santy.finances.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Transaction> registerTransaction(@RequestBody Transaction newTransaction) {
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
     * GET Request: Retrieves all stored transactions.
     *
     * @return HTTP 200 (OK) and a list of all transactions.
     */
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
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
            @RequestBody Transaction transaction) {
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