package com.securedesk.config;

import com.securedesk.entity.Category;
import com.securedesk.entity.Role;
import com.securedesk.entity.Ticket;
import com.securedesk.entity.TicketPriority;
import com.securedesk.entity.TicketStatus;
import com.securedesk.entity.User;
import com.securedesk.repository.CategoryRepository;
import com.securedesk.repository.TicketRepository;
import com.securedesk.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialisation des données au démarrage de l'application.
 *
 * Crée les utilisateurs par défaut, les catégories et deux tickets
 * de démonstration pour l'IDOR (V3) si la base est vide.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           TicketRepository ticketRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.ticketRepository = ticketRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            log.info("=== Initialisation des données de SecureDesk ===");
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
            createCategory("Réseau", "Problèmes réseau et connectivité");
            createCategory("Logiciel", "Problèmes logiciels et applications");
            createCategory("Matériel", "Problèmes matériels et équipements");
        } else {
            log.info("Les utilisateurs initiaux existent déjà.");
            if (userRepository.findByUsername("client1").isEmpty()) {
                createUser("client1", "client123", "client1@securedesk.local",
                        "Client Test 1", Role.ROLE_CLIENT);
            }
        }

        if (ticketRepository.count() == 0) {
            seedDemoTickets();
        }

        log.info("=== Initialisation des données terminée ===");
    }

    private void seedDemoTickets() {
        User client = userRepository.findByUsername("client").orElse(null);
        User client1 = userRepository.findByUsername("client1").orElse(null);
        User agent = userRepository.findByUsername("agent").orElse(null);
        Category reseau = categoryRepository.findAll().stream()
                .filter(c -> "Réseau".equals(c.getName()))
                .findFirst()
                .orElse(null);

        if (client == null || client1 == null) {
            return;
        }

        Ticket t1 = new Ticket();
        t1.setTitle("Connexion VPN instable");
        t1.setDescription("Le VPN du poste client coupe toutes les 10 minutes.");
        t1.setStatus(TicketStatus.OPEN);
        t1.setPriority(TicketPriority.HIGH);
        t1.setCreatedBy(client);
        t1.setAssignedTo(agent);
        t1.setCategory(reseau);
        ticketRepository.save(t1);

        Ticket t2 = new Ticket();
        t2.setTitle("Accès confidentiel — dossier RH client1");
        t2.setDescription("Données internes fictives de client1 pour démontrer l'IDOR (V3).");
        t2.setStatus(TicketStatus.OPEN);
        t2.setPriority(TicketPriority.CRITICAL);
        t2.setCreatedBy(client1);
        t2.setCategory(reseau);
        ticketRepository.save(t2);
        log.info("Tickets de démonstration IDOR créés (client / client1).");
    }

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

    private void createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setActive(true);

        categoryRepository.save(category);
        log.info("Catégorie créée : {}", name);
    }
}
