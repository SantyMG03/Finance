package com.santy.finances.services;

import com.santy.finances.exceptions.ResourceNotFoundException;
import com.santy.finances.models.Category;
import com.santy.finances.models.User;
import com.santy.finances.repositories.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("santy");
        return user;
    }

    private Category buildCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }

    @Test
    void getUserCategories_returnsCategoriesOfUser() {
        User user = buildUser();
        List<Category> categories = List.of(
                buildCategory(1L, "Ocio"),
                buildCategory(2L, "Alimentación"));
        when(categoryRepository.findByUser(user)).thenReturn(categories);

        List<Category> result = categoryService.getUserCategories(user);

        assertThat(result).isEqualTo(categories);
        verify(categoryRepository).findByUser(user);
    }

    @Test
    void registerCategory_setsUserAndSaves() {
        User user = buildUser();
        Category category = buildCategory(null, "Ocio");
        when(categoryRepository.save(category)).thenReturn(category);

        Category result = categoryService.registerCategory(category, user);

        assertThat(result).isSameAs(category);
        assertThat(category.getUser()).isSameAs(user);
        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_success_updatesAndSaves() {
        User user = buildUser();
        Category existing = buildCategory(1L, "Viejo");
        Category newData = buildCategory(1L, "Nuevo");
        when(categoryRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);

        Category result = categoryService.updateCategory(1L, newData, user);

        assertThat(result).isSameAs(existing);
        assertThat(existing.getName()).isEqualTo("Nuevo");
        verify(categoryRepository).save(existing);
    }

    @Test
    void updateCategory_throwsWhenCategoryNotOwned() {
        User user = buildUser();
        Category newData = buildCategory(99L, "Nuevo");
        when(categoryRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(99L, newData, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with ID: 99");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_success_deletesById() {
        User user = buildUser();
        when(categoryRepository.existsByIdAndUser(1L, user)).thenReturn(true);

        categoryService.deleteCategory(1L, user);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategory_throwsWhenCategoryNotOwned() {
        User user = buildUser();
        when(categoryRepository.existsByIdAndUser(1L, user)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.deleteCategory(1L, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with ID: 1");
        verify(categoryRepository, never()).deleteById(1L);
    }
}
