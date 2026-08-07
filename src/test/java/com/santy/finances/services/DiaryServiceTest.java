package com.santy.finances.services;

import com.santy.finances.exceptions.ResourceNotFoundException;
import com.santy.finances.models.BankAccount;
import com.santy.finances.models.Category;
import com.santy.finances.models.Diary;
import com.santy.finances.models.User;
import com.santy.finances.models.enums.DiaryType;
import com.santy.finances.repositories.BankAccountRepository;
import com.santy.finances.repositories.CategoryRepository;
import com.santy.finances.repositories.DiaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DiaryService diaryService;

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("santy");
        return user;
    }

    private BankAccount buildAccount(Long id, BigDecimal balance) {
        BankAccount account = new BankAccount();
        account.setId(id);
        account.setName("Cuenta");
        account.setInitialBalance(balance);
        return account;
    }

    private Category buildCategory(Long id) {
        Category category = new Category();
        category.setId(id);
        category.setName("Ocio");
        return category;
    }

    private Diary buildDiary(Long id, DiaryType type, BigDecimal amount, BankAccount account, Category category) {
        Diary diary = new Diary();
        diary.setId(id);
        diary.setDate(LocalDate.of(2026, 8, 7));
        diary.setBankAccount(account);
        diary.setCategory(category);
        diary.setDiaryType(type);
        diary.setAmount(amount);
        diary.setConcept("Pago");
        return diary;
    }

    @Test
    void getUserDiaries_returnsDiariesOfUser() {
        User user = buildUser();
        BankAccount account = buildAccount(1L, new BigDecimal("1000.00"));
        Category category = buildCategory(1L);
        List<Diary> diaries = List.of(buildDiary(1L, DiaryType.INCOME, new BigDecimal("50.00"), account, category));
        when(diaryRepository.findByUser(user)).thenReturn(diaries);

        List<Diary> result = diaryService.getUserDiaries(user);

        assertThat(result).isEqualTo(diaries);
        verify(diaryRepository).findByUser(user);
    }

    @Test
    void registerNewDiary_income_incrementsAccountBalance() {
        User user = buildUser();
        BankAccount account = buildAccount(1L, new BigDecimal("1000.00"));
        Category category = buildCategory(1L);
        Diary newDiary = buildDiary(null, DiaryType.INCOME, new BigDecimal("250.00"), account, category);
        when(bankAccountRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(category));
        when(diaryRepository.save(newDiary)).thenReturn(newDiary);

        Diary result = diaryService.registerNewDiary(newDiary, user);

        assertThat(result).isSameAs(newDiary);
        assertThat(account.getInitialBalance()).isEqualByComparingTo(new BigDecimal("1250.00"));
        assertThat(newDiary.getUser()).isSameAs(user);
        assertThat(newDiary.getBankAccount()).isSameAs(account);
        assertThat(newDiary.getCategory()).isSameAs(category);
        verify(bankAccountRepository).save(account);
        verify(diaryRepository).save(newDiary);
    }

    @Test
    void registerNewDiary_outcome_decrementsAccountBalance() {
        User user = buildUser();
        BankAccount account = buildAccount(1L, new BigDecimal("1000.00"));
        Category category = buildCategory(1L);
        Diary newDiary = buildDiary(null, DiaryType.OUTCOME, new BigDecimal("300.00"), account, category);
        when(bankAccountRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(category));
        when(diaryRepository.save(newDiary)).thenReturn(newDiary);

        diaryService.registerNewDiary(newDiary, user);

        assertThat(account.getInitialBalance()).isEqualByComparingTo(new BigDecimal("700.00"));
        verify(bankAccountRepository).save(account);
    }

    @Test
    void registerNewDiary_throwsWhenBankAccountNotFound() {
        User user = buildUser();
        BankAccount account = buildAccount(99L, new BigDecimal("1000.00"));
        Category category = buildCategory(1L);
        Diary newDiary = buildDiary(null, DiaryType.INCOME, new BigDecimal("50.00"), account, category);
        when(bankAccountRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diaryService.registerNewDiary(newDiary, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Error: Bank account not found");
        verify(diaryRepository, never()).save(any());
    }

    @Test
    void registerNewDiary_throwsWhenCategoryNotFound() {
        User user = buildUser();
        BankAccount account = buildAccount(1L, new BigDecimal("1000.00"));
        Category category = buildCategory(99L);
        Diary newDiary = buildDiary(null, DiaryType.INCOME, new BigDecimal("50.00"), account, category);
        when(bankAccountRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diaryService.registerNewDiary(newDiary, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Error: Category not found");
        verify(diaryRepository, never()).save(any());
    }

    @Test
    void updateDiary_switchingFromIncomeToOutcome_recalculatesBalance() {
        User user = buildUser();
        BankAccount account = buildAccount(1L, new BigDecimal("1000.00"));
        Category oldCategory = buildCategory(1L);
        Category newCategory = buildCategory(2L);
        Diary existing = buildDiary(1L, DiaryType.INCOME, new BigDecimal("100.00"), account, oldCategory);
        existing.setUser(user);
        Diary updatedData = buildDiary(1L, DiaryType.OUTCOME, new BigDecimal("40.00"), account, newCategory);

        when(diaryRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(existing));
        when(bankAccountRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUser(2L, user)).thenReturn(Optional.of(newCategory));
        when(diaryRepository.save(existing)).thenReturn(existing);

        Diary result = diaryService.updateDiary(1L, updatedData, user);

        // 1000 - 100 (revert old income) - 40 (apply new outcome) = 860
        assertThat(account.getInitialBalance()).isEqualByComparingTo(new BigDecimal("860.00"));
        assertThat(existing.getDiaryType()).isEqualTo(DiaryType.OUTCOME);
        assertThat(existing.getAmount()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(existing.getCategory()).isSameAs(newCategory);
        assertThat(existing.getConcept()).isEqualTo("Pago");
        verify(bankAccountRepository).save(account);
        verify(diaryRepository).save(existing);
    }

    @Test
    void updateDiary_throwsWhenDiaryNotFound() {
        User user = buildUser();
        Diary updatedData = buildDiary(1L, DiaryType.INCOME, new BigDecimal("40.00"), buildAccount(1L, BigDecimal.ZERO), buildCategory(1L));
        when(diaryRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diaryService.updateDiary(99L, updatedData, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Diary entry not found with ID: 99");
    }

    @Test
    void updateDiary_throwsWhenBankAccountNotFound() {
        User user = buildUser();
        BankAccount account = buildAccount(99L, new BigDecimal("1000.00"));
        Category category = buildCategory(1L);
        Diary existing = buildDiary(1L, DiaryType.INCOME, new BigDecimal("100.00"), account, category);
        existing.setUser(user);
        Diary updatedData = buildDiary(1L, DiaryType.OUTCOME, new BigDecimal("40.00"), account, category);
        when(diaryRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(existing));
        when(bankAccountRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diaryService.updateDiary(1L, updatedData, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Bank Account not found");
    }

    @Test
    void updateDiary_throwsWhenCategoryNotFound() {
        User user = buildUser();
        BankAccount account = buildAccount(1L, new BigDecimal("1000.00"));
        Category category = buildCategory(1L);
        Diary existing = buildDiary(1L, DiaryType.INCOME, new BigDecimal("100.00"), account, category);
        existing.setUser(user);
        Diary updatedData = buildDiary(1L, DiaryType.OUTCOME, new BigDecimal("40.00"), account, buildCategory(99L));
        when(diaryRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(existing));
        when(bankAccountRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diaryService.updateDiary(1L, updatedData, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found");
    }

    @Test
    void deleteDiary_revertsBalanceAndDeletes() {
        User user = buildUser();
        BankAccount account = buildAccount(1L, new BigDecimal("1000.00"));
        Category category = buildCategory(1L);
        Diary existing = buildDiary(1L, DiaryType.OUTCOME, new BigDecimal("300.00"), account, category);
        existing.setUser(user);
        when(diaryRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(existing));

        diaryService.deleteDiary(1L, user);

        // 1000 + 300 (revert outcome) = 1300
        assertThat(account.getInitialBalance()).isEqualByComparingTo(new BigDecimal("1300.00"));
        verify(bankAccountRepository).save(account);
        verify(diaryRepository).deleteById(1L);
    }

    @Test
    void deleteDiary_throwsWhenDiaryNotFound() {
        User user = buildUser();
        when(diaryRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diaryService.deleteDiary(99L, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Diary entry not found with ID: 99");
        verify(diaryRepository, never()).deleteById(99L);
    }
}
