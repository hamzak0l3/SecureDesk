package com.securedesk.controller;

import com.securedesk.config.SecurityConfig;
import com.securedesk.service.CustomUserDetailsService;
import com.securedesk.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration des règles d'accès Spring Security (RBAC et routes protégées).
 */
@WebMvcTest(controllers = {DashboardController.class, AuthController.class})
@Import(SecurityConfig.class)
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("Accès public : la page /login est accessible sans authentification")
    void loginPageShouldBePublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Accès public : la page /register est accessible sans authentification")
    void registerPageShouldBePublic() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Accès non authentifié : tentative sur /dashboard redirige vers /login")
    void unauthenticatedUserShouldBeRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "client_user", roles = {"CLIENT"})
    @DisplayName("Contrôle RBAC : ROLE_CLIENT n'a pas accès à /admin/** (403 Forbidden)")
    void clientCannotAccessAdminSection() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "client_user", roles = {"CLIENT"})
    @DisplayName("Contrôle RBAC : ROLE_CLIENT n'a pas accès à /agent/** (403 Forbidden)")
    void clientCannotAccessAgentSection() throws Exception {
        mockMvc.perform(get("/agent/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "agent_user", roles = {"AGENT"})
    @DisplayName("Contrôle RBAC : ROLE_AGENT n'a pas accès à /admin/** (403 Forbidden)")
    void agentCannotAccessAdminSection() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }
}

