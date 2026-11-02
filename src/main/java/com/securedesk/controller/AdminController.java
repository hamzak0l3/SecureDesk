package com.securedesk.controller;

import com.securedesk.dto.DashboardStats;
import com.securedesk.entity.Category;
import com.securedesk.entity.Role;
import com.securedesk.entity.User;
import com.securedesk.service.CategoryService;
import com.securedesk.service.TicketService;
import com.securedesk.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * Contrôleur d'administration.
 * Réservé aux utilisateurs ayant le rôle ADMIN.
 * Gère le tableau de bord admin, la gestion des utilisateurs et des catégories.
 */
@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final TicketService ticketService;
    private final CategoryService categoryService;

    public AdminController(UserService userService,
                           TicketService ticketService,
                           CategoryService categoryService) {
        this.userService = userService;
        this.ticketService = ticketService;
        this.categoryService = categoryService;
    }

    /**
     * Affiche le tableau de bord de l'administrateur avec les statistiques globales.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName());
        log.info("Accès au tableau de bord admin par : {}", currentUser.getUsername());

        DashboardStats stats = ticketService.getDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("user", currentUser);
        return "admin/dashboard";
    }

    // ==================== Gestion des utilisateurs ====================

    /**
     * Liste tous les utilisateurs du système.
     */
    @GetMapping("/users")
    public String listUsers(Model model, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName());
        log.info("Liste des utilisateurs demandée par : {}", currentUser.getUsername());

        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("user", currentUser);
        return "admin/users";
    }

    /**
     * Affiche le formulaire de modification d'un utilisateur.
     *
     * @param id identifiant de l'utilisateur à modifier
     */
    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        User currentUser = userService.findByUsername(principal.getName());
        User userToEdit = userService.findById(id);

        if (userToEdit == null) {
            log.warn("Utilisateur introuvable avec l'identifiant : {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur introuvable.");
            return "redirect:/admin/users";
        }

        model.addAttribute("userToEdit", userToEdit);
        model.addAttribute("roles", Role.values());
        model.addAttribute("user", currentUser);
        log.debug("Affichage du formulaire de modification pour l'utilisateur : {}",
                userToEdit.getUsername());
        return "admin/user-edit";
    }

    /**
     * Traite la modification d'un utilisateur (nom d'utilisateur, email, nom complet,
     * rôle, et statut actif/inactif).
     *
     * @param id                 identifiant de l'utilisateur
     * @param username           nouveau nom d'utilisateur
     * @param email              nouvelle adresse email
     * @param fullName           nouveau nom complet
     * @param role               nouveau rôle
     * @param active             statut actif/inactif
     * @param redirectAttributes attributs pour les messages flash
     * @return redirection vers la liste des utilisateurs
     */
    @PostMapping("/users/{id}/edit")
    public String updateUser(
            @PathVariable Long id,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("fullName") String fullName,
            @RequestParam("role") Role role,
            @RequestParam(value = "active", defaultValue = "false") boolean active,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User currentUser = userService.findByUsername(principal.getName());
        log.info("Modification de l'utilisateur {} par l'admin {}", id, currentUser.getUsername());

        try {
            User userToUpdate = userService.findById(id);
            if (userToUpdate == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur introuvable.");
                return "redirect:/admin/users";
            }

            userToUpdate.setUsername(username);
            userToUpdate.setEmail(email);
            userToUpdate.setFullName(fullName);
            userToUpdate.setRole(role);
            userToUpdate.setActive(active);

            userService.updateUser(userToUpdate);
            log.info("Utilisateur {} mis à jour avec succès", username);
            redirectAttributes.addFlashAttribute("successMessage",
                    "L'utilisateur " + username + " a été mis à jour avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors de la modification de l'utilisateur {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur lors de la modification de l'utilisateur.");
        }

        return "redirect:/admin/users";
    }

    /**
     * Supprime (soft delete) un utilisateur.
     *
     * @param id                 identifiant de l'utilisateur à supprimer
     * @param redirectAttributes attributs pour les messages flash
     * @return redirection vers la liste des utilisateurs
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, Principal principal,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.findByUsername(principal.getName());
        log.info("Suppression de l'utilisateur {} par l'admin {}", id, currentUser.getUsername());

        try {
            userService.softDeleteUser(id);
            log.info("Utilisateur {} désactivé avec succès", id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "L'utilisateur a été désactivé avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'utilisateur {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur lors de la suppression de l'utilisateur.");
        }

        return "redirect:/admin/users";
    }

    // ==================== Gestion des catégories ====================

    /**
     * Liste toutes les catégories.
     */
    @GetMapping("/categories")
    public String listCategories(Model model, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName());
        log.debug("Liste des catégories demandée par : {}", currentUser.getUsername());

        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("user", currentUser);
        return "admin/categories";
    }

    /**
     * Crée une nouvelle catégorie.
     *
     * @param name               nom de la catégorie
     * @param description        description de la catégorie
     * @param redirectAttributes attributs pour les messages flash
     * @return redirection vers la liste des catégories
     */
    @PostMapping("/categories")
    public String createCategory(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User currentUser = userService.findByUsername(principal.getName());
        log.info("Création de la catégorie '{}' par l'admin {}", name, currentUser.getUsername());

        try {
            Category category = new Category();
            category.setName(name);
            category.setDescription(description);

            categoryService.createCategory(category);
            log.info("Catégorie '{}' créée avec succès", name);
            redirectAttributes.addFlashAttribute("successMessage",
                    "La catégorie '" + name + "' a été créée avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors de la création de la catégorie '{}'", name, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur lors de la création de la catégorie.");
        }

        return "redirect:/admin/categories";
    }

    /**
     * Met à jour une catégorie existante.
     *
     * @param id                 identifiant de la catégorie
     * @param name               nouveau nom
     * @param description        nouvelle description
     * @param redirectAttributes attributs pour les messages flash
     * @return redirection vers la liste des catégories
     */
    @PostMapping("/categories/{id}/edit")
    public String updateCategory(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User currentUser = userService.findByUsername(principal.getName());
        log.info("Modification de la catégorie {} par l'admin {}", id, currentUser.getUsername());

        try {
            Category category = categoryService.findById(id);
            if (category == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Catégorie introuvable.");
                return "redirect:/admin/categories";
            }

            category.setName(name);
            category.setDescription(description);

            categoryService.updateCategory(category);
            log.info("Catégorie '{}' mise à jour avec succès", name);
            redirectAttributes.addFlashAttribute("successMessage",
                    "La catégorie '" + name + "' a été mise à jour avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors de la modification de la catégorie {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur lors de la modification de la catégorie.");
        }

        return "redirect:/admin/categories";
    }

    /**
     * Supprime (soft delete) une catégorie.
     *
     * @param id                 identifiant de la catégorie à supprimer
     * @param redirectAttributes attributs pour les messages flash
     * @return redirection vers la liste des catégories
     */
    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, Principal principal,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = userService.findByUsername(principal.getName());
        log.info("Suppression de la catégorie {} par l'admin {}", id, currentUser.getUsername());

        try {
            categoryService.softDeleteCategory(id);
            log.info("Catégorie {} désactivée avec succès", id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "La catégorie a été désactivée avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la catégorie {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur lors de la suppression de la catégorie.");
        }

        return "redirect:/admin/categories";
    }
}
