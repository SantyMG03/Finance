package com.santy.finances.services;

import com.santy.finances.exceptions.ResourceNotFoundException;
import com.santy.finances.models.Category;
import com.santy.finances.models.User;
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
     * Retrieves all categories belonging to the given user.
     *
     * @param user The user whose categories should be retrieved.
     * @return A list containing all the user's stored categories.
     */
    @Transactional(readOnly = true)
    public List<Category> getUserCategories(User user) {
        return categoryRepository.findByUser(user);
    }

    /**
     * Saves a new category into the database for the given user.
     *
     * @param c The category entity to save.
     * @param user The user that owns the category.
     * @return The saved category entity.
     */
    @Transactional
    public Category registerCategory(Category c, User user) {
        c.setUser(user);
        return categoryRepository.save(c);
    }

    /**
     * Searches for a category owned by the user, by its ID, and updates it with new data.
     *
     * @param id The ID of the category to update.
     * @param updatedData The new category data to overwrite the existing one.
     * @param user The user that owns the category.
     * @return The updated and saved category entity.
     * @throws ResourceNotFoundException if the category ID is not found for the user.
     */
    @Transactional
    public Category updateCategory(Long id, Category updatedData, User user) {
        return categoryRepository.findByIdAndUser(id, user).map(existingCategory -> {
            existingCategory.setName(updatedData.getName());
            return categoryRepository.save(existingCategory);
        }).orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    }

    /**
     * Deletes a category owned by the user, by its ID.
     *
     * @param id The ID of the category to be removed.
     * @param user The user that owns the category.
     * @throws ResourceNotFoundException if the category ID is not found for the user.
     */
    @Transactional
    public void deleteCategory(Long id, User user) {
        if (!categoryRepository.existsByIdAndUser(id, user)) {
            throw new ResourceNotFoundException("Category not found with ID: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
