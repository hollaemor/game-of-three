package org.hollaemor.gameofthree.listener;

import lombok.extern.slf4j.Slf4j;
import org.hollaemor.gameofthree.model.Player;
import org.hollaemor.gameofthree.service.PlayerService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
public class WebSocketEventListener {

    private final PlayerService playerService;

    public WebSocketEventListener(PlayerService playerService) {
        this.playerService = playerService;
    }

    @EventListener
    public void handleWebSocketConnected(SessionConnectedEvent event) {
        log.debug("player connected: {}", event.getUser().getName());
        playerService.save(new Player(event.getUser().getName()));

    }


    @EventListener
    public void handleWebSocketDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String playerName = (String)accessor.getSessionAttributes().get("username");
        log.debug("player disconnected: {}", playerName);
        playerService.removePlayer(playerName);
    }
}
