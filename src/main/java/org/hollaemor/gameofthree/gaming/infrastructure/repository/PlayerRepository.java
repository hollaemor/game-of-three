package org.hollaemor.gameofthree.gaming.infrastructure.repository;

import org.hollaemor.gameofthree.gaming.domain.Player;

import java.util.Optional;

public interface PlayerRepository {

    void save(Player player);

    Optional<Player> findByName(String playerName);

    boolean exists(String playerName);

    Optional<Player> findAvailableForPlayer(String playerName);

    void delete(Player player);
}
