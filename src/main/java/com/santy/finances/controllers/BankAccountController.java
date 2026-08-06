package com.santy.finances.controllers;

import com.santy.finances.models.BankAccount;
import com.santy.finances.models.User;
import com.santy.finances.services.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class BankAccountController {
    private final BankAccountService bankAccountService;

    /**
     * GET Request: Retrieves all bank accounts from the authenticated user.
     *
     * @return HTTP 200 (OK) and a list of all the user's bank accounts.
     */
    @GetMapping
    public ResponseEntity<List<BankAccount>> getAllAccounts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        List<BankAccount> accounts = bankAccountService.getUserAccounts(currentUser);
        return ResponseEntity.ok(accounts);
    }

    /**
     * POST Request: Saves a new bank account into the database for the authenticated user.
     *
     * @param newAccount The bank account data to save.
     * @return HTTP 201 (Created) and the saved bank account data.
     */
    @PostMapping
    public ResponseEntity<BankAccount> createNewAccount(@Valid @RequestBody BankAccount newAccount) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        BankAccount savedAccount = bankAccountService.createAccount(newAccount, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
    }

    /**
     * PUT Request: Updates an existing bank account owned by the authenticated user.
     *
     * @param id The ID of the bank account to update.
     * @param account The new bank account data to overwrite the existing one.
     * @return HTTP 200 (OK) and the updated bank account data.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BankAccount> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody BankAccount account) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        BankAccount updated = bankAccountService.updateAccount(id, account, currentUser);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE Request: Deletes a bank account owned by the authenticated user, by its ID.
     *
     * @param id The ID of the bank account to be removed.
     * @return HTTP 204 (No Content) upon successful deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        bankAccountService.deleteAccount(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}