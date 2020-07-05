package org.hollaemor.gameofthree.gaming.infrastructure.service;

import org.hollaemor.gameofthree.gaming.domain.GameMessage;
import org.hollaemor.gameofthree.gaming.domain.GameStatus;
import org.hollaemor.gameofthree.gaming.domain.Player;
import org.hollaemor.gameofthree.gaming.domain.PlayerStatus;
import org.hollaemor.gameofthree.gaming.infrastructure.repository.PlayerRepository;
import org.hollaemor.gameofthree.gaming.infrastructure.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService service;

    @Captor
    private ArgumentCaptor<GameMessage> messageCaptor;


    @Test
    public void whenPlayerIsSaved_Then_DelegateToPlayerStore() {
        // given
        var player = new Player("Wonder Woman");

        // when
        service.save(player);

        // then
        verify(playerRepository).save(player);
    }


    @Test
    public void whenPlayerIsRemoved_Then_UpdateOpponent() {
        // given
        var player = new Player("Flash");
        var opponent = new Player("Aqua Man");
        player.setOpponent(opponent);

        given(playerRepository.findByName(BDDMockito.anyString()))
                .willReturn(Optional.of(player));

        assertThat(opponent.getStatus()).isEqualTo(PlayerStatus.PAIRED);

        // when
        service.removePlayer("Flash");

        // then
        assertThat(opponent.getOpponent()).isNull();
        assertThat(opponent.getStatus()).isEqualTo(PlayerStatus.AVAILABLE);

        verify(playerRepository).delete(eq(player));
        verify(playerRepository).save(eq(opponent));

        verify(messagingTemplate).convertAndSendToUser(eq("Aqua Man"), eq("/queue/updates"), messageCaptor.capture());

        var message = messageCaptor.getValue();
        assertThat(message.getGameStatus()).isEqualTo(GameStatus.DISCONNECT);
        assertThat(message.getContent()).isEqualTo("Flash disconnected from game");
    }
}
