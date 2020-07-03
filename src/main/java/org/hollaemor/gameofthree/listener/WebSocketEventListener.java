package org.hollaemor.gameofthree.listener;

import lombok.extern.slf4j.Slf4j;
import org.hollaemor.gameofthree.gaming.config.WebSocketConfig;
import org.hollaemor.gameofthree.gaming.domain.Player;
import org.hollaemor.gameofthree.gaming.service.PlayerService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Optional;

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

        Optional.ofNullable(accessor.getSessionAttributes().get(WebSocketConfig.USERNAME_HEADER))
                .map(String.class::cast)
                .ifPresent(username -> {
                    log.debug("player disconnected: {}", username);
                    playerService.removePlayer(username);
                });
    }
}
