package com.securedesk.controller;

import com.securedesk.dto.CommentCreateDto;
import com.securedesk.dto.TicketCreateDto;
import com.securedesk.entity.*;
import com.securedesk.service.CategoryService;
import com.securedesk.service.CommentService;
import com.securedesk.service.TicketService;
import com.securedesk.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * Contrôleur de gestion des tickets.
 * Gère la création, la consultation, la recherche, l'assignation,
 * le changement de statut et l'ajout de commentaires aux tickets.
 */
@Slf4j
@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;
    private final CommentService commentService;
    private final CategoryService categoryService;

    public TicketController(TicketService ticketService,
                            UserService userService,
                            CommentService commentService,
                            CategoryService categoryService) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.commentService = commentService;
        this.categoryService = categoryService;
    }

    /**
     * Liste les tickets en fonction du rôle de l'utilisateur connecté.
     * - CLIENT : voit uniquement ses propres tickets
     * - AGENT : voit les tickets qui lui sont assignés
     * - ADMIN : voit tous les tickets
     */
    @GetMapping
    public String listTickets(Model model, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName());
        log.info("Liste des tickets demandée par : {} (rôle : {})",
                currentUser.getUsername(), currentUser.getRole());

        List<Ticket> tickets = switch (currentUser.getRole()) {
            case ROLE_CLIENT -> ticketService.findByCreatedBy(currentUser);
            case ROLE_AGENT -> ticketService.findByAssignedTo(currentUser);
            case ROLE_ADMIN -> ticketService.findAll();
        };

        model.addAttribute("tickets", tickets);
        model.addAttribute("user", currentUser);
        return "ticket/list";
    }

    /**
     * Affiche le formulaire de création d'un nouveau ticket.
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        log.debug("Affichage du formulaire de création de ticket");
        model.addAttribute("ticketCreateDto", new TicketCreateDto());
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("priorities", TicketPriority.values());
        return "ticket/create";
    }

    /**
     * Traite la soumission du formulaire de création de ticket.
     *
     * @param dto               les données du ticket à créer
     * @param bindingResult     résultat de la validation
     * @param principal         l'utilisateur authentifié
     * @param model             le modèle pour la vue
     * @param redirectAttributes attributs pour les messages flash
     * @return redirection vers la liste des tickets ou retour au formulaire
     */
    @PostMapping("/create")
    public String createTicket(
            @Valid @ModelAttribute("ticketCreateDto") TicketCreateDto dto,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.warn("Erreurs de validation lors de la création du ticket");
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("priorities", TicketPriority.values());
            return "ticket/create";
        }

        User currentUser = userService.findByUsername(principal.getName());
        log.info("Création d'un nouveau ticket par l'utilisateur : {}", currentUser.getUsername());

        try {
            Ticket ticket = new Ticket();
            ticket.setTitle(dto.getTitle());
            ticket.setDescription(dto.getDescription());
            try {
                ticket.setPriority(TicketPriority.valueOf(dto.getPriority().toUpperCase()));
            } catch (Exception e) {
                ticket.setPriority(TicketPriority.MEDIUM);
            }
            ticket.setCreatedBy(currentUser);

            // Associer la catégorie si elle est spécifiée
            if (dto.getCategoryId() != null) {
                Category category = categoryService.findById(dto.getCategoryId());
                ticket.setCategory(category);
            }

            ticketService.createTicket(ticket);
            log.info("Ticket créé avec succès : {}", ticket.getTitle());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Le ticket a été créé avec succès.");
            return "redirect:/tickets";
        } catch (Exception e) {
            log.error("Erreur lors de la création du ticket", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Une erreur est survenue lors de la création du ticket.");
            return "redirect:/tickets/create";
        }
    }

    /**
     * Affiche le détail d'un ticket avec ses commentaires.
     * Vérifie les droits d'accès : un client ne peut voir que ses propres tickets.
     *
     * @param id        identifiant du ticket
     * @param model     le modèle pour la vue
     * @param principal l'utilisateur authentifié
     * @return la page de détail du ticket
     */
    @GetMapping("/{id}")
    public String viewTicket(@PathVariable Long id, Model model, Principal principal,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.findByUsername(principal.getName());
        Ticket ticket = ticketService.findById(id);

        if (ticket == null) {
            log.warn("Ticket introuvable avec l'identifiant : {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Ticket introuvable.");
            return "redirect:/tickets";
        }

        // [V3] IDOR : le ticket est chargé par identifiant sans vérifier le propriétaire.

        List<Comment> comments = commentService.findByTicket(ticket);
        List<User> agents = userService.findByRole(Role.ROLE_AGENT);

        model.addAttribute("ticket", ticket);
        model.addAttribute("comments", comments);
        model.addAttribute("commentCreateDto", new CommentCreateDto());
        model.addAttribute("agents", agents);
        model.addAttribute("statuses", TicketStatus.values());
        model.addAttribute("user", currentUser);

        log.debug("Affichage du détail du ticket {} pour l'utilisateur {}",
                id, currentUser.getUsername());
        return "ticket/detail";
    }

    /**
     * Ajoute un commentaire à un ticket existant.
     *
     * @param id                 identifiant du ticket
     * @param dto                les données du commentaire
     * @param bindingResult      résultat de la validation
     * @param principal          l'utilisateur authentifié
     * @param redirectAttributes attributs pour les messages flash
     * @return redirection vers le détail du ticket
     */
    @PostMapping("/{id}/comment")
    public String addComment(
            @PathVariable Long id,
            @Valid @ModelAttribute("commentCreateDto") CommentCreateDto dto,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Le contenu du commentaire est invalide.");
            return "redirect:/tickets/" + id;
        }

        User currentUser = userService.findByUsername(principal.getName());
        Ticket ticket = ticketService.findById(id);

        if (ticket == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ticket introuvable.");
            return "redirect:/tickets";
        }

        try {
            Comment comment = new Comment();
            comment.setContent(dto.getContent());
            comment.setTicket(ticket);
            comment.setAuthor(currentUser);

            commentService.addComment(comment);
            log.info("Commentaire ajouté au ticket {} par l'utilisateur {}",
                    id, currentUser.getUsername());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Commentaire ajouté avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout du commentaire au ticket {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur lors de l'ajout du commentaire.");
        }

        return "redirect:/tickets/" + id;
    }

    /**
     * Change le statut d'un ticket (réservé aux agents et administrateurs).
     *
     * @param id                 identifiant du ticket
     * @param status             le nouveau statut
     * @param principal          l'utilisateur authentifié
     * @param redirectAttributes attributs pour les messages flash
     * @return redirection vers le détail du ticket
     */
    @PostMapping("/{id}/status")
    public String changeStatus(
            @PathVariable Long id,
            @RequestParam("status") TicketStatus status,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User currentUser = userService.findByUsername(principal.getName());

        // Seuls les agents et les admins peuvent changer le statut
        if (currentUser.getRole() == Role.ROLE_CLIENT) {
            log.warn("Accès refusé : le client {} tente de modifier le statut du ticket {}",
                    currentUser.getUsername(), id);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Vous n'avez pas l'autorisation de modifier le statut de ce ticket.");
            return "redirect:/tickets/" + id;
        }

        try {
            ticketService.updateStatus(id, status);
            log.info("Statut du ticket {} changé en {} par l'utilisateur {}",
                    id, status, currentUser.getUsername());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Le statut du ticket a été mis à jour.");
        } catch (Exception e) {
            log.error("Erreur lors du changement de statut du ticket {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur lors de la mise à jour du statut.");
        }

        return "redirect:/tickets/" + id;
    }

    /**
     * Assigne un ticket à un agent (réservé aux agents et administrateurs).
     *
     * @param id                 identifiant du ticket
     * @param agentId            identifiant de l'agent à assigner
     * @param principal          l'utilisateur authentifié
     * @param redirectAttributes attributs pour les messages flash
     * @return redirection vers le détail du ticket
     */
    @PostMapping("/{id}/assign")
    public String assignTicket(
            @PathVariable Long id,
            @RequestParam("agentId") Long agentId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User currentUser = userService.findByUsername(principal.getName());

        // Seuls les agents et les admins peuvent assigner un ticket
        if (currentUser.getRole() == Role.ROLE_CLIENT) {
            log.warn("Accès refusé : le client {} tente d'assigner le ticket {}",
                    currentUser.getUsername(), id);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Vous n'avez pas l'autorisation d'assigner ce ticket.");
            return "redirect:/tickets/" + id;
        }

        try {
            User agent = userService.findById(agentId);
            ticketService.assignTicket(id, agent);
            log.info("Ticket {} assigné à l'agent {} par l'utilisateur {}",
                    id, agent.getUsername(), currentUser.getUsername());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Le ticket a été assigné à " + agent.getFullName() + ".");
        } catch (Exception e) {
            log.error("Erreur lors de l'assignation du ticket {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur lors de l'assignation du ticket.");
        }

        return "redirect:/tickets/" + id;
    }

    /**
     * Recherche de tickets par mot-clé.
     *
     * @param query     le terme de recherche
     * @param model     le modèle pour la vue
     * @param principal l'utilisateur authentifié
     * @return la liste des tickets correspondants
     */
    @GetMapping("/search")
    public String searchTickets(
            @RequestParam("query") String query,
            Model model,
            Principal principal) {

        User currentUser = userService.findByUsername(principal.getName());
        log.info("Recherche de tickets avec le terme '{}' par l'utilisateur {}",
                query, currentUser.getUsername());

        List<Ticket> tickets = ticketService.search(query, currentUser);

        model.addAttribute("tickets", tickets);
        model.addAttribute("user", currentUser);
        model.addAttribute("searchQuery", query);
        return "ticket/list";
    }
}
