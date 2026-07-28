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
     * POST Reques: Save a new stock or ETF purchase or sale.
     * @param newTransaction Transaction data to save.
     * @return 201 code and the data saved.
     */
    @PostMapping
    public ResponseEntity<Transaction> registerTransaction(@RequestBody Transaction newTransaction) {
        Transaction savedTransaction = transactionService.registerTransaction(newTransaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);
    }

    /**
     * GET Request: Returns the portfolio analysis.
     * @return 200 code.
     */
    @GetMapping("/portfolio")
    public ResponseEntity<List<PortfolioDTO>> getPortfolioAnalysis() {
        List<PortfolioDTO> portfolio = transactionService.getPortfolioAnalysis();
        return ResponseEntity.ok(portfolio);
    }
}
