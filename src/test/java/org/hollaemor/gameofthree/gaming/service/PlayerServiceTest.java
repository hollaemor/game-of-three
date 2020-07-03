package org.hollaemor.gameofthree.gaming.service;

import org.hollaemor.gameofthree.gaming.datatransfer.GameMessage;
import org.hollaemor.gameofthree.gaming.datatransfer.GameStatus;
import org.hollaemor.gameofthree.gaming.datatransfer.PlayerDto;
import org.hollaemor.gameofthree.gaming.domain.Player;
import org.hollaemor.gameofthree.gaming.domain.PlayerStatus;
import org.hollaemor.gameofthree.gaming.storage.PlayerStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private PlayerStore playerStore;

    @InjectMocks
    private PlayerService service;

    @Captor
    private ArgumentCaptor<GameMessage> messageCaptor;


    @Captor
    private ArgumentCaptor<List<PlayerDto>> playerListCaptor;

    @Test
    public void whenPlayerIsSaved_Then_PublishMessage() {
        // given
        var player = new Player("Wonder Woman");
        var playerList = List.of(new Player("Batman"), new Player("Superman"));

        given(playerStore.getPlayers()).willReturn(playerList);

        // when
        service.save(player);

        // then
        verify(playerStore).save(player);
        verify(messagingTemplate).convertAndSend(eq("/topic/users"), playerListCaptor.capture());

        assertThat(playerListCaptor.getValue()).hasSize(2);
        var playerDtos = playerListCaptor.getValue();

        assertThat(playerDtos).extracting("name", "status")
                .containsExactly(tuple("Batman", PlayerStatus.AVAILABLE), tuple("Superman", PlayerStatus.AVAILABLE));
    }


    @Test
    public void whenPlayerIsRemoved_Then_UpdateOpponent() {
        // given
        var player = new Player("Flash");
        var opponent = new Player("Aqua Man");
        player.setOpponent(opponent);

        given(playerStore.findByName(BDDMockito.anyString()))
                .willReturn(Optional.of(player));

        given(playerStore.getPlayers()).willReturn(List.of(opponent));

        assertThat(opponent.getStatus()).isEqualTo(PlayerStatus.PAIRED);

        // when
        service.removePlayer("Flash");

        // then
        assertThat(opponent.getOpponent()).isNull();
        assertThat(opponent.getStatus()).isEqualTo(PlayerStatus.AVAILABLE);

        verify(playerStore).delete(eq(player));
        verify(playerStore).save(eq(opponent));

        verify(messagingTemplate).convertAndSendToUser(eq("Aqua Man"), eq("/queue/updates"), messageCaptor.capture());

        var message = messageCaptor.getValue();
        assertThat(message.getGameStatus()).isEqualTo(GameStatus.DISCONNECT);
        assertThat(message.getContent()).isEqualTo("Flash disconnected from game");

        verify(messagingTemplate).convertAndSend(eq("/topic/users"), anyCollection());
    }
}
