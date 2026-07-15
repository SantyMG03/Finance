package com.santy.finances.services;

import com.santy.finances.models.BankAccount;
import com.santy.finances.models.Diary;
import com.santy.finances.models.Transaction;
import com.santy.finances.models.enums.DiaryType;
import com.santy.finances.repositories.BankAccountRepository;
import com.santy.finances.repositories.DiaryRepository;
import com.santy.finances.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final BankAccountRepository bankAccountRepository;

    /**
     * This method updates the amount of money inside an account, additionally inserts a
     * new input to the Diary table.
     * @param newDiary input to add to the Diary table
     * @return saved entity
     */
    @Transactional
    public Diary registerNewDiary(Diary newDiary) {
        BankAccount account = bankAccountRepository
                .findById(newDiary.getBankAccount().getId())
                .orElseThrow(() -> new RuntimeException("Error: Bank account not found"));

        // Check input type
        if (newDiary.getDiaryType() == DiaryType.INCOME) {
            account.setInitialBalance(account.getInitialBalance().add(newDiary.getAmount()));
        } else if (newDiary.getDiaryType() == DiaryType.OUTCOME) {
            account.setInitialBalance(account.getInitialBalance().subtract(newDiary.getAmount()));
        }

        bankAccountRepository.save(account);
        return diaryRepository.save(newDiary);
    }
}
