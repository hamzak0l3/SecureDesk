package com.securedesk.service;

import com.securedesk.entity.Comment;
import com.securedesk.entity.Ticket;
import com.securedesk.entity.User;
import com.securedesk.repository.CommentRepository;
import com.securedesk.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service de gestion des commentaires.
 * Permet d'ajouter et de consulter les commentaires associés aux tickets.
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;

    /**
     * Récupère tous les commentaires d'un ticket donné.
     *
     * @param ticket le ticket dont on veut les commentaires
     * @return la liste des commentaires du ticket
     */
    @Transactional(readOnly = true)
    public List<Comment> findByTicket(Ticket ticket) {
        return commentRepository.findByTicket(ticket);
    }

    /**
     * Ajoute un commentaire à un ticket.
     * Met également à jour la date de dernière modification du ticket.
     *
     * @param ticketId l'identifiant du ticket
     * @param content  le contenu du commentaire
     * @param author   l'auteur du commentaire
     * @return le commentaire sauvegardé
     */
    public Comment addComment(Long ticketId, String content, User author) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.error("Ticket non trouvé avec l'id : {}", ticketId);
                    return new RuntimeException("Ticket non trouvé avec l'id : " + ticketId);
                });

        log.info("Ajout d'un commentaire au ticket id={} par l'utilisateur : {}",
                ticketId, author.getUsername());

        // Créer le commentaire
        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setContent(content);
        comment.setAuthor(author);
        comment.setCreatedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        // Mettre à jour la date de dernière modification du ticket
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        log.info("Commentaire ajouté avec succès au ticket id={} (commentaire id={})",
                ticketId, savedComment.getId());
        return savedComment;
    }

    /**
     * Ajoute directement une entité Comment et synchronise la date du ticket.
     *
     * @param comment le commentaire à ajouter
     * @return le commentaire sauvegardé
     */
    public Comment addComment(Comment comment) {
        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(LocalDateTime.now());
        }
        Comment savedComment = commentRepository.save(comment);

        if (comment.getTicket() != null) {
            comment.getTicket().setUpdatedAt(LocalDateTime.now());
            ticketRepository.save(comment.getTicket());
        }

        log.info("Commentaire sauvegardé avec l'id : {}", savedComment.getId());
        return savedComment;
    }

    /**
     * Compte le nombre de commentaires d'un ticket.
     *
     * @param ticket le ticket dont on veut compter les commentaires
     * @return le nombre de commentaires
     */
    @Transactional(readOnly = true)
    public long countByTicket(Ticket ticket) {
        return commentRepository.countByTicket(ticket);
    }
}
