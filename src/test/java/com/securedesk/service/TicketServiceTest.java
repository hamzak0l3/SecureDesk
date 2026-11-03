package com.securedesk.service;

import com.securedesk.dto.DashboardStats;
import com.securedesk.entity.*;
import com.securedesk.repository.CategoryRepository;
import com.securedesk.repository.TicketRepository;
import com.securedesk.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour TicketService (Cycle de vie, assignation, statut, statistiques).
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TicketService ticketService;

    private User client;
    private User agent;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        client = User.builder().id(10L).username("client1").role(Role.ROLE_CLIENT).build();
        agent = User.builder().id(20L).username("agent1").role(Role.ROLE_AGENT).build();

        ticket = Ticket.builder()
                .id(1L)
                .title("Connexion VPN impossible")
                .description("Erreur timeout lors de la connexion")
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.HIGH)
                .createdBy(client)
                .build();
    }

    @Test
    @DisplayName("Création d'un ticket : statut OPEN et créateur associé")
    void shouldCreateTicketWithOpenStatus() {
        Ticket newTicket = Ticket.builder()
                .title("Problème écran")
                .description("Écran noir")
                .priority(TicketPriority.MEDIUM)
                .build();

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket created = ticketService.createTicket(newTicket, client);

        assertThat(created.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(created.getCreatedBy()).isEqualTo(client);
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getUpdatedAt()).isNotNull();
        verify(ticketRepository).save(newTicket);
    }

    @Test
    @DisplayName("Assignation à un agent : mise à jour du statut en IN_PROGRESS")
    void shouldAssignTicketAndSetInProgress() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(20L)).thenReturn(Optional.of(agent));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket assigned = ticketService.assignTicket(1L, agent);

        assertThat(assigned.getAssignedTo()).isEqualTo(agent);
        assertThat(assigned.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("Clôture d'un ticket : statut CLOSED et closedAt renseigné")
    void shouldCloseTicketAndSetClosedAt() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket closed = ticketService.updateStatus(1L, TicketStatus.CLOSED);

        assertThat(closed.getStatus()).isEqualTo(TicketStatus.CLOSED);
        assertThat(closed.getClosedAt()).isNotNull();
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("Génération des statistiques globales pour le tableau de bord admin")
    void shouldCalculateDashboardStats() {
        when(ticketRepository.count()).thenReturn(10L);
        when(ticketRepository.countByStatus(TicketStatus.OPEN)).thenReturn(4L);
        when(ticketRepository.countByStatus(TicketStatus.IN_PROGRESS)).thenReturn(3L);
        when(ticketRepository.countByStatus(TicketStatus.RESOLVED)).thenReturn(2L);
        when(ticketRepository.countByStatus(TicketStatus.CLOSED)).thenReturn(1L);
        when(userRepository.count()).thenReturn(8L);
        when(userRepository.findByRole(Role.ROLE_AGENT)).thenReturn(List.of(agent));
        when(categoryRepository.count()).thenReturn(3L);

        DashboardStats stats = ticketService.getDashboardStats();

        assertThat(stats.getTotalTickets()).isEqualTo(10L);
        assertThat(stats.getOpenTickets()).isEqualTo(4L);
        assertThat(stats.getInProgressTickets()).isEqualTo(3L);
        assertThat(stats.getResolvedTickets()).isEqualTo(2L);
        assertThat(stats.getClosedTickets()).isEqualTo(1L);
        assertThat(stats.getTotalUsers()).isEqualTo(8L);
        assertThat(stats.getTotalAgents()).isEqualTo(1L);
        assertThat(stats.getTotalCategories()).isEqualTo(3L);
    }

    @Test
    @DisplayName("V1 : la recherche utilise une requête native concaténée")
    void shouldSearchWithNativeConcatenatedQuery() {
        Query nativeQuery = mock(Query.class);
        when(entityManager.createNativeQuery(any(String.class), eq(Ticket.class)))
                .thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(List.of(ticket));

        List<Ticket> results = ticketService.search("VPN", client);

        assertThat(results).hasSize(1);
        verify(entityManager).createNativeQuery(contains("VPN"), eq(Ticket.class));
        verify(ticketRepository, never()).findByCreatedByAndTitleContainingIgnoreCase(any(), any());
    }
}

