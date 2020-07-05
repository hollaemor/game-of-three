package org.hollaemor.gameofthree.gaming.infrastructure.repository;

import org.hollaemor.gameofthree.gaming.domain.Player;
import org.hollaemor.gameofthree.gaming.domain.PlayerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryPlayerRepositoryTest {

    private InMemoryPlayerRepository repository;

    @BeforeEach
    public void setup() {
        repository = new InMemoryPlayerRepository();
    }


    @Test
    public void findByName_Should_ReturnPlayerWithName() {
        // given
        var johnSnow = new Player("John Snow");
        repository.save(johnSnow);

        // when
        Optional<Player> playerByName = repository.findByName("John Snow");
        Optional<Player> noneExistingPlayer = repository.findByName("Khaleesi");

        //then
        assertThat(playerByName).isPresent();
        assertThat(playerByName).hasValue(johnSnow);

        assertThat(noneExistingPlayer).isEmpty();
    }

    @Test
    public void delete_Should_RemovePlayerFromStorage() {
        // given
        var littleFinger = new Player("Little Finger");
        repository.save(littleFinger);

        assertThat(repository.findByName("Little Finger")).isPresent();
        assertThat(repository.findByName("Little Finger")).hasValue(littleFinger);
        // when
        repository.delete(littleFinger);

        // then
        assertThat(repository.findByName("Little Finger")).isNotPresent();
    }

    @Test
    public void exist_Should_ReturnTrueIfPlayerWithNameExists() {
        // given / when
        repository.save(new Player("Arya Stark"));

        // then
        assertThat(repository.exists("Arya Stark")).isTrue();
        assertThat(repository.exists("Mountain")).isFalse();
    }

    @Test
    public void findAvailableForPlayer_Should_ReturnPlayerIfAvailable() {
        // given
        var player = new Player("Theon Grayjoy");

        repository.save(player);

        // when
        Optional<Player> available = repository.findAvailableForPlayer("Drogo");

        // then
        assertThat(available).hasValue(player);
    }

    @Test
    public void findAvailableForPlayer_Should_NotReturnWhenPlayerIsPaired() {
        //given
        var player = new Player("Nerd Stark");
        player.setStatus(PlayerStatus.PAIRED);
        repository.save(player);

        // when
        Optional<Player> available = repository.findAvailableForPlayer("Kings Landing");

        // then
        assertThat(available).isEmpty();
    }

    @Test
    public void findAvailableForPlayer_Should_NotReturnRequestingPlayer() {
        //given
        var player = new Player("Rob Barathian");
        repository.save(player);

        // when
        Optional<Player> available = repository.findAvailableForPlayer("Rob Barathian");

        // then
        assertThat(available).isEmpty();
    }
}
