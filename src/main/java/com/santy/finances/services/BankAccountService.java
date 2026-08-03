package com.santy.finances.services;

import com.santy.finances.exceptions.ResourceNotFoundException;
import com.santy.finances.models.BankAccount;
import com.santy.finances.repositories.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountService {
    private final BankAccountRepository bankAccountRepository;

    /**
     * Retrieves all bank accounts from the database.
     *
     * @return A list containing all stored bank accounts.
     */
    @Transactional(readOnly = true)
    public List<BankAccount> getAllAccounts(){
        return bankAccountRepository.findAll();
    }

    /**
     * Saves a new bank account into the database.
     *
     * @param newAccount The bank account entity to save.
     * @return The saved bank account entity.
     */
    @Transactional
    public BankAccount createAccount(BankAccount newAccount) {
        return bankAccountRepository.save(newAccount);
    }

    /**
     * Searches for a bank account by its ID and updates it with new data.
     *
     * @param id The ID of the bank account to update.
     * @param newAccount The new bank account data to overwrite the existing one.
     * @return The updated and saved bank account entity.
     * @throws ResourceNotFoundException if the bank account ID is not found.
     */
    @Transactional
    public BankAccount updateAccount(Long id, BankAccount newAccount) {
        return bankAccountRepository.findById(id).map(existingAccount -> {
            existingAccount.setName(newAccount.getName());
            existingAccount.setInitialBalance(newAccount.getInitialBalance());
            return bankAccountRepository.save(existingAccount);
        }).orElseThrow(() -> new ResourceNotFoundException("Bank Account not found with ID: " + id));
    }

    /**
     * Deletes a bank account from the database by its ID.
     *
     * @param id The ID of the bank account to be removed.
     * @throws ResourceNotFoundException if the bank account ID is not found.
     */
    @Transactional
    public void deleteAccount(Long id) {
        if(!bankAccountRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bank Account not found with ID: " + id);
        }
        bankAccountRepository.deleteById(id);
    }
}
