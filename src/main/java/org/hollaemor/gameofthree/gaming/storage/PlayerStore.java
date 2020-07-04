package org.hollaemor.gameofthree.gaming.storage;

import org.hollaemor.gameofthree.gaming.domain.Player;

import java.util.Optional;

public interface PlayerStore {

    void save(Player player);

    Optional<Player> findByName(String playerName);

    boolean exists(String playerName);


    Optional<Player> findAvailableForPlayer(String playerName);

    void delete(Player player);
}
