package com.santy.finances.controllers;

import com.santy.finances.models.BankAccount;
import com.santy.finances.services.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class BankAccountController {
    private final BankAccountService bankAccountService;

    /**
     * GET Request
     * @return The list of all bank accounts
     */
    @GetMapping
    public ResponseEntity<List<BankAccount>> getAllAccounts() {
        List<BankAccount> accounts = bankAccountService.listAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    /**
     * POST Request allows to save a new account into the DB
     * @param newAccount Account to save.
     * @return 201 code and the data of the newly saved account.
     */
    @PostMapping
    public ResponseEntity<BankAccount> createNewAccount(@RequestBody BankAccount newAccount) {
        BankAccount savedAccount = bankAccountService.createAccount(newAccount);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
    }
}
