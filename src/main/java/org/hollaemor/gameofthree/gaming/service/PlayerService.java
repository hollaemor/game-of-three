package org.hollaemor.gameofthree.gaming.service;

import org.hollaemor.gameofthree.gaming.datatransfer.GameMessage;
import org.hollaemor.gameofthree.gaming.datatransfer.GameStatus;
import org.hollaemor.gameofthree.gaming.domain.Player;
import org.hollaemor.gameofthree.gaming.storage.PlayerStore;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlayerService {

    private static final String UPDATES_QUEUE = "/queue/updates";

    private final PlayerStore playerStore;

    private final SimpMessagingTemplate messagingTemplate;

    public PlayerService(PlayerStore playerStore, SimpMessagingTemplate messagingTemplate) {
        this.playerStore = playerStore;
        this.messagingTemplate = messagingTemplate;
    }

    public void save(Player player) {
        playerStore.save(player);
    }


    public void removePlayer(String playerName) {
        playerStore.findByName(playerName)
                .ifPresent(player -> {
                    playerStore.delete(player);
                    updateAndNotifyPlayer(player.getOpponent());
                });
    }

    private void updateAndNotifyPlayer(Player player) {
        Optional.ofNullable(player)
                .ifPresent(p -> {
                    String disconnectedPlayerName = player.getOpponent().getName();
                    player.removeOpponent();
                    playerStore.save(p);
                    notifyPlayerOfDisconnect(p, disconnectedPlayerName);
                });
    }

    private void notifyPlayerOfDisconnect(Player player, String disconnectedPlayerName) {
        var message = GameMessage.builder()
                .gameStatus(GameStatus.DISCONNECT)
                .content(String.format("%s disconnected from game", disconnectedPlayerName))
                .build();
        messagingTemplate.convertAndSendToUser(player.getName(), UPDATES_QUEUE, message);
    }
}
