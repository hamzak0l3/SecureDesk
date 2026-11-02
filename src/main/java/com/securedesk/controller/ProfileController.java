package com.securedesk.controller;

import com.securedesk.dto.PasswordChangeDto;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Contrôleur de gestion du profil utilisateur.
 * Permet à l'utilisateur de consulter son profil et de modifier son mot de passe.
 */
@Slf4j
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Affiche la page de profil de l'utilisateur connecté.
     */
    @GetMapping
    public String showProfile(Model model, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName());
        log.debug("Affichage du profil pour l'utilisateur : {}", currentUser.getUsername());

        model.addAttribute("user", currentUser);
        model.addAttribute("passwordChangeDto", new PasswordChangeDto());
        return "profile";
    }

    /**
     * Traite le changement de mot de passe.
     * Vérifie le mot de passe actuel, la correspondance du nouveau mot de passe
     * et sa confirmation, puis effectue la mise à jour.
     *
     * @param dto                les données du changement de mot de passe
     * @param bindingResult      résultat de la validation
     * @param principal          l'utilisateur authentifié
     * @param redirectAttributes attributs pour les messages flash
     * @return redirection vers la page de profil
     */
    @PostMapping("/password")
    public String changePassword(
            @Valid @ModelAttribute("passwordChangeDto") PasswordChangeDto dto,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User currentUser = userService.findByUsername(principal.getName());
        log.info("Tentative de changement de mot de passe pour l'utilisateur : {}",
                currentUser.getUsername());

        // Vérification des erreurs de validation
        if (bindingResult.hasErrors()) {
            log.warn("Erreurs de validation lors du changement de mot de passe");
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Veuillez remplir correctement tous les champs du formulaire.");
            return "redirect:/profile";
        }

        // Vérifier que le mot de passe actuel est correct
        if (!userService.checkPassword(currentUser, dto.getCurrentPassword())) {
            log.warn("Mot de passe actuel incorrect pour l'utilisateur : {}",
                    currentUser.getUsername());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Le mot de passe actuel est incorrect.");
            return "redirect:/profile";
        }

        // Vérifier la correspondance du nouveau mot de passe et de la confirmation
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            log.warn("Les nouveaux mots de passe ne correspondent pas pour l'utilisateur : {}",
                    currentUser.getUsername());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Le nouveau mot de passe et sa confirmation ne correspondent pas.");
            return "redirect:/profile";
        }

        try {
            userService.changePassword(currentUser, dto.getNewPassword());
            log.info("Mot de passe changé avec succès pour l'utilisateur : {}",
                    currentUser.getUsername());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Votre mot de passe a été modifié avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors du changement de mot de passe pour l'utilisateur : {}",
                    currentUser.getUsername(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Une erreur est survenue lors de la modification du mot de passe.");
        }

        return "redirect:/profile";
    }
}
