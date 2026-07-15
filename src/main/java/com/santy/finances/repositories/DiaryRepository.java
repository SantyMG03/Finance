package com.santy.finances.repositories;

import com.santy.finances.models.BankAccount;
import com.santy.finances.models.Diary;
import com.santy.finances.models.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    List<Diary> findByCategory(Category category);

    List<Diary> findByBankAccountId(Long bankAccountId);
}
