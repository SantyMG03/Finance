package com.santy.finances.controllers;

import com.santy.finances.models.Diary;
import com.santy.finances.services.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    /**
     * GET Request: Retrieves all stored diary entries.
     *
     * @return HTTP 200 (OK) and a list of all entries.
     */
    @GetMapping
    public ResponseEntity<List<Diary>> getAllDiaryEntries() {
        List<Diary> entries = diaryService.getAllDiaries();
        return ResponseEntity.ok(entries);
    }

    /**
     * POST Request: Saves a new diary entry into the database.
     *
     * @param newDiary The entry data to save.
     * @return HTTP 201 (Created) and the saved diary data.
     */
    @PostMapping
    public ResponseEntity<Diary> registerDiaryEntry(@RequestBody Diary newDiary) {
        Diary savedDiary = diaryService.registerNewDiary(newDiary);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDiary);
    }

    /**
     * PUT Request: Updates an existing diary entry.
     *
     * @param id The ID of the diary entry to update.
     * @param diary The new diary entry data to overwrite the existing one.
     * @return HTTP 200 (OK) and the updated diary data.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Diary> updateDiaryEntry(
            @PathVariable Long id,
            @RequestBody Diary diary) {
        Diary updated = diaryService.updateDiary(id, diary);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE Request: Deletes a diary entry by its ID.
     *
     * @param id The ID of the diary entry to be removed.
     * @return HTTP 204 (No Content) upon successful deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiaryEntry(@PathVariable Long id) {
        diaryService.deleteDiary(id);
        return ResponseEntity.noContent().build();
    }
}