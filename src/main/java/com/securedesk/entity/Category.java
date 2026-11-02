package com.securedesk.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant une catégorie de tickets.
 * Permet de classifier et d'organiser les demandes de support.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom unique de la catégorie */
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    private String name;

    /** Description détaillée de la catégorie */
    @Column
    private String description;

    /** Indique si la catégorie est active et disponible à la sélection */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    // ─── Relations ──────────────────────────────────────────────

    /** Tickets associés à cette catégorie */
    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();
}
