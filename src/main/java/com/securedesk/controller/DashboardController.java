package com.securedesk.controller;

import com.securedesk.entity.User;
import com.securedesk.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Collection;

/**
 * Contrôleur du tableau de bord.
 * Redirige l'utilisateur vers le tableau de bord approprié selon son rôle.
 */
@Slf4j
@Controller
public class DashboardController {

    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Point d'entrée principal — redirige vers /dashboard.
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    /**
     * Détermine le rôle de l'utilisateur connecté et le redirige
     * vers le tableau de bord correspondant.
     *
     * @param principal l'utilisateur authentifié
     * @return redirection vers le tableau de bord approprié
     */
    @GetMapping("/dashboard")
    public String dashboard(Principal principal) {
        User currentUser = userService.findByUsername(principal.getName());
        log.info("Redirection du tableau de bord pour l'utilisateur : {} (rôle : {})",
                currentUser.getUsername(), currentUser.getRole());

        return switch (currentUser.getRole()) {
            case ROLE_ADMIN -> "redirect:/admin/dashboard";
            case ROLE_AGENT -> "redirect:/agent/dashboard";
            case ROLE_CLIENT -> "redirect:/tickets";
        };
    }
}
