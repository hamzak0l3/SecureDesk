package com.securedesk.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un utilisateur du système SecureDesk.
 * Un utilisateur peut être un client, un agent ou un administrateur.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom d'utilisateur unique pour l'authentification */
    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    private String username;

    /** Mot de passe hashé avec BCrypt */
    @Column(nullable = false)
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    /** Adresse e-mail unique de l'utilisateur */
    @Column(unique = true, nullable = false)
    @NotBlank(message = "L'adresse e-mail est obligatoire")
    @Email(message = "L'adresse e-mail doit être valide")
    private String email;

    /** Nom complet affiché dans l'interface */
    @Column(nullable = false)
    @NotBlank(message = "Le nom complet est obligatoire")
    private String fullName;

    /** Rôle déterminant les permissions de l'utilisateur */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** Indique si le compte est actif */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    /** Date de création du compte */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Date de dernière modification du compte */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ─── Relations ──────────────────────────────────────────────

    /** Tickets créés par cet utilisateur (client) */
    @OneToMany(mappedBy = "createdBy")
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();

    /** Tickets assignés à cet utilisateur (agent) */
    @OneToMany(mappedBy = "assignedTo")
    @Builder.Default
    private List<Ticket> assignedTickets = new ArrayList<>();

    /** Commentaires rédigés par cet utilisateur */
    @OneToMany(mappedBy = "author")
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

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
