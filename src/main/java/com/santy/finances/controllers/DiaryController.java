package com.santy.finances.controllers;

import com.santy.finances.models.Diary;
import com.santy.finances.services.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {
    private DiaryService diaryService;

    /**
     * POST Request: Send a new income/outcome
     * Upon execution, it will save the record to the Diary table
     * and update the balance of the associated BankAccount.
     * @param newDiary Diary Data
     * @return 201 code and the data saved.
     */
    @PostMapping
    public ResponseEntity<Diary> registerDiaryEntry(@RequestBody Diary newDiary) {
        Diary savedDiary = diaryService.registerNewDiary(newDiary);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDiary);
    }
}
