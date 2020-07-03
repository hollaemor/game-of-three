package org.hollaemor.gameofthree.gaming.storage;

import org.hollaemor.gameofthree.gaming.domain.Player;
import org.hollaemor.gameofthree.gaming.domain.PlayerStatus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryPlayerStore implements PlayerStore {


    private final Map<String, Player> store;

    public InMemoryPlayerStore() {
        store = new HashMap<>();
    }

    @Override
    public void save(Player player) {
        store.put(player.getName(), player);
    }

    @Override
    public Collection<Player> getPlayers() {
        return store.values();
    }

    @Override
    public Optional<Player> findByName(String playerName) {
        return store.containsKey(playerName) ? Optional.of(store.get(playerName)) : Optional.empty();
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
