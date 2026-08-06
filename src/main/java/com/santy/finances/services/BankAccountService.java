package com.santy.finances.services;

import com.santy.finances.exceptions.ResourceNotFoundException;
import com.santy.finances.models.BankAccount;
import com.santy.finances.models.User;
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
     * Retrieves all bank accounts belonging to the given user.
     *
     * @param user The user whose accounts should be retrieved.
     * @return A list containing all the user's stored bank accounts.
     */
    @Transactional(readOnly = true)
    public List<BankAccount> getUserAccounts(User user){
        return bankAccountRepository.findByUser(user);
    }

    /**
     * Saves a new bank account into the database for the given user.
     *
     * @param newAccount The bank account entity to save.
     * @param user The user that owns the account.
     * @return The saved bank account entity.
     */
    @Transactional
    public BankAccount createAccount(BankAccount newAccount, User user) {
        newAccount.setUser(user);
        return bankAccountRepository.save(newAccount);
    }

    /**
     * Searches for a bank account owned by the user, by its ID, and updates it with new data.
     *
     * @param id The ID of the bank account to update.
     * @param newAccount The new bank account data to overwrite the existing one.
     * @param user The user that owns the account.
     * @return The updated and saved bank account entity.
     * @throws ResourceNotFoundException if the bank account ID is not found for the user.
     */
    @Transactional
    public BankAccount updateAccount(Long id, BankAccount newAccount, User user) {
        return bankAccountRepository.findByIdAndUser(id, user).map(existingAccount -> {
            existingAccount.setName(newAccount.getName());
            existingAccount.setInitialBalance(newAccount.getInitialBalance());
            return bankAccountRepository.save(existingAccount);
        }).orElseThrow(() -> new ResourceNotFoundException("Bank Account not found with ID: " + id));
    }

    /**
     * Deletes a bank account owned by the user, by its ID.
     *
     * @param id The ID of the bank account to be removed.
     * @param user The user that owns the account.
     * @throws ResourceNotFoundException if the bank account ID is not found for the user.
     */
    @Transactional
    public void deleteAccount(Long id, User user) {
        if(!bankAccountRepository.existsByIdAndUser(id, user)) {
            throw new ResourceNotFoundException("Bank Account not found with ID: " + id);
        }
        bankAccountRepository.deleteById(id);
    }
}
