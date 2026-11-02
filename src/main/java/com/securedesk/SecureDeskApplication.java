package com.securedesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principale de l'application SecureDesk.
 * <p>
 * SecureDesk est une application HelpDesk sécurisée permettant la gestion
 * de tickets d'assistance technique avec trois rôles : CLIENT, AGENT et ADMIN.
 * </p>
 *
 * @author SecureDesk Team
 * @version 1.0.0
 */
@SpringBootApplication
public class SecureDeskApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureDeskApplication.class, args);
    }
}
