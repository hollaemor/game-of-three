package org.hollaemor.gameofthree.gaming.infrastructure.service;

import org.hollaemor.gameofthree.gaming.domain.GameMessageFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService service;


    @Test
    public void notifyPlayer_Should_SendMessageToPlayer() {
        // given
        var playerName = "Beats";
        var gameMessage = GameMessageFactory.buildPlayMessage(50);


        // then
        service.notifyPlayer(playerName, gameMessage);

        // then
        verify(messagingTemplate).convertAndSendToUser(eq(playerName), eq("/queue/updates"), eq(gameMessage));
    }
}
