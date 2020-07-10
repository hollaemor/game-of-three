package org.hollaemor.gameofthree.gaming.infrastructure.service;

import org.hollaemor.gameofthree.gaming.domain.GameMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final String UPDATE_QUEUE = "/queue/updates";

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyPlayer(String playerName, GameMessage message) {
        messagingTemplate.convertAndSendToUser(playerName, UPDATE_QUEUE, message);
    }
}
