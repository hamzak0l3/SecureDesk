package com.securedesk.controller;

import com.securedesk.dto.UserRegistrationDto;
import com.securedesk.entity.Role;
import com.securedesk.entity.User;
import com.securedesk.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Contrôleur d'authentification.
 * Gère la connexion, l'inscription et la déconnexion des utilisateurs.
 */
@Slf4j
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Affiche la page de connexion.
     */
    @GetMapping("/login")
    public String showLoginPage() {
        log.debug("Affichage de la page de connexion");
        return "auth/login";
    }

    /**
     * Affiche le formulaire d'inscription.
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        log.debug("Affichage du formulaire d'inscription");
        model.addAttribute("userRegistrationDto", new UserRegistrationDto());
        return "auth/register";
    }

    /**
     * Traite la soumission du formulaire d'inscription.
     * Vérifie la validité des données, la correspondance des mots de passe,
     * et l'unicité du nom d'utilisateur et de l'email.
     *
     * @param dto            les données d'inscription saisies par l'utilisateur
     * @param bindingResult  résultat de la validation
     * @param model          le modèle pour la vue
     * @param redirectAttributes attributs pour les messages flash après redirection
     * @return redirection vers /login en cas de succès, ou retour au formulaire en cas d'erreur
     */
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("userRegistrationDto") UserRegistrationDto dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.info("Tentative d'inscription pour l'utilisateur : {}", dto.getUsername());

        // Vérification des erreurs de validation Jakarta Bean Validation
        if (bindingResult.hasErrors()) {
            log.warn("Erreurs de validation détectées lors de l'inscription");
            return "auth/register";
        }

        // Vérifier la correspondance des mots de passe
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            log.warn("Les mots de passe ne correspondent pas pour l'utilisateur : {}", dto.getUsername());
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword",
                    "Les mots de passe ne correspondent pas");
            return "auth/register";
        }

        // Vérifier l'unicité du nom d'utilisateur
        if (userService.existsByUsername(dto.getUsername())) {
            log.warn("Nom d'utilisateur déjà pris : {}", dto.getUsername());
            bindingResult.rejectValue("username", "error.username",
                    "Ce nom d'utilisateur est déjà utilisé");
            return "auth/register";
        }

        // Vérifier l'unicité de l'adresse email
        if (userService.existsByEmail(dto.getEmail())) {
            log.warn("Adresse email déjà utilisée : {}", dto.getEmail());
            bindingResult.rejectValue("email", "error.email",
                    "Cette adresse email est déjà utilisée");
            return "auth/register";
        }

        try {
            // Créer l'utilisateur avec le rôle CLIENT par défaut
            userService.registerUser(dto, Role.ROLE_CLIENT);
            log.info("Utilisateur inscrit avec succès : {}", dto.getUsername());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            log.error("Erreur lors de l'inscription de l'utilisateur : {}", dto.getUsername(), e);
            model.addAttribute("errorMessage",
                    "Une erreur est survenue lors de l'inscription. Veuillez réessayer.");
            return "auth/register";
        }
    }
}
