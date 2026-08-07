package com.santy.finances.services;

import com.santy.finances.exceptions.ResourceNotFoundException;
import com.santy.finances.models.BankAccount;
import com.santy.finances.models.User;
import com.santy.finances.repositories.BankAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private BankAccountService bankAccountService;

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("santy");
        return user;
    }

    private BankAccount buildAccount(Long id, String name, BigDecimal balance) {
        BankAccount account = new BankAccount();
        account.setId(id);
        account.setName(name);
        account.setInitialBalance(balance);
        return account;
    }

    @Test
    void getUserAccounts_returnsAccountsOfUser() {
        User user = buildUser();
        List<BankAccount> accounts = List.of(
                buildAccount(1L, "Cuenta corriente", new BigDecimal("1000.00")),
                buildAccount(2L, "Ahorro", new BigDecimal("500.00")));
        when(bankAccountRepository.findByUser(user)).thenReturn(accounts);

        List<BankAccount> result = bankAccountService.getUserAccounts(user);

        assertThat(result).isEqualTo(accounts);
        verify(bankAccountRepository).findByUser(user);
    }

    @Test
    void createAccount_setsUserAndSaves() {
        User user = buildUser();
        BankAccount account = buildAccount(null, "Cuenta corriente", new BigDecimal("100.00"));
        when(bankAccountRepository.save(account)).thenReturn(account);

        BankAccount result = bankAccountService.createAccount(account, user);

        assertThat(result).isSameAs(account);
        assertThat(account.getUser()).isSameAs(user);
        verify(bankAccountRepository).save(account);
    }

    @Test
    void updateAccount_success_updatesAndSaves() {
        User user = buildUser();
        BankAccount existing = buildAccount(1L, "Viejo", new BigDecimal("100.00"));
        BankAccount newData = buildAccount(1L, "Nuevo", new BigDecimal("200.00"));
        when(bankAccountRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(existing));
        when(bankAccountRepository.save(existing)).thenReturn(existing);

        BankAccount result = bankAccountService.updateAccount(1L, newData, user);

        assertThat(result).isSameAs(existing);
        assertThat(existing.getName()).isEqualTo("Nuevo");
        assertThat(existing.getInitialBalance()).isEqualByComparingTo(new BigDecimal("200.00"));
        verify(bankAccountRepository).save(existing);
    }

    @Test
    void updateAccount_throwsWhenAccountNotOwned() {
        User user = buildUser();
        BankAccount newData = buildAccount(99L, "Nuevo", new BigDecimal("200.00"));
        when(bankAccountRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankAccountService.updateAccount(99L, newData, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Bank Account not found with ID: 99");
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void deleteAccount_success_deletesById() {
        User user = buildUser();
        when(bankAccountRepository.existsByIdAndUser(1L, user)).thenReturn(true);

        bankAccountService.deleteAccount(1L, user);

        verify(bankAccountRepository).deleteById(1L);
    }

    @Test
    void deleteAccount_throwsWhenAccountNotOwned() {
        User user = buildUser();
        when(bankAccountRepository.existsByIdAndUser(1L, user)).thenReturn(false);

        assertThatThrownBy(() -> bankAccountService.deleteAccount(1L, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Bank Account not found with ID: 1");
        verify(bankAccountRepository, never()).deleteById(1L);
    }
}
