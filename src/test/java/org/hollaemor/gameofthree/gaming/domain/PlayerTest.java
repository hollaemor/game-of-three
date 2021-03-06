package org.hollaemor.gameofthree.gaming.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PlayerTest {

    @Test
    public void whenPlayerIsCreated_Then_StatusIsAvailable() {
        // given / when
        var player = new Player("Transformers");

        // then
        assertThat(player.getStatus()).isEqualTo(PlayerStatus.AVAILABLE);
        assertThat(player.getName()).isEqualTo("Transformers");
        assertThat(player.isPrimary()).isFalse();
        assertThat(player.getOpponent()).isNull();
        assertThat(player.hasOpponent()).isFalse();
    }


    @Test
    public void whenOpponentIsSet_Then_UpdatePlayerStatuses() {
        // given
        var player = new Player("Decepticons");
        var opponent = new Player("Autobots");

        // when
        player.setOpponent(opponent);

        // then
        assertThat(player.getStatus()).isEqualTo(PlayerStatus.PAIRED);
        assertThat(player.getOpponent()).isEqualTo(opponent);
        assertThat(opponent.getStatus()).isEqualTo(PlayerStatus.PAIRED);
        assertThat(opponent.getOpponent()).isEqualTo(player);
        assertThat(player.hasOpponent()).isTrue();
    }

    @Test
    public void removeOpponent_Should_RemoveOpponent_And_UpdateState() {
        // given
        var player = new Player("Optimus Prime");
        var opponent = new Player("Bumblebee");

        player.setOpponent(opponent);

        assertThat(player.getStatus()).isEqualTo(PlayerStatus.PAIRED);

        // when
        player.removeOpponent();

        // then
        assertThat(player.getOpponent()).isNull();
        assertThat(player.getStatus()).isEqualTo(PlayerStatus.AVAILABLE);
    }
}
