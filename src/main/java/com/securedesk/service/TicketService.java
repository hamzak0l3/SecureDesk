package com.securedesk.service;

import com.securedesk.dto.DashboardStats;
import com.securedesk.entity.Role;
import com.securedesk.entity.Ticket;
import com.securedesk.entity.TicketStatus;
import com.securedesk.entity.User;
import com.securedesk.repository.CategoryRepository;
import com.securedesk.repository.TicketRepository;
import com.securedesk.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de gestion des tickets.
 * Gère le cycle de vie complet des tickets :
 * OPEN → IN_PROGRESS → RESOLVED → CLOSED
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EntityManager entityManager;

    /**
     * Récupère tous les tickets triés par date de création décroissante.
     *
     * @return la liste de tous les tickets
     */
    @Transactional(readOnly = true)
    public List<Ticket> findAll() {
        return ticketRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Recherche un ticket par son identifiant.
     *
     * @param id l'identifiant du ticket
     * @return le ticket trouvé
     * @throws RuntimeException si le ticket n'existe pas
     */
    @Transactional(readOnly = true)
    public Ticket findById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Ticket non trouvé avec l'id : {}", id);
                    return new RuntimeException("Ticket non trouvé avec l'id : " + id);
                });
    }

    /**
     * Récupère tous les tickets créés par un utilisateur donné.
     *
     * @param user l'utilisateur créateur des tickets
     * @return la liste des tickets de l'utilisateur
     */
    @Transactional(readOnly = true)
    public List<Ticket> findByCreatedBy(User user) {
        return ticketRepository.findByCreatedBy(user);
    }

    /**
     * Récupère tous les tickets assignés à un agent donné.
     *
     * @param user l'agent assigné
     * @return la liste des tickets assignés à l'agent
     */
    @Transactional(readOnly = true)
    public List<Ticket> findByAssignedTo(User user) {
        return ticketRepository.findByAssignedTo(user);
    }

    /**
     * Récupère tous les tickets ayant un statut donné.
     *
     * @param status le statut recherché
     * @return la liste des tickets avec ce statut
     */
    @Transactional(readOnly = true)
    public List<Ticket> findByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status);
    }

    /**
     * Crée un nouveau ticket avec le statut OPEN.
     *
     * @param ticket  le ticket à créer
     * @param creator l'utilisateur qui crée le ticket
     * @return le ticket sauvegardé
     */
    public Ticket createTicket(Ticket ticket, User creator) {
        log.info("Création d'un nouveau ticket par l'utilisateur : {} - Titre : {}",
                creator.getUsername(), ticket.getTitle());

        // Initialiser les champs du ticket
        ticket.setCreatedBy(creator);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Ticket créé avec succès : id={}, titre={}", savedTicket.getId(), savedTicket.getTitle());
        return savedTicket;
    }

    /**
     * Crée un nouveau ticket en utilisant le créateur déjà attaché au ticket.
     *
     * @param ticket le ticket à créer
     * @return le ticket sauvegardé
     */
    public Ticket createTicket(Ticket ticket) {
        if (ticket.getCreatedBy() == null) {
            throw new IllegalArgumentException("Le créateur du ticket est obligatoire");
        }
        return createTicket(ticket, ticket.getCreatedBy());
    }

    /**
     * Met à jour les informations d'un ticket existant.
     * Seuls le titre, la description, la priorité et la catégorie sont modifiables.
     *
     * @param id            l'identifiant du ticket à modifier
     * @param updatedTicket les nouvelles données du ticket
     * @return le ticket mis à jour
     */
    public Ticket updateTicket(Long id, Ticket updatedTicket) {
        Ticket existingTicket = findById(id);

        log.info("Mise à jour du ticket : id={}, titre={}", id, existingTicket.getTitle());

        // Mettre à jour les champs autorisés
        existingTicket.setTitle(updatedTicket.getTitle());
        existingTicket.setDescription(updatedTicket.getDescription());
        existingTicket.setPriority(updatedTicket.getPriority());
        existingTicket.setCategory(updatedTicket.getCategory());
        existingTicket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(existingTicket);
        log.info("Ticket mis à jour avec succès : id={}", savedTicket.getId());
        return savedTicket;
    }

    /**
     * Assigne un ticket à un agent et passe le statut à IN_PROGRESS.
     *
     * @param ticketId l'identifiant du ticket
     * @param agentId  l'identifiant de l'agent à assigner
     * @return le ticket mis à jour
     */
    public Ticket assignTicket(Long ticketId, Long agentId) {
        Ticket ticket = findById(ticketId);
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> {
                    log.error("Agent non trouvé avec l'id : {}", agentId);
                    return new RuntimeException("Agent non trouvé avec l'id : " + agentId);
                });

        log.info("Assignation du ticket id={} à l'agent : {} (id={})",
                ticketId, agent.getUsername(), agentId);

        ticket.setAssignedTo(agent);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Ticket id={} assigné avec succès à l'agent : {}", ticketId, agent.getUsername());
        return savedTicket;
    }

    /**
     * Assigne un ticket en fournissant directement l'objet User de l'agent.
     */
    public Ticket assignTicket(Long ticketId, User agent) {
        if (agent == null || agent.getId() == null) {
            throw new IllegalArgumentException("L'agent à assigner est obligatoire");
        }
        return assignTicket(ticketId, agent.getId());
    }

    /**
     * Change le statut d'un ticket.
     * Si le nouveau statut est CLOSED, la date de fermeture est enregistrée.
     *
     * @param ticketId  l'identifiant du ticket
     * @param newStatus le nouveau statut
     * @return le ticket mis à jour
     */
    public Ticket changeStatus(Long ticketId, TicketStatus newStatus) {
        Ticket ticket = findById(ticketId);

        log.info("Changement de statut du ticket id={} : {} → {}",
                ticketId, ticket.getStatus(), newStatus);

        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());

        // Enregistrer la date de fermeture si le ticket est clos
        if (newStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
            log.info("Ticket id={} fermé à : {}", ticketId, ticket.getClosedAt());
        }

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Statut du ticket id={} changé avec succès en : {}", ticketId, newStatus);
        return savedTicket;
    }

    /**
     * Alias pour la mise à jour du statut.
     */
    public Ticket updateStatus(Long ticketId, TicketStatus newStatus) {
        return changeStatus(ticketId, newStatus);
    }

    /**
     * Recherche des tickets par titre.
     *
     * [V1] Concaténation SQL volontaire — laboratoire Phase 3 uniquement.
     * Ne pas réutiliser ce motif hors environnement académique contrôlé.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Ticket> searchTickets(String query) {
        String sql = "SELECT * FROM tickets WHERE title LIKE '%" + query + "%' OR description LIKE '%" + query + "%'";
        log.debug("Recherche SQL native (V1) : {}", sql);
        return entityManager.createNativeQuery(sql, Ticket.class).getResultList();
    }

    /**
     * Recherche globale sans filtrage par rôle (V1).
     */
    @Transactional(readOnly = true)
    public List<Ticket> search(String query, User user) {
        log.debug("Recherche (V1) par {} — aucun filtrage d'autorisation appliqué", user.getUsername());
        return searchTickets(query);
    }

    /**
     * Compte le nombre de tickets ayant un statut donné.
     *
     * @param status le statut recherché
     * @return le nombre de tickets
     */
    @Transactional(readOnly = true)
    public long countByStatus(TicketStatus status) {
        return ticketRepository.countByStatus(status);
    }

    /**
     * Compte le nombre de tickets créés par un utilisateur donné.
     *
     * @param user l'utilisateur créateur
     * @return le nombre de tickets
     */
    @Transactional(readOnly = true)
    public long countByUser(User user) {
        return ticketRepository.countByCreatedBy(user);
    }

    /**
     * Génère les statistiques globales des tickets.
     * Retourne un Map contenant le nombre de tickets par statut et le total.
     *
     * @return les statistiques sous forme de Map
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getStatistics() {
        log.debug("Calcul des statistiques des tickets");

        Map<String, Long> statistics = new LinkedHashMap<>();

        // Comptage par statut
        for (TicketStatus status : TicketStatus.values()) {
            statistics.put(status.name(), countByStatus(status));
        }

        // Total de tous les tickets
        statistics.put("TOTAL", ticketRepository.count());

        log.debug("Statistiques calculées : {}", statistics);
        return statistics;
    }

    /**
     * Calcule et retourne les métriques globales pour le tableau de bord administrateur.
     */
    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        return DashboardStats.builder()
                .totalTickets(ticketRepository.count())
                .openTickets(countByStatus(TicketStatus.OPEN))
                .inProgressTickets(countByStatus(TicketStatus.IN_PROGRESS))
                .resolvedTickets(countByStatus(TicketStatus.RESOLVED))
                .closedTickets(countByStatus(TicketStatus.CLOSED))
                .totalUsers(userRepository.count())
                .totalAgents(userRepository.findByRole(Role.ROLE_AGENT).size())
                .totalCategories(categoryRepository.count())
                .build();
    }
}
