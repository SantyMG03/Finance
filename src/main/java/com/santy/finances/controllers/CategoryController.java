package com.santy.finances.controllers;

import com.santy.finances.models.Category;
import com.santy.finances.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * GET Request: Retrieves all stored categories.
     *
     * @return HTTP 200 (OK) and a list of all categories.
     */
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * POST Request: Saves a new category into the database.
     *
     * @param newCategory The category data to save.
     * @return HTTP 201 (Created) and the saved category data.
     */
    @PostMapping
    public ResponseEntity<Category> registerCategory(@RequestBody Category newCategory) {
        Category savedCategory = categoryService.registerCategory(newCategory);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }

    /**
     * PUT Request: Updates an existing category.
     *
     * @param id The ID of the category to update.
     * @param category The new category data to overwrite the existing one.
     * @return HTTP 200 (OK) and the updated category data.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @RequestBody Category category) {
        Category updated = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE Request: Deletes a category by its ID.
     *
     * @param id The ID of the category to be removed.
     * @return HTTP 204 (No Content) upon successful deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}