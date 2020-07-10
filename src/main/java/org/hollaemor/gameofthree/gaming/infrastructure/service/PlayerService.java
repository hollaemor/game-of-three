package org.hollaemor.gameofthree.gaming.infrastructure.service;

import org.hollaemor.gameofthree.gaming.domain.GameMessage;
import org.hollaemor.gameofthree.gaming.domain.Player;
import org.hollaemor.gameofthree.gaming.infrastructure.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import static java.util.Optional.ofNullable;
import static org.hollaemor.gameofthree.gaming.domain.GameMessageFactory.buildDisconnectMessage;

@Service
public class PlayerService {


    private final PlayerRepository playerRepository;
    private NotificationService notificationService;


    public PlayerService(PlayerRepository playerRepository, NotificationService notificationService) {
        this.playerRepository = playerRepository;
        this.notificationService = notificationService;
    }

    public void save(Player player) {
        playerRepository.save(player);
    }


    public void removePlayer(String playerName) {
        playerRepository.findByName(playerName)
                .ifPresent(player -> {
                    playerRepository.delete(player);
                    updateAndNotifyPlayer(player.getOpponent());
                });
    }

    private void updateAndNotifyPlayer(Player player) {
        ofNullable(player)
                .ifPresent(p -> {
                    var disconnectedMessage = buildDisconnectMessage(p.getOpponent().getName());
                    player.removeOpponent();
                    playerRepository.save(p);
                    notifyPlayerOfDisconnect(p, disconnectedMessage);
                });
    }

    private void notifyPlayerOfDisconnect(Player player, GameMessage message) {
        notificationService.notifyPlayer(player.getName(), message);
    }
}
