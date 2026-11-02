package com.securedesk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour le formulaire d'inscription d'un nouvel utilisateur.
 * 
 * Contient les validations nécessaires pour garantir
 * l'intégrité des données saisies par l'utilisateur.
 */
public class UserRegistrationDto {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, max = 100, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmPassword;

    @NotBlank(message = "L'adresse email est obligatoire")
    @Email(message = "L'adresse email doit être valide")
    private String email;

    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom complet doit contenir entre 2 et 100 caractères")
    private String fullName;

    // --- Constructeurs ---

    public UserRegistrationDto() {
    }

    public UserRegistrationDto(String username, String password, String confirmPassword,
                               String email, String fullName) {
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.email = email;
        this.fullName = fullName;
    }

    // --- Getters et Setters ---

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
