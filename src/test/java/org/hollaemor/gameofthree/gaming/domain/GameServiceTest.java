package org.hollaemor.gameofthree.gaming.domain;

import org.hollaemor.gameofthree.gaming.infrastructure.repository.PlayerRepository;
import org.hollaemor.gameofthree.gaming.infrastructure.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private GameService gameService;

    @Captor
    private ArgumentCaptor<GameMessage> messageCaptor;


    @Test
    public void whenPlayerWithNameIsNotFound_Then_ThrowException() {
        // given
        given(playerRepository.findByName(anyString()))
                .willReturn(Optional.empty());

        // when / then
        assertThatExceptionOfType(PlayerNotFoundException.class)
                .isThrownBy(() -> gameService.startForPlayer("Hulk"))
                .withMessage("Player not found: Hulk");
    }

    @Test
    public void whenGameIsStarted_And_NoPlayerIsAvailable_Then_SendWaitMessage() {
        // given
        given(playerRepository.findByName(anyString()))
                .willReturn(Optional.of(new Player("Thor")));

        given(playerRepository.findAvailableForPlayer(anyString()))
                .willReturn(Optional.empty());

        // when
        var message = gameService.startForPlayer("Thor");

        assertThat(message.getGameStatus()).isEqualTo(GameStatus.WAITING);
        assertThat(message.getContent()).isEqualTo("Waiting for available player");
        verify(playerRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }


    @Test
    public void whenGameIsStarted_And_PlayerIsAvailable_Then_NotifyAvailablePlayer() {
        // given
        var player = new Player("Ant Man");
        var availablePlayer = new Player("Wasp");

        given(playerRepository.findByName(anyString()))
                .willReturn(Optional.of(player));
        given(playerRepository.findAvailableForPlayer(anyString()))
                .willReturn(Optional.of(availablePlayer));

        // when
        var message = gameService.startForPlayer("Ant Man");

        // then
        assertThat(player.isPrimary()).isFalse();
        assertThat(availablePlayer.isPrimary()).isTrue();
        assertThat(message.getGameStatus()).isEqualTo(GameStatus.START);
        assertThat(message.getOpponent()).isEqualTo("Wasp");
        assertThat(message.isPrimaryPlayer()).isFalse();

        verify(notificationService).notifyPlayer(eq("Wasp"), messageCaptor.capture());

        var availablePlayerMessage = messageCaptor.getValue();
        assertThat(availablePlayerMessage.getGameStatus()).isEqualTo(GameStatus.START);
        assertThat(availablePlayerMessage.getOpponent()).isEqualTo("Ant Man");
        assertThat(availablePlayerMessage.isPrimaryPlayer()).isTrue();
        assertThat(availablePlayerMessage.getContent()).isEqualTo("Ant Man requested a game session");

        verify(playerRepository, times(2)).save(any());
    }


    @Test
    public void whenGameIsStarted_And_PlayerIsAlreadyPaired_Then_NotifyOpponent() {
        // given
        var opponent = new Player("Falcon");
        var player = new Player("Captain America");

        player.setOpponent(opponent);

        given(playerRepository.findByName(anyString()))
                .willReturn(Optional.of(player));

        // when
        var message = gameService.startForPlayer("Captain America");

        // then

        assertThat(message.getGameStatus()).isEqualTo(GameStatus.START);
        assertThat(message.getOpponent()).isEqualTo("Falcon");

        verify(playerRepository, never()).findAvailableForPlayer(anyString());
        verify(playerRepository, never()).save(any());

        verify(notificationService).notifyPlayer(eq("Falcon"), any());
    }

    @Test
    public void processRandomNumberFromPlayer_ShouldFailIfPlayerNotFound() {
        // given
        given(playerRepository.findByName(anyString()))
                .willReturn(Optional.empty());

        // when / then
        assertThatExceptionOfType(PlayerNotFoundException.class)
                .isThrownBy(() -> gameService.processRandomNumberFromPlayer(42, "Black Widow"))
                .withMessage("Player not found: Black Widow");
    }

    @Test
    public void processRandomNumberFromPlayer_ShouldFailIfPlayerHasNotBeenPaired() {
        // given
        given(playerRepository.findByName(anyString()))
                .willReturn(Optional.of(new Player("Hawk Eye")));

        // when / then
        assertThatExceptionOfType(OpponentDoesNotExistException.class)
                .isThrownBy(() -> gameService.processRandomNumberFromPlayer(42, "Hawk Eye"))
                .withMessage("You have not been paired with an opponent");
    }

    @Test
    public void processRandomNumberFromPlayer_ShouldNotifyOpponent() {
        // given
        var randomNumber = 50;
        var player = new Player("Loki");
        player.setOpponent(new Player("Asgard"));

        given(playerRepository.findByName(anyString()))
                .willReturn(Optional.of(player));

        // when
        gameService.processRandomNumberFromPlayer(randomNumber, player.getName());

        // then
        verify(notificationService).notifyPlayer(eq("Asgard"), messageCaptor.capture());

        var message = messageCaptor.getValue();
        assertThat(message.getGameStatus()).isEqualTo(GameStatus.PLAY);
        assertThat(message.getValue()).isEqualTo(50);
    }

    @Test
    public void processPlayerMove_ShouldFailIfCombinationIsNotDivisibleByDivisor() {
        // given
        var instruction = GameInstruction.builder().value(20).move(-1).build();

        // when / then
        assertThatExceptionOfType(InvalidCombinationException.class)
                .isThrownBy(() -> gameService.processPlayerMove("Star Lord", instruction))
                .withMessage("19 is not divisible by 3");
    }

    @Test
    public void processPlayerMove_ShouldFailIfPlayerNotFound() {
        // given
        var instruction = GameInstruction.builder().value(41).move(1).build();

        given(playerRepository.findByName(anyString()))
                .willReturn(Optional.empty());

        // when / then
        assertThatExceptionOfType(PlayerNotFoundException.class)
                .isThrownBy(() -> gameService.processPlayerMove("Iron Man", instruction));
    }


    @Test
    public void processPlayerMove_ShouldFailIfOpponentDoesNotExist() {
        // given
        given(playerRepository.findByName(anyString()))
                .willReturn(Optional.of(new Player("Winter Soldier")));

        // when / then
        assertThatExceptionOfType(OpponentDoesNotExistException.class)
                .isThrownBy(() -> gameService.processPlayerMove("Winter Soldier", GameInstruction.builder().value(12).move(0).build()));
    }

    @Test
    public void processPlayerMove_ShouldNotifyOpponentIfDivisionByThreeIsNotOne() {
        // given
        var instruction = GameInstruction.builder().value(21).move(0).build();
        var player = new Player("Rocket");
        var opponent = new Player("Groot");

        player.setOpponent(opponent);

        given(playerRepository.findByName(anyString()))
                .willReturn(Optional.of(player));

        // when
        gameService.processPlayerMove("Rocket", instruction);

        // then
        verify(notificationService).notifyPlayer(eq("Groot"), messageCaptor.capture());

        var message = messageCaptor.getValue();
        assertThat(message.getGameStatus()).isEqualTo(GameStatus.PLAY);
        assertThat(message.getValue()).isEqualTo(7);
    }


    @Test
    public void processPlayerMove_ShouldNotifyBothPlayersIsDivisionByThreeIsOne() {
        // given
        var instruction = GameInstruction.builder().value(2).move(1).build();
        var player = new Player("Black Panther");
        var opponent = new Player("Okoye");

        player.setOpponent(opponent);

        given(playerRepository.findByName(anyString()))
                .willReturn(Optional.of(player));

        // when
        gameService.processPlayerMove("Black Panther", instruction);

        // then
        verify(notificationService).notifyPlayer(eq("Black Panther"), messageCaptor.capture());
        verify(notificationService).notifyPlayer(eq("Okoye"), messageCaptor.capture());

        assertThat(messageCaptor.getAllValues()).extracting("gameStatus")
                .allMatch(type -> type.equals(GameStatus.GAMEOVER));

        assertThat(messageCaptor.getAllValues()).extracting("winner").containsExactly(true, false);
    }

}
