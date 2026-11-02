package com.securedesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour l'ajout d'un commentaire à un ticket.
 */
public class CommentCreateDto {

    @NotBlank(message = "Le contenu du commentaire est obligatoire")
    @Size(max = 5000, message = "Le commentaire ne doit pas dépasser 5000 caractères")
    private String content;

    // --- Constructeurs ---

    public CommentCreateDto() {
    }

    public CommentCreateDto(String content) {
        this.content = content;
    }

    // --- Getters et Setters ---

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
