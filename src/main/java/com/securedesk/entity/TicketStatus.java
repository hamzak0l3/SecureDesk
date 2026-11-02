package com.securedesk.entity;

/**
 * Énumération représentant le cycle de vie d'un ticket.
 * Cycle : OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED
 */
public enum TicketStatus {

    /** Ticket ouvert, en attente de prise en charge */
    OPEN("Ouvert"),

    /** Ticket en cours de traitement par un agent */
    IN_PROGRESS("En cours"),

    /** Ticket résolu, en attente de confirmation du client */
    RESOLVED("Résolu"),

    /** Ticket fermé définitivement */
    CLOSED("Fermé");

    /** Libellé d'affichage en français */
    private final String displayLabel;

    TicketStatus(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    /**
     * Retourne le libellé d'affichage en français.
     *
     * @return le libellé localisé du statut
     */
    public String getDisplayLabel() {
        return displayLabel;
    }
}
