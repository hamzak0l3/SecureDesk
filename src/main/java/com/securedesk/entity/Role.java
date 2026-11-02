package com.securedesk.entity;

/**
 * Énumération des rôles utilisateur dans l'application SecureDesk.
 * Chaque rôle détermine les permissions et l'accès aux fonctionnalités.
 */
public enum Role {

    /** Client qui crée et suit ses tickets */
    ROLE_CLIENT,

    /** Agent du support qui traite les tickets */
    ROLE_AGENT,

    /** Administrateur avec accès complet au système */
    ROLE_ADMIN
}
