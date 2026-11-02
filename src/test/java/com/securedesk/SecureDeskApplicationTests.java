package com.securedesk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de chargement du contexte applicatif Spring Boot (avec base H2 de test).
 */
@SpringBootTest
@ActiveProfiles("test")
class SecureDeskApplicationTests {

    @Test
    @DisplayName("Le contexte applicatif Spring Boot doit se charger sans erreur")
    void contextLoads() {
        // Vérifie l'injection de dépendances et l'initialisation du contexte Spring Boot
    }
}

