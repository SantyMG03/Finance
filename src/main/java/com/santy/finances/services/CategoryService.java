package com.santy.finances.services;

import com.santy.finances.models.Category;
import com.santy.finances.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Retrieves all categories from the database.
     *
     * @return A list containing all stored categories.
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Saves a new category into the database.
     *
     * @param c The category entity to save.
     * @return The saved category entity.
     */
    @Transactional
    public Category registerCategory(Category c) {
        return categoryRepository.save(c);
    }

    /**
     * Searches for a category by its ID and updates it with new data.
     *
     * @param id The ID of the category to update.
     * @param updatedData The new category data to overwrite the existing one.
     * @return The updated and saved category entity.
     * @throws RuntimeException if the category ID is not found.
     */
    @Transactional
    public Category updateCategory(Long id, Category updatedData) {
        return categoryRepository.findById(id).map(existingCategory -> {
            existingCategory.setName(updatedData.getName());
            return categoryRepository.save(existingCategory);
        }).orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));
    }

    /**
     * Deletes a category from the database by its ID.
     *
     * @param id The ID of the category to be removed.
     * @throws RuntimeException if the category ID is not found.
     */
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with ID: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
