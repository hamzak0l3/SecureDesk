package com.securedesk.config;

import com.securedesk.entity.Category;
import com.securedesk.entity.Role;
import com.securedesk.entity.User;
import com.securedesk.repository.CategoryRepository;
import com.securedesk.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialisation des données au démarrage de l'application.
 * 
 * Crée les utilisateurs par défaut (admin, agent, client) et
 * les catégories de tickets si la base de données est vide.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Vérifier si l'administrateur existe déjà
        if (userRepository.findByUsername("admin").isPresent()) {
            log.info("Les données initiales existent déjà, initialisation ignorée.");
            return;
        }

        log.info("=== Initialisation des données de SecureDesk ===");

        // --- Création des utilisateurs par défaut ---
        createUser("admin", "admin123", "admin@securedesk.local",
                "Administrateur", Role.ROLE_ADMIN);
        createUser("agent", "agent123", "agent@securedesk.local",
                "Agent Support", Role.ROLE_AGENT);
        createUser("agent1", "agent123", "agent1@securedesk.local",
                "Agent Support 1", Role.ROLE_AGENT);
        createUser("client", "client123", "client@securedesk.local",
                "Client Test", Role.ROLE_CLIENT);
        createUser("client1", "client123", "client1@securedesk.local",
                "Client Test 1", Role.ROLE_CLIENT);

        // --- Création des catégories par défaut ---
        createCategory("Réseau", "Problèmes réseau et connectivité");
        createCategory("Logiciel", "Problèmes logiciels et applications");
        createCategory("Matériel", "Problèmes matériels et équipements");

        log.info("=== Initialisation des données terminée avec succès ===");
    }

    /**
     * Crée un utilisateur avec le rôle spécifié et le mot de passe encodé en BCrypt.
     */
    private void createUser(String username, String rawPassword, String email,
                            String fullName, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(role);
        user.setActive(true);

        userRepository.save(user);
        log.info("Utilisateur créé : {} ({})", username, role);
    }

    /**
     * Crée une catégorie de tickets.
     */
    private void createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);

        categoryRepository.save(category);
        log.info("Catégorie créée : {}", name);
    }
}
