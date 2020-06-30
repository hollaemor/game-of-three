package org.hollaemor.gameofthree.service;

import org.hollaemor.gameofthree.datatransfer.GameMessage;
import org.hollaemor.gameofthree.datatransfer.GameStatus;
import org.hollaemor.gameofthree.model.Player;
import org.hollaemor.gameofthree.storage.PlayerStore;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    private static final String USER_TOPIC = "/topic/users";

    private static final String UPDATES_QUEUE = "/queue/updates";

    private final PlayerStore playerStore;

    private final SimpMessagingTemplate messagingTemplate;

    public PlayerService(PlayerStore playerStore, SimpMessagingTemplate messagingTemplate) {
        this.playerStore = playerStore;
        this.messagingTemplate = messagingTemplate;
    }

    public void save(Player player) {
        playerStore.save(player);
        publishOnlinePlayers();
    }


    public void removePlayer(String playerName) {
        playerStore.findByName(playerName)
                .ifPresent(player -> {
                    updateAndNotifyPlayer(player.getOpponent());
                    playerStore.delete(player);
                    publishOnlinePlayers();
                });
    }

    private void updateAndNotifyPlayer(Player player) {
        if (null != player) {
            String disconnectedPlayerName = player.getOpponent().getName();
            player.removeOpponent();
            playerStore.save(player);
            notifyPlayerOfDisconnect(player, disconnectedPlayerName);
        }
    }

    private void notifyPlayerOfDisconnect(Player player, String disconnectedPlayerName) {
        var message = GameMessage.builder()
                .gameStatus(GameStatus.DISCONNECT)
                .content(String.format("%s disconnected from game", disconnectedPlayerName))
                .build();
        messagingTemplate.convertAndSendToUser(player.getName(), UPDATES_QUEUE, message);
    }

    private void publishOnlinePlayers() {
        messagingTemplate.convertAndSend(USER_TOPIC, playerStore.getPlayers());
    }
}
