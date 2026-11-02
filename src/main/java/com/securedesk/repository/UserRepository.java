package com.securedesk.repository;

import com.securedesk.entity.Role;
import com.securedesk.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data JPA pour l'entité {@link User}.
 * Fournit les opérations CRUD et les requêtes personnalisées
 * pour la gestion des utilisateurs.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par son nom d'utilisateur.
     *
     * @param username le nom d'utilisateur
     * @return l'utilisateur correspondant, s'il existe
     */
    Optional<User> findByUsername(String username);

    /**
     * Recherche un utilisateur par son adresse e-mail.
     *
     * @param email l'adresse e-mail
     * @return l'utilisateur correspondant, s'il existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si un nom d'utilisateur est déjà utilisé.
     *
     * @param username le nom d'utilisateur à vérifier
     * @return true si le nom d'utilisateur existe déjà
     */
    boolean existsByUsername(String username);

    /**
     * Vérifie si une adresse e-mail est déjà utilisée.
     *
     * @param email l'adresse e-mail à vérifier
     * @return true si l'adresse e-mail existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Récupère tous les utilisateurs ayant un rôle donné.
     *
     * @param role le rôle recherché
     * @return la liste des utilisateurs correspondants
     */
    List<User> findByRole(Role role);

    /**
     * Récupère tous les utilisateurs dont le compte est actif.
     *
     * @return la liste des utilisateurs actifs
     */
    List<User> findByActiveTrue();
}
