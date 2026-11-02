package com.securedesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la création d'un nouveau ticket de support.
 * 
 * Permet de valider les données du formulaire avant
 * la création effective du ticket en base de données.
 */
public class TicketCreateDto {

    @NotBlank(message = "Le titre du ticket est obligatoire")
    @Size(min = 5, max = 200, message = "Le titre doit contenir entre 5 et 200 caractères")
    private String title;

    @NotBlank(message = "La description du ticket est obligatoire")
    @Size(min = 10, max = 5000, message = "La description doit contenir entre 10 et 5000 caractères")
    private String description;

    @NotBlank(message = "La priorité est obligatoire")
    private String priority;

    @NotNull(message = "La catégorie est obligatoire")
    private Long categoryId;

    // --- Constructeurs ---

    public TicketCreateDto() {
    }

    public TicketCreateDto(String title, String description, String priority, Long categoryId) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.categoryId = categoryId;
    }

    // --- Getters et Setters ---

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
