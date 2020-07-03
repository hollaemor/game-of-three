package org.hollaemor.gameofthree.gaming.storage;

import java.util.Collection;
import java.util.Optional;

import org.hollaemor.gameofthree.gaming.domain.Player;

public interface PlayerStore {

    void save(Player player);

    Collection<Player> getPlayers();

    Optional<Player> findByName(String playerName);

    boolean exists(String playerName);


    Optional<Player> findAvailableForPlayer(String playerName);

    void delete(Player player);
}
