package com.securedesk.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un ticket de support dans SecureDesk.
 * Cycle de vie : OPEN → IN_PROGRESS → RESOLVED → CLOSED
 */
@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Titre résumant la demande */
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 200, message = "Le titre doit contenir entre 5 et 200 caractères")
    private String title;

    /** Description détaillée du problème ou de la demande */
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "La description est obligatoire")
    private String description;

    /** Statut actuel du ticket dans son cycle de vie */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;

    /** Niveau de priorité du ticket */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TicketPriority priority = TicketPriority.MEDIUM;

    // ─── Relations ──────────────────────────────────────────────

    /** Client ayant créé le ticket */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    /** Agent assigné au traitement du ticket (peut être null) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    /** Catégorie du ticket (peut être null) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /** Commentaires associés au ticket, triés par date de création */
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    // ─── Horodatages ────────────────────────────────────────────

    /** Date de création du ticket */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Date de dernière modification du ticket */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /** Date de fermeture du ticket (null si non fermé) */
    @Column
    private LocalDateTime closedAt;

    // ─── Callbacks JPA ──────────────────────────────────────────

    /** Initialise les horodatages avant la première persistance */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /** Met à jour l'horodatage avant chaque modification */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
