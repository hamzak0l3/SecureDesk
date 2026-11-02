package com.securedesk.service;

import com.securedesk.dto.UserRegistrationDto;
import com.securedesk.entity.User;
import com.securedesk.entity.Role;
import com.securedesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des utilisateurs.
 * Gère la création, modification, suppression (logique) et recherche
 * des utilisateurs de l'application SecureDesk.
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Récupère la liste de tous les utilisateurs.
     *
     * @return la liste complète des utilisateurs
     */
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Recherche un utilisateur par son identifiant.
     *
     * @param id l'identifiant de l'utilisateur
     * @return l'utilisateur trouvé
     * @throws RuntimeException si l'utilisateur n'existe pas
     */
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé avec l'id : {}", id);
                    return new RuntimeException("Utilisateur non trouvé avec l'id : " + id);
                });
    }

    /**
     * Recherche un utilisateur par son nom d'utilisateur.
     *
     * @param username le nom d'utilisateur
     * @return l'utilisateur trouvé
     * @throws RuntimeException si l'utilisateur n'existe pas
     */
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec le nom : " + username));
    }

    /**
     * Recherche un utilisateur par son nom d'utilisateur sous forme d'Optional.
     *
     * @param username le nom d'utilisateur
     * @return un Optional contenant l'utilisateur s'il existe
     */
    @Transactional(readOnly = true)
    public Optional<User> findOptionalByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Crée un nouvel utilisateur avec le mot de passe encodé en BCrypt.
     *
     * @param user l'utilisateur à créer
     * @return l'utilisateur sauvegardé
     */
    public User createUser(User user) {
        log.info("Création d'un nouvel utilisateur : {} (rôle : {})", user.getUsername(), user.getRole());

        // Encoder le mot de passe avant la sauvegarde
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("Utilisateur créé avec succès : {} (id : {})", savedUser.getUsername(), savedUser.getId());
        return savedUser;
    }

    /**
     * Enregistre un nouvel utilisateur à partir des données du formulaire d'inscription.
     *
     * @param dto  les données d'inscription
     * @param role le rôle à assigner (ROLE_CLIENT par défaut)
     * @return l'utilisateur créé et persisté
     */
    public User registerUser(UserRegistrationDto dto, Role role) {
        log.info("Enregistrement d'un nouvel utilisateur : {} avec rôle : {}", dto.getUsername(), role);
        User user = User.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .role(role != null ? role : Role.ROLE_CLIENT)
                .active(true)
                .build();
        return createUser(user);
    }

    /**
     * Met à jour les informations d'un utilisateur existant.
     * Le mot de passe n'est PAS modifié par cette méthode.
     *
     * @param id          l'identifiant de l'utilisateur à modifier
     * @param updatedUser les nouvelles données de l'utilisateur
     * @return l'utilisateur mis à jour
     */
    public User updateUser(Long id, User updatedUser) {
        User existingUser = findById(id);

        log.info("Mise à jour de l'utilisateur : {} (id : {})", existingUser.getUsername(), id);

        // Mettre à jour les champs autorisés (le mot de passe est exclu)
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setRole(updatedUser.getRole());
        existingUser.setActive(updatedUser.isActive());

        User savedUser = userRepository.save(existingUser);
        log.info("Utilisateur mis à jour avec succès : {} (id : {})", savedUser.getUsername(), id);
        return savedUser;
    }

    /**
     * Sauvegarde directement les modifications d'un utilisateur existant.
     *
     * @param user l'utilisateur à sauvegarder
     * @return l'utilisateur mis à jour
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Change le mot de passe d'un utilisateur.
     * Le nouveau mot de passe est encodé en BCrypt.
     *
     * @param id          l'identifiant de l'utilisateur
     * @param newPassword le nouveau mot de passe en clair
     */
    public void changePassword(Long id, String newPassword) {
        User user = findById(id);
        log.info("Changement de mot de passe pour l'utilisateur : {} (id : {})", user.getUsername(), id);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Mot de passe changé avec succès pour l'utilisateur : {}", user.getUsername());
    }

    /**
     * Vérifie si le mot de passe brut correspond au mot de passe encodé de l'utilisateur.
     *
     * @param user        l'utilisateur
     * @param rawPassword le mot de passe en clair à vérifier
     * @return true si le mot de passe correspond
     */
    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /**
     * Change le mot de passe d'un utilisateur directement à partir de son entité.
     *
     * @param user        l'utilisateur
     * @param newPassword le nouveau mot de passe en clair
     */
    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Mot de passe changé avec succès pour l'utilisateur : {}", user.getUsername());
    }

    /**
     * Suppression logique d'un utilisateur (désactivation du compte).
     * L'utilisateur n'est pas supprimé physiquement de la base de données.
     *
     * @param id l'identifiant de l'utilisateur à désactiver
     */
    public void deleteUser(Long id) {
        User user = findById(id);
        log.info("Suppression logique de l'utilisateur : {} (id : {})", user.getUsername(), id);

        user.setActive(false);
        userRepository.save(user);

        log.info("Utilisateur désactivé avec succès : {}", user.getUsername());
    }

    /**
     * Alias pour la suppression logique d'un utilisateur.
     */
    public void softDeleteUser(Long id) {
        deleteUser(id);
    }

    /**
     * Vérifie si un nom d'utilisateur existe déjà.
     *
     * @param username le nom d'utilisateur à vérifier
     * @return true si le nom d'utilisateur est déjà pris
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Vérifie si une adresse email existe déjà.
     *
     * @param email l'adresse email à vérifier
     * @return true si l'email est déjà utilisé
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Recherche tous les utilisateurs ayant un rôle donné.
     *
     * @param role le rôle recherché
     * @return la liste des utilisateurs avec ce rôle
     */
    @Transactional(readOnly = true)
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    /**
     * Raccourci pour récupérer tous les agents du support.
     *
     * @return la liste des utilisateurs ayant le rôle ROLE_AGENT
     */
    @Transactional(readOnly = true)
    public List<User> findAgents() {
        return findByRole(Role.ROLE_AGENT);
    }

    /**
     * Compte le nombre total d'utilisateurs.
     *
     * @return le nombre d'utilisateurs
     */
    @Transactional(readOnly = true)
    public long countAll() {
        return userRepository.count();
    }
}
