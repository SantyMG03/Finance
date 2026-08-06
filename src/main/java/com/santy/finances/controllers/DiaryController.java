package com.santy.finances.controllers;

import com.santy.finances.models.Diary;
import com.santy.finances.models.User;
import com.santy.finances.services.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    /**
     * GET Request: Retrieves all diary entries from the authenticated user.
     *
     * @return HTTP 200 (OK) and a list of all the user's entries.
     */
    @GetMapping
    public ResponseEntity<List<Diary>> getAllDiaryEntries() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        List<Diary> entries = diaryService.getUserDiaries(currentUser);
        return ResponseEntity.ok(entries);
    }

    /**
     * POST Request: Saves a new diary entry into the database for the authenticated user.
     *
     * @param newDiary The entry data to save.
     * @return HTTP 201 (Created) and the saved diary data.
     */
    @PostMapping
    public ResponseEntity<Diary> registerDiaryEntry(@Valid @RequestBody Diary newDiary) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Diary savedDiary = diaryService.registerNewDiary(newDiary, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDiary);
    }

    /**
     * PUT Request: Updates an existing diary entry owned by the authenticated user.
     *
     * @param id The ID of the diary entry to update.
     * @param diary The new diary entry data to overwrite the existing one.
     * @return HTTP 200 (OK) and the updated diary data.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Diary> updateDiaryEntry(
            @PathVariable Long id,
            @Valid @RequestBody Diary diary) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Diary updated = diaryService.updateDiary(id, diary, currentUser);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE Request: Deletes a diary entry owned by the authenticated user, by its ID.
     *
     * @param id The ID of the diary entry to be removed.
     * @return HTTP 204 (No Content) upon successful deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiaryEntry(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        diaryService.deleteDiary(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}