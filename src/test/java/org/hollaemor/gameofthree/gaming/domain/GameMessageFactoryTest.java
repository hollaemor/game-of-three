package org.hollaemor.gameofthree.gaming.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GameMessageFactoryTest {

    @Test
    public void buildWaitingMessage_Should_ReturnWaitingGameStatus() {
        // given / when
        var message = GameMessageFactory.buildWaitingMessage();

        // then
        assertThat(message.getGameStatus()).isEqualTo(GameStatus.WAITING);
        assertThat(message.getOpponent()).isNull();
        assertThat(message.isPrimaryPlayer()).isTrue();
        assertThat(message.getContent()).isEqualTo("Waiting for available player");
    }


    @Test
    public void buildPlayMessage_Should_ReturnPlayGameStatus() {
        // given / when
        var message = GameMessageFactory.buildPlayMessage(40);

        // then
        assertThat(message.getGameStatus()).isEqualTo(GameStatus.PLAY);
        assertThat(message.getValue()).isEqualTo(40);
    }

    @Test
    public void buildGameOverMessage_Should_ReturnGameOverStatus() {
        //  given / when
        var message = GameMessageFactory.buildGameOverMessage(true);

        // then
        assertThat(message.getGameStatus()).isEqualTo(GameStatus.GAMEOVER);
        assertThat(message.isWinner()).isTrue();
    }

    @Test
    public void buildStartMessageForPlayer_Should_ReturnStartGameStatus() {
        // given
        var player = new Player("Samsung");
        player.setPrimary(true);
        var opponent = new Player("Apple");

        player.setOpponent(opponent);

        // when
        var message = GameMessageFactory.buildStartMessageForPlayer(player);

        // then
        assertThat(message.getGameStatus()).isEqualTo(GameStatus.START);
        assertThat(message.getOpponent()).isEqualTo("Apple");
        assertThat(message.isPrimaryPlayer()).isTrue();
        assertThat(message.getContent()).isEqualTo("Apple requested a game session");
    }

    @Test
    public void buildDisconnectMessage_Should_ReturnDisconnectGameStatus() {
        // given / when
        var message = GameMessageFactory.buildDisconnectMessage("Microsoft");

        // then
        assertThat(message.getGameStatus()).isEqualTo(GameStatus.DISCONNECT);
        assertThat(message.getContent()).isEqualTo("Microsoft disconnected from game");
    }
}
