package com.securedesk.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité représentant un commentaire sur un ticket.
 * Permet la communication entre clients et agents.
 */
@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Contenu textuel du commentaire */
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Le contenu du commentaire est obligatoire")
    private String content;

    // ─── Relations ──────────────────────────────────────────────

    /** Ticket auquel ce commentaire est rattaché */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /** Auteur du commentaire */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // ─── Horodatage ─────────────────────────────────────────────

    /** Date de création du commentaire */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ─── Callback JPA ───────────────────────────────────────────

    /** Initialise l'horodatage avant la première persistance */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
