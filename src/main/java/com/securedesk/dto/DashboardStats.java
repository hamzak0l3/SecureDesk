package com.securedesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO contenant les statistiques affichées sur le tableau de bord.
 * 
 * Regroupe les compteurs de tickets par statut ainsi que
 * les compteurs d'utilisateurs, agents et catégories.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStats {

    /** Nombre total de tickets dans le système */
    private long totalTickets;

    /** Nombre de tickets ouverts (statut OPEN) */
    private long openTickets;

    /** Nombre de tickets en cours de traitement (statut IN_PROGRESS) */
    private long inProgressTickets;

    /** Nombre de tickets résolus (statut RESOLVED) */
    private long resolvedTickets;

    /** Nombre de tickets fermés (statut CLOSED) */
    private long closedTickets;

    /** Nombre total d'utilisateurs enregistrés */
    private long totalUsers;

    /** Nombre total d'agents de support */
    private long totalAgents;

    /** Nombre total de catégories de tickets */
    private long totalCategories;
}
