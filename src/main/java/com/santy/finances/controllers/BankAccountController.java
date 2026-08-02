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
     * GET Request: Retrieves all stored bank accounts.
     *
     * @return HTTP 200 (OK) and a list of all bank accounts.
     */
    @GetMapping
    public ResponseEntity<List<BankAccount>> getAllAccounts() {
        List<BankAccount> accounts = bankAccountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    /**
     * POST Request: Saves a new bank account into the database.
     *
     * @param newAccount The bank account data to save.
     * @return HTTP 201 (Created) and the saved bank account data.
     */
    @PostMapping
    public ResponseEntity<BankAccount> createNewAccount(@RequestBody BankAccount newAccount) {
        BankAccount savedAccount = bankAccountService.createAccount(newAccount);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
    }

    /**
     * PUT Request: Updates an existing bank account.
     *
     * @param id The ID of the bank account to update.
     * @param account The new bank account data to overwrite the existing one.
     * @return HTTP 200 (OK) and the updated bank account data.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BankAccount> updateAccount(
            @PathVariable Long id,
            @RequestBody BankAccount account) {
        BankAccount updated = bankAccountService.updateAccount(id, account);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE Request: Deletes a bank account by its ID.
     *
     * @param id The ID of the bank account to be removed.
     * @return HTTP 204 (No Content) upon successful deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        bankAccountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}