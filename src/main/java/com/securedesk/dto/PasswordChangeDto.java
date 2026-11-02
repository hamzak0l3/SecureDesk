package com.securedesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour le formulaire de changement de mot de passe.
 * 
 * Contient le mot de passe actuel pour vérification,
 * ainsi que le nouveau mot de passe et sa confirmation.
 */
public class PasswordChangeDto {

    @NotBlank(message = "Le mot de passe actuel est obligatoire")
    private String currentPassword;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 6, max = 100, message = "Le nouveau mot de passe doit contenir au moins 6 caractères")
    private String newPassword;

    @NotBlank(message = "La confirmation du nouveau mot de passe est obligatoire")
    private String confirmPassword;

    // --- Constructeurs ---

    public PasswordChangeDto() {
    }

    public PasswordChangeDto(String currentPassword, String newPassword, String confirmPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    // --- Getters et Setters ---

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getConfirmNewPassword() {
        return confirmPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmPassword = confirmNewPassword;
    }
}
