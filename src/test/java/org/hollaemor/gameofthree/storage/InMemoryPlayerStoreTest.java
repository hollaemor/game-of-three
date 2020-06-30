package org.hollaemor.gameofthree.storage;

import org.hollaemor.gameofthree.model.Player;
import org.hollaemor.gameofthree.model.PlayerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryPlayerStoreTest {

    private InMemoryPlayerStore store;

    @BeforeEach
    public void setup() {
        store = new InMemoryPlayerStore();
    }

    @Test
    public void saveShouldPersistUser() {
        // given
        var player = new Player("Player One");

        // when
        store.save(player);

        // then
        assertThat(store.getPlayers()).hasSize(1);
        assertThat(store.getPlayers()).containsExactly(player);
    }


    @Test
    public void findByNameShouldReturnPlayerWithName() {
        // given
        var johnSnow = new Player("John Snow");
        store.save(johnSnow);

        // when
        Optional<Player> playerByName = store.findByName("John Snow");
        Optional<Player> noneExistingPlayer = store.findByName("Khaleesi");

        //then
        assertThat(playerByName).isPresent();
        assertThat(playerByName).hasValue(johnSnow);

        assertThat(noneExistingPlayer).isEmpty();
    }

    @Test
    public void deleteShouldRemovePlayerFromStorage() {
        // given
        var littleFinger = new Player("Little Finger");
        store.save(littleFinger);

        assertThat(store.getPlayers()).hasSize(1);

        // when
        store.delete(littleFinger);

        // then
        assertThat(store.getPlayers()).isEmpty();
    }

    @Test
    public void existShouldReturnTrueIfPlayerWithNameExists() {
        // given / when
        store.save(new Player("Arya Stark"));

        // then
        assertThat(store.exists("Arya Stark")).isTrue();
        assertThat(store.exists("Mountain")).isFalse();
    }

    @Test
    public void findAvailableForPlayerShouldReturnPlayerIfAvailable() {
        // given
        var player = new Player("Theon Grayjoy");

        store.save(player);

        // when
        Optional<Player> available = store.findAvailableForPlayer("Drogo");

        // then
        assertThat(available).hasValue(player);
    }

    @Test
    public void findAvailableForPlayerShouldNotReturnWhenPlayerIsBusy() {
        //given
        var player = new Player("Nerd Stark");
        player.setStatus(PlayerStatus.BUSY);
        store.save(player);

        // when
        Optional<Player> available = store.findAvailableForPlayer("Kings Landing");

        // then
        assertThat(available).isEmpty();
    }

    @Test
    public void findAvailableForPlayerShouldNotReturnRequestingPlayer() {
        //given
        var player = new Player("Rob Barathian");
        store.save(player);

        // when
        Optional<Player> available = store.findAvailableForPlayer("Rob Barathian");

        // then
        assertThat(available).isEmpty();
    }
}
