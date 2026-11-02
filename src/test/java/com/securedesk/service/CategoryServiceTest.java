package com.securedesk.service;

import com.securedesk.entity.Category;
import com.securedesk.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CategoryService (CRUD, filtrage active, soft-delete).
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Réseau")
                .description("Problèmes de connectivité")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Création d'une catégorie")
    void shouldCreateCategory() {
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category created = categoryService.createCategory(category);

        assertThat(created.getName()).isEqualTo("Réseau");
        verify(categoryRepository).save(category);
    }

    @Test
    @DisplayName("Récupération des catégories actives uniquement")
    void shouldFindAllActive() {
        when(categoryRepository.findByActiveTrue()).thenReturn(List.of(category));

        List<Category> activeList = categoryService.findAllActive();

        assertThat(activeList).hasSize(1);
        assertThat(activeList.get(0).isActive()).isTrue();
    }

    @Test
    @DisplayName("Désactivation logique (soft-delete) d'une catégorie")
    void shouldSoftDeleteCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.softDeleteCategory(1L);

        assertThat(category.isActive()).isFalse();
        verify(categoryRepository).save(category);
    }
}

