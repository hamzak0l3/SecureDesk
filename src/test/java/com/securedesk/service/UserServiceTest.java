package com.securedesk.service;

import com.securedesk.dto.UserRegistrationDto;
import com.securedesk.entity.Role;
import com.securedesk.entity.User;
import com.securedesk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserService (Gestion des utilisateurs, BCrypt, rôles et soft-delete).
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("testclient")
                .password("encoded_pass")
                .email("client@test.local")
                .fullName("Client Test")
                .role(Role.ROLE_CLIENT)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Création d'un utilisateur : mot de passe correctement encodé avec BCrypt")
    void shouldCreateUserWithEncodedPassword() {
        User rawUser = User.builder()
                .username("newuser")
                .password("plainSecret123")
                .email("new@test.local")
                .fullName("Nouveau")
                .role(Role.ROLE_CLIENT)
                .build();

        when(passwordEncoder.encode("plainSecret123")).thenReturn("bcrypt_hash_xyz");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(rawUser);

        assertThat(created.getPassword()).isEqualTo("bcrypt_hash_xyz");
        verify(passwordEncoder).encode("plainSecret123");
        verify(userRepository).save(rawUser);
    }

    @Test
    @DisplayName("Inscription d'un utilisateur via DTO avec attribution de ROLE_CLIENT par défaut")
    void shouldRegisterUserFromDto() {
        UserRegistrationDto dto = new UserRegistrationDto("registered_user", "secret123", "secret123",
                "reg@test.local", "Jean Dupont");

        when(passwordEncoder.encode("secret123")).thenReturn("bcrypt_hash_dto");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userService.registerUser(dto, Role.ROLE_CLIENT);

        assertThat(registered.getUsername()).isEqualTo("registered_user");
        assertThat(registered.getRole()).isEqualTo(Role.ROLE_CLIENT);
        assertThat(registered.isActive()).isTrue();
    }

    @Test
    @DisplayName("Vérification du mot de passe avec le PasswordEncoder")
    void shouldVerifyPasswordCorrectly() {
        when(passwordEncoder.matches("rawPass", "encoded_pass")).thenReturn(true);
        when(passwordEncoder.matches("wrongPass", "encoded_pass")).thenReturn(false);

        assertThat(userService.checkPassword(sampleUser, "rawPass")).isTrue();
        assertThat(userService.checkPassword(sampleUser, "wrongPass")).isFalse();
    }

    @Test
    @DisplayName("Changement de mot de passe : encodage et mise à jour")
    void shouldChangePassword() {
        when(passwordEncoder.encode("newSecretPass")).thenReturn("new_bcrypt_hash");

        userService.changePassword(sampleUser, "newSecretPass");

        assertThat(sampleUser.getPassword()).isEqualTo("new_bcrypt_hash");
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Soft-delete : désactivation du compte utilisateur sans suppression physique")
    void shouldSoftDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        userService.softDeleteUser(1L);

        assertThat(sampleUser.isActive()).isFalse();
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Recherche par nom d'utilisateur existant et exception si introuvable")
    void shouldFindUserByUsernameOrThrow() {
        when(userRepository.findByUsername("testclient")).thenReturn(Optional.of(sampleUser));
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        User found = userService.findByUsername("testclient");
        assertThat(found.getUsername()).isEqualTo("testclient");

        assertThatThrownBy(() -> userService.findByUsername("unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Utilisateur non trouvé");
    }

    @Test
    @DisplayName("Filtrage des agents de support")
    void shouldFindAgents() {
        User agent = User.builder().username("agent1").role(Role.ROLE_AGENT).build();
        when(userRepository.findByRole(Role.ROLE_AGENT)).thenReturn(List.of(agent));

        List<User> agents = userService.findAgents();
        assertThat(agents).hasSize(1);
        assertThat(agents.get(0).getRole()).isEqualTo(Role.ROLE_AGENT);
    }
}

