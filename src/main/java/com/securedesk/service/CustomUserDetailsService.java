package com.securedesk.service;

import com.securedesk.entity.User;
import com.securedesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service d'authentification Spring Security.
 * Charge les détails de l'utilisateur à partir de la base de données
 * pour l'authentification par session.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Charge un utilisateur par son nom d'utilisateur.
     * Vérifie que le compte est actif avant de retourner les détails.
     *
     * @param username le nom d'utilisateur recherché
     * @return les détails de l'utilisateur pour Spring Security
     * @throws UsernameNotFoundException si l'utilisateur n'existe pas ou est inactif
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Tentative de chargement de l'utilisateur : {}", username);

        // Rechercher l'utilisateur dans la base de données
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Utilisateur non trouvé : {}", username);
                    return new UsernameNotFoundException(
                            "Utilisateur non trouvé : " + username);
                });

        // Vérifier que le compte est actif
        if (!user.isActive()) {
            log.warn("Tentative de connexion avec un compte inactif : {}", username);
            throw new UsernameNotFoundException(
                    "Le compte utilisateur est désactivé : " + username);
        }

        log.info("Utilisateur chargé avec succès : {} (rôle : {})", username, user.getRole());

        // Construire et retourner l'objet UserDetails de Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isActive(),       // enabled
                true,                  // accountNonExpired
                true,                  // credentialsNonExpired
                true,                  // accountNonLocked
                Collections.singletonList(
                        new SimpleGrantedAuthority(user.getRole().name())
                )
        );
    }
}
