package com.securedesk.controller;

import com.securedesk.entity.Ticket;
import com.securedesk.entity.TicketStatus;
import com.securedesk.entity.User;
import com.securedesk.service.TicketService;
import com.securedesk.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

/**
 * Contrôleur pour le tableau de bord de l'agent.
 * Affiche les statistiques et les tickets assignés à l'agent connecté.
 */
@Slf4j
@Controller
@RequestMapping("/agent")
public class AgentController {

    private final UserService userService;
    private final TicketService ticketService;

    public AgentController(UserService userService, TicketService ticketService) {
        this.userService = userService;
        this.ticketService = ticketService;
    }

    /**
     * Affiche le tableau de bord de l'agent avec les statistiques
     * de ses tickets assignés (ouverts, en cours, résolus, fermés).
     *
     * @param model     le modèle pour la vue
     * @param principal l'utilisateur authentifié
     * @return la vue du tableau de bord agent
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName());
        log.info("Accès au tableau de bord agent par : {}", currentUser.getUsername());

        // Récupérer les tickets assignés à l'agent
        List<Ticket> assignedTickets = ticketService.findByAssignedTo(currentUser);

        // Calculer les statistiques par statut
        long totalAssigned = assignedTickets.size();
        long openCount = assignedTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.OPEN)
                .count();
        long inProgressCount = assignedTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS)
                .count();
        long resolvedCount = assignedTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.RESOLVED)
                .count();
        long closedCount = assignedTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.CLOSED)
                .count();

        model.addAttribute("user", currentUser);
        model.addAttribute("assignedTickets", assignedTickets);
        model.addAttribute("totalAssigned", totalAssigned);
        model.addAttribute("openCount", openCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("closedCount", closedCount);

        return "agent/dashboard";
    }
}
