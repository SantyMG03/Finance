package com.santy.finances.repositories;

import com.santy.finances.models.Transaction;
import com.santy.finances.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByTicker(String ticker);

    List<Transaction> findByBroker(String broker);

    List<Transaction> findByUser(User user);
}
