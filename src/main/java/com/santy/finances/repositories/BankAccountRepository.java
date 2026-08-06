package com.santy.finances.repositories;

import com.santy.finances.models.BankAccount;
import com.santy.finances.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByUser(User user);

    Optional<BankAccount> findByIdAndUser(Long id, User user);

    boolean existsByIdAndUser(Long id, User user);
}
