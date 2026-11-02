package com.securedesk.service;

import com.securedesk.entity.Category;
import com.securedesk.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des catégories de tickets.
 * Permet la création, modification et suppression logique des catégories.
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Récupère toutes les catégories (actives et inactives).
     *
     * @return la liste complète des catégories
     */
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    /**
     * Récupère uniquement les catégories actives.
     * Utilisé pour les formulaires de création/édition de tickets.
     *
     * @return la liste des catégories actives
     */
    @Transactional(readOnly = true)
    public List<Category> findAllActive() {
        return categoryRepository.findByActiveTrue();
    }

    /**
     * Recherche une catégorie par son identifiant.
     *
     * @param id l'identifiant de la catégorie
     * @return la catégorie trouvée
     * @throws RuntimeException si la catégorie n'existe pas
     */
    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Catégorie non trouvée avec l'id : {}", id);
                    return new RuntimeException("Catégorie non trouvée avec l'id : " + id);
                });
    }

    /**
     * Crée une nouvelle catégorie.
     *
     * @param category la catégorie à créer
     * @return la catégorie sauvegardée
     */
    public Category createCategory(Category category) {
        log.info("Création d'une nouvelle catégorie : {}", category.getName());

        Category savedCategory = categoryRepository.save(category);
        log.info("Catégorie créée avec succès : {} (id : {})", savedCategory.getName(), savedCategory.getId());
        return savedCategory;
    }

    /**
     * Met à jour une catégorie existante.
     * Seuls le nom et la description sont modifiables.
     *
     * @param id              l'identifiant de la catégorie à modifier
     * @param updatedCategory les nouvelles données de la catégorie
     * @return la catégorie mise à jour
     */
    public Category updateCategory(Long id, Category updatedCategory) {
        Category existingCategory = findById(id);

        log.info("Mise à jour de la catégorie : {} (id : {})", existingCategory.getName(), id);

        // Mettre à jour les champs autorisés
        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(updatedCategory.getDescription());

        Category savedCategory = categoryRepository.save(existingCategory);
        log.info("Catégorie mise à jour avec succès : {} (id : {})", savedCategory.getName(), id);
        return savedCategory;
    }

    /**
     * Sauvegarde directement les modifications d'une catégorie.
     */
    public Category updateCategory(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * Suppression logique d'une catégorie (désactivation).
     * La catégorie n'est pas supprimée physiquement de la base de données.
     *
     * @param id l'identifiant de la catégorie à désactiver
     */
    public void deleteCategory(Long id) {
        Category category = findById(id);
        log.info("Suppression logique de la catégorie : {} (id : {})", category.getName(), id);

        category.setActive(false);
        categoryRepository.save(category);

        log.info("Catégorie désactivée avec succès : {}", category.getName());
    }

    /**
     * Alias pour la suppression logique d'une catégorie.
     */
    public void softDeleteCategory(Long id) {
        deleteCategory(id);
    }

    /**
     * Compte le nombre total de catégories.
     */
    @Transactional(readOnly = true)
    public long countAll() {
        return categoryRepository.count();
    }

    /**
     * Vérifie si une catégorie avec ce nom existe déjà.
     *
     * @param name le nom de la catégorie à vérifier
     * @return true si le nom est déjà utilisé
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}
