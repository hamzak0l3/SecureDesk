package com.securedesk.repository;

import com.securedesk.entity.Comment;
import com.securedesk.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Spring Data JPA pour l'entité {@link Comment}.
 * Fournit les opérations CRUD et les requêtes personnalisées
 * pour la gestion des commentaires sur les tickets.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Récupère tous les commentaires d'un ticket.
     *
     * @param ticket le ticket dont on veut les commentaires
     * @return la liste des commentaires
     */
    List<Comment> findByTicket(Ticket ticket);

    /**
     * Récupère tous les commentaires d'un ticket, triés par date de création croissante.
     *
     * @param ticket le ticket dont on veut les commentaires
     * @return la liste ordonnée des commentaires
     */
    List<Comment> findByTicketOrderByCreatedAtAsc(Ticket ticket);

    /**
     * Compte le nombre de commentaires associés à un ticket.
     *
     * @param ticket le ticket à compter
     * @return le nombre de commentaires
     */
    long countByTicket(Ticket ticket);
}
