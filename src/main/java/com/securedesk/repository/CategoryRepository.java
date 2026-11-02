package com.securedesk.repository;

import com.securedesk.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data JPA pour l'entité {@link Category}.
 * Fournit les opérations CRUD et les requêtes personnalisées
 * pour la gestion des catégories de tickets.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Recherche une catégorie par son nom.
     *
     * @param name le nom de la catégorie
     * @return la catégorie correspondante, si elle existe
     */
    Optional<Category> findByName(String name);

    /**
     * Récupère toutes les catégories actives.
     *
     * @return la liste des catégories actives
     */
    List<Category> findByActiveTrue();

    /**
     * Vérifie si une catégorie portant ce nom existe déjà.
     *
     * @param name le nom à vérifier
     * @return true si le nom est déjà utilisé
     */
    boolean existsByName(String name);
}
