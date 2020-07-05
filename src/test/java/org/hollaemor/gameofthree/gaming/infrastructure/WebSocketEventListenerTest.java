package org.hollaemor.gameofthree.gaming.infrastructure;

import org.hollaemor.gameofthree.gaming.domain.Player;
import org.hollaemor.gameofthree.gaming.infrastructure.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class WebSocketEventListenerTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private SessionConnectedEvent sessionConnectedEvent;

    @InjectMocks
    private WebSocketEventListener listener;

    @Captor
    private ArgumentCaptor<Player> playerCaptor;


    @Test
    public void whenSessionIsConnected_Then_PlayerIsSaved() {
        // given
        given(sessionConnectedEvent.getUser())
                .willReturn(() -> "Sheldon");

        // when
        listener.handleWebSocketConnected(sessionConnectedEvent);

        // then
        verify(playerService).save(playerCaptor.capture());

        assertThat(playerCaptor.getValue().getName()).isEqualTo("Sheldon");
    }

    @Test
    public void whenSessionIsDisconnected_Then_PlayerIsRemoved() {
        // given
        var sessionMap = new HashMap<String, Object>();
        sessionMap.put("username", "Penny");

        var message = MessageBuilder.withPayload(new byte[0])
                .setHeader(StompHeaderAccessor.SESSION_ATTRIBUTES, sessionMap)
                .build();

        var sessionDisconnectEvent = new SessionDisconnectEvent(new Object(), message, "sessionId", CloseStatus.NORMAL);

        // when
        listener.handleWebSocketDisconnected(sessionDisconnectEvent);

        // then
        verify(playerService).removePlayer(eq("Penny"));
    }
}
