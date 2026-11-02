package com.securedesk.entity;

/**
 * Énumération des niveaux de priorité d'un ticket.
 * Permet de classer les tickets par ordre d'urgence.
 */
public enum TicketPriority {

    /** Priorité basse — traitement non urgent */
    LOW("Basse"),

    /** Priorité moyenne — traitement dans les délais normaux */
    MEDIUM("Moyenne"),

    /** Priorité haute — traitement rapide requis */
    HIGH("Haute"),

    /** Priorité critique — traitement immédiat requis */
    CRITICAL("Critique");

    /** Libellé d'affichage en français */
    private final String displayLabel;

    TicketPriority(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    /**
     * Retourne le libellé d'affichage en français.
     *
     * @return le libellé localisé de la priorité
     */
    public String getDisplayLabel() {
        return displayLabel;
    }
}
