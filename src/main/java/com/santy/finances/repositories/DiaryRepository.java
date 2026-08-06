package com.santy.finances.repositories;

import com.santy.finances.models.Diary;
import com.santy.finances.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    List<Diary> findByUser(User user);

    Optional<Diary> findByIdAndUser(Long id, User user);
}
