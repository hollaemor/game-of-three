package org.hollaemor.gameofthree.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PlayerTest {

    @Test
    public void whenPlayerIsCreated_Then_StatusIsReady() {
        // given / when
        var player = new Player("Transformers");

        // then
        assertThat(player.getStatus()).isEqualTo(PlayerStatus.READY);
        assertThat(player.getName()).isEqualTo("Transformers");
        assertThat(player.isPrimary()).isFalse();
        assertThat(player.getOpponent()).isNull();
    }


    @Test
    public void whenOpponentIsSet_Then_UpdatePlayerStatuses() {
        // given
        var player = new Player("Decepticons");
        var opponent = new Player("Autobots");

        // when
        player.setOpponent(opponent);

        // then
        assertThat(player.getStatus()).isEqualTo(PlayerStatus.BUSY);
        assertThat(player.getOpponent()).isEqualTo(opponent);
        assertThat(opponent.getStatus()).isEqualTo(PlayerStatus.BUSY);
        assertThat(opponent.getOpponent()).isEqualTo(player);
    }

    @Test
    public void removeOpponent_Should_RemoveOpponent_And_UpdateState() {
        // given
        var player = new Player("Optimus Prime");
        var opponent = new Player("Bumblebee");

        player.setOpponent(opponent);

        assertThat(player.getStatus()).isEqualTo(PlayerStatus.BUSY);

        // when
        player.removeOpponent();

        // then
        assertThat(player.getOpponent()).isNull();
        assertThat(player.getStatus()).isEqualTo(PlayerStatus.READY);
    }
}
