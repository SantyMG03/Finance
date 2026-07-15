package com.santy.finances.services;

import com.santy.finances.models.BankAccount;
import com.santy.finances.repositories.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountService {
    private final BankAccountRepository bankAccountRepository;

    /**
     * @return all accounts in the database
     */
    public List<BankAccount> listAllAccounts(){
        return bankAccountRepository.findAll();
    }

    /**
     * Method to add a new account to the database
     * @param newAccount new account to add
     * @return saved entity
     */
    public BankAccount createAccount(BankAccount newAccount) {
        //todo: Add validation checks (does exist this account in the db?)
        return bankAccountRepository.save(newAccount);
    }
}
