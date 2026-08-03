package com.santy.finances.services;

import com.santy.finances.exceptions.ResourceNotFoundException;
import com.santy.finances.models.BankAccount;
import com.santy.finances.models.Category;
import com.santy.finances.models.Diary;
import com.santy.finances.models.enums.DiaryType;
import com.santy.finances.repositories.BankAccountRepository;
import com.santy.finances.repositories.CategoryRepository;
import com.santy.finances.repositories.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Retrieves all diary entries from the database.
     *
     * @return A list containing all stored diary entries.
     */
    @Transactional(readOnly = true)
    public List<Diary> getAllDiaries() {
        return diaryRepository.findAll();
    }

    /**
     * Saves a new diary entry and automatically updates the corresponding bank account balance.
     *
     * @param newDiary The diary entry data to save.
     * @return The saved diary entity.
     * @throws ResourceNotFoundException if the bank account or category is not found.
     */
    @Transactional
    public Diary registerNewDiary(Diary newDiary) {
        BankAccount account = bankAccountRepository
                .findById(newDiary.getBankAccount().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Error: Bank account not found"));

        Category category = categoryRepository
                .findById(newDiary.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Error: Category not found"));

        // Check input type and update balance
        if (newDiary.getDiaryType() == DiaryType.INCOME) {
            account.setInitialBalance(account.getInitialBalance().add(newDiary.getAmount()));
        } else if (newDiary.getDiaryType() == DiaryType.OUTCOME) {
            account.setInitialBalance(account.getInitialBalance().subtract(newDiary.getAmount()));
        }

        bankAccountRepository.save(account);
        newDiary.setBankAccount(account);
        newDiary.setCategory(category);

        return diaryRepository.save(newDiary);
    }

    /**
     * Searches for a diary entry by its ID, updates it, and recalculates the bank account balance.
     *
     * @param id The ID of the entry to update.
     * @param updatedData The new entry data to overwrite the existing one.
     * @return The updated and saved diary entity.
     * @throws ResourceNotFoundException if the entry diary ID is not found.
     */
    @Transactional
    public Diary updateDiary(Long id, Diary updatedData) {
        Diary existingDiary = diaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diary entry not found with ID: " + id));

        BankAccount account = bankAccountRepository.findById(existingDiary.getBankAccount().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bank Account not found"));

        Category newCategory = categoryRepository.findById(updatedData.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Revert the old amount from the bank account
        if (existingDiary.getDiaryType() == DiaryType.INCOME) {
            account.setInitialBalance(account.getInitialBalance().subtract(existingDiary.getAmount()));
        } else {
            account.setInitialBalance(account.getInitialBalance().add(existingDiary.getAmount()));
        }

        // Apply the new amount to the bank account
        if (updatedData.getDiaryType() == DiaryType.INCOME) {
            account.setInitialBalance(account.getInitialBalance().add(updatedData.getAmount()));
        } else {
            account.setInitialBalance(account.getInitialBalance().subtract(updatedData.getAmount()));
        }

        // Update the fields
        existingDiary.setDiaryType(updatedData.getDiaryType());
        existingDiary.setDate(updatedData.getDate());
        existingDiary.setAmount(updatedData.getAmount());
        existingDiary.setConcept(updatedData.getConcept());
        existingDiary.setInfo(updatedData.getInfo());
        existingDiary.setCategory(newCategory);

        bankAccountRepository.save(account);
        return diaryRepository.save(existingDiary);
    }

    /**
     * Deletes a diary entry by its ID and reverts its effect on the bank account balance.
     *
     * @param id The ID of the entry to be removed.
     * @throws ResourceNotFoundException if the diary entry ID is not found.
     */
    @Transactional
    public void deleteDiary(Long id) {
        Diary existingDiary = diaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diary entry not found with ID: " + id));

        BankAccount account = existingDiary.getBankAccount();

        // Revert balance before deleting
        if (existingDiary.getDiaryType() == DiaryType.INCOME) {
            account.setInitialBalance(account.getInitialBalance().subtract(existingDiary.getAmount()));
        } else {
            account.setInitialBalance(account.getInitialBalance().add(existingDiary.getAmount()));
        }

        bankAccountRepository.save(account);
        diaryRepository.deleteById(id);
    }
}