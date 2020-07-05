package org.hollaemor.gameofthree.gaming.infrastructure.repository;

import org.hollaemor.gameofthree.gaming.domain.Player;
import org.hollaemor.gameofthree.gaming.domain.PlayerStatus;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Repository
public class InMemoryPlayerRepository implements PlayerRepository {


    private final Map<String, Player> store;

    public InMemoryPlayerRepository() {
        store = new HashMap<>();
    }

    @Override
    public void save(Player player) {
        store.put(player.getName(), player);
    }

    @Override
    public Optional<Player> findByName(String playerName) {
        return ofNullable(store.get(playerName));
    }

    @Override
    public Optional<Player> findAvailableForPlayer(String playerName) {
        return store.values().stream().
                filter(p -> p.getStatus() == PlayerStatus.AVAILABLE && !p.getName().equals(playerName))
                .findFirst();
    }

    @Override
    public void delete(Player player) {
        store.remove(player.getName());
    }

    @Override
    public boolean exists(String playerName) {
        return store.containsKey(playerName);
    }
}
