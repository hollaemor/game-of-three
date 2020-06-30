package org.hollaemor.gameofthree.listener;

import org.hollaemor.gameofthree.model.Player;
import org.hollaemor.gameofthree.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class WebSocketEventListenerTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private SessionConnectedEvent sessionConnectedEvent;

    @Mock
    private SessionDisconnectEvent sessionDisconnectEvent;

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

}
