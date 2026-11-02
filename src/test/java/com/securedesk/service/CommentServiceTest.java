package com.securedesk.service;

import com.securedesk.entity.Comment;
import com.securedesk.entity.Ticket;
import com.securedesk.entity.User;
import com.securedesk.repository.CommentRepository;
import com.securedesk.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CommentService (Ajout de commentaire, synchronisation du ticket).
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("Ajout d'un commentaire avec mise à jour automatique de la date du ticket")
    void shouldAddCommentAndUpdateTicketTimestamp() {
        Ticket ticket = Ticket.builder().id(5L).title("Bug").build();
        User author = User.builder().id(2L).username("agent1").build();

        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comment result = commentService.addComment(5L, "Solution envoyée", author);

        assertThat(result.getContent()).isEqualTo("Solution envoyée");
        assertThat(result.getAuthor()).isEqualTo(author);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(ticket.getUpdatedAt()).isNotNull();

        verify(commentRepository).save(any(Comment.class));
        verify(ticketRepository).save(ticket);
    }
}

