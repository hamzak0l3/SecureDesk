package com.securedesk.repository;

import com.securedesk.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Spring Data JPA pour l'entité {@link Ticket}.
 * Fournit les opérations CRUD et les requêtes personnalisées
 * pour la gestion des tickets de support.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Récupère tous les tickets créés par un utilisateur donné.
     *
     * @param createdBy l'utilisateur ayant créé les tickets
     * @return la liste des tickets de cet utilisateur
     */
    List<Ticket> findByCreatedBy(User createdBy);

    /**
     * Récupère tous les tickets assignés à un agent donné.
     *
     * @param assignedTo l'agent assigné
     * @return la liste des tickets assignés à cet agent
     */
    List<Ticket> findByAssignedTo(User assignedTo);

    /**
     * Récupère tous les tickets ayant un statut donné.
     *
     * @param status le statut recherché
     * @return la liste des tickets correspondants
     */
    List<Ticket> findByStatus(TicketStatus status);

    /**
     * Récupère tous les tickets d'une catégorie donnée.
     *
     * @param category la catégorie recherchée
     * @return la liste des tickets de cette catégorie
     */
    List<Ticket> findByCategory(Category category);

    /**
     * Récupère les tickets d'un utilisateur filtrés par statut.
     *
     * @param createdBy l'utilisateur ayant créé les tickets
     * @param status    le statut recherché
     * @return la liste des tickets correspondants
     */
    List<Ticket> findByCreatedByAndStatus(User createdBy, TicketStatus status);

    /**
     * Recherche les tickets dont le titre contient la chaîne donnée
     * (insensible à la casse).
     *
     * @param title le terme de recherche
     * @return la liste des tickets correspondants
     */
    List<Ticket> findByTitleContainingIgnoreCase(String title);

    /**
     * Recherche les tickets d'un créateur dont le titre contient la chaîne donnée.
     */
    List<Ticket> findByCreatedByAndTitleContainingIgnoreCase(User createdBy, String title);

    /**
     * Recherche les tickets d'un agent dont le titre contient la chaîne donnée.
     */
    List<Ticket> findByAssignedToAndTitleContainingIgnoreCase(User assignedTo, String title);

    List<Ticket> searchByTitleContainingIgnoreCase(String title);

    /**
     * Compte le nombre de tickets ayant un statut donné.
     *
     * @param status le statut à compter
     * @return le nombre de tickets correspondants
     */
    long countByStatus(TicketStatus status);

    /**
     * Compte le nombre de tickets créés par un utilisateur donné.
     *
     * @param createdBy l'utilisateur à compter
     * @return le nombre de tickets créés par cet utilisateur
     */
    long countByCreatedBy(User createdBy);

    /**
     * Récupère tous les tickets triés par date de création décroissante.
     *
     * @return la liste complète des tickets, du plus récent au plus ancien
     */
    List<Ticket> findAllByOrderByCreatedAtDesc();
}
