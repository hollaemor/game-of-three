package org.hollaemor.gameofthree.gaming.api;

import org.hollaemor.gameofthree.gaming.datatransfer.GameInstruction;
import org.hollaemor.gameofthree.gaming.datatransfer.GameMessage;
import org.hollaemor.gameofthree.gaming.datatransfer.GameStatus;
import org.hollaemor.gameofthree.gaming.exception.PlayerNotFoundException;
import org.hollaemor.gameofthree.gaming.service.GameService;
import org.hollaemor.gameofthree.gaming.storage.PlayerStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @MockBean
    private GameService gameService;

    @MockBean
    private PlayerStore playerStore;

    private WebSocketStompClient stompClient;

    private String wsUrl;

    private CompletableFuture<Object> completableFuture;


    @BeforeEach
    public void setup() {

        wsUrl = String.format("ws://localhost:%d/game-of-three", port);
        stompClient = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        stompClient.setTaskScheduler(new ConcurrentTaskScheduler());
        completableFuture = new CompletableFuture<>();
    }


    @Test
    public void whenUsernameIsNotSpecified_Then_ExceptionIsThrown() throws Exception {
        // when / then
        assertThatExceptionOfType(ExecutionException.class)
                .isThrownBy(() -> {
                    stompClient.connect(wsUrl, new StompSessionHandlerAdapter() {
                        @Override
                        public void handleFrame(StompHeaders headers, Object payload) {
                            assertThat(headers.getFirst("message")).isEqualTo("username is required to establish a connection");
                        }
                    }).get();
                });
    }


    @Test
    public void whenUsernameAlreadyExist_Then_ExceptionIsThrown() throws Exception {
        // given
        var stompHeaders = new StompHeaders();
        stompHeaders.add("username", "Magneto");

        given(playerStore.exists(anyString()))
                .willReturn(true);

        assertThatExceptionOfType(ExecutionException.class)
                .isThrownBy(() -> {
                    stompClient.connect(wsUrl, new WebSocketHttpHeaders(), stompHeaders, new StompSessionHandlerAdapter() {
                        @Override
                        public void handleFrame(StompHeaders headers, Object payload) {
                            assertThat(headers.getFirst("message")).isEqualTo("Player with username already connected!!");
                        }
                    }).get();
                });

        verify(playerStore).exists(anyString());
    }

    @Test
    public void startEndpoint_Should_CallGameService() throws Exception {

        // given
        given(gameService.startForPlayer(anyString()))
                .willReturn(GameMessage.builder().gameStatus(GameStatus.WAITING).build());

        // when
        StompSession stompSession = createSession(new MappingJackson2MessageConverter());

        stompSession.subscribe("/user/queue/updates", new TestStompFrameHandler(GameMessage.class));
        stompSession.send("/app/game.start", null);

        // then
        var message = (GameMessage) completableFuture.get(3, TimeUnit.SECONDS);

        assertThat(message.getGameStatus()).isEqualTo(GameStatus.WAITING);
    }


    @Test
    public void whenExceptionOccurs_Then_PublishToUserErrorQueue() throws Exception {
        // given
        given(gameService.startForPlayer(anyString()))
                .willThrow(new PlayerNotFoundException("could not find player"));

        // when
        StompSession stompSession = createSession(new StringMessageConverter());
        stompSession.subscribe("/user/queue/errors", new TestStompFrameHandler(String.class));
        stompSession.send("/app/game.start", null);

        // then
        String errorMessage = (String) completableFuture.get(3, TimeUnit.SECONDS);
        assertThat(errorMessage).isEqualTo("could not find player");
    }

    @Test
    public void randomNumberEndpoint_Should_DelegateToGameService() throws Exception {
        // given
        var stompSession = createSession(new MappingJackson2MessageConverter());

        // when / then
        stompSession.setAutoReceipt(true);
        stompSession.send("/app/game.number", GameInstruction.builder().value(10).build())
                .addReceiptTask(() -> verify(gameService).processRandomNumberFromPlayer(eq(10), eq("Xavier")));
    }

    @Test
    public void playEndpoint_Should_DelegateToGameService() throws Exception {
        // given
        var stompSession = createSession(new MappingJackson2MessageConverter());
        var gameInstruction = GameInstruction.builder().value(12).move(0).build();

        // when / then
        stompSession.setAutoReceipt(true);
        stompSession.send("/app/game.play", gameInstruction)
                .addReceiptTask(() -> verify(gameService).processPlayerMove(eq("Xavier"), eq(gameInstruction)));
    }

    private StompSession createSession(MessageConverter messageConverter) throws Exception {
        var stompHeaders = new StompHeaders();
        stompHeaders.add("username", "Xavier");

        stompClient.setMessageConverter(messageConverter);
        return stompClient.connect(wsUrl, new WebSocketHttpHeaders(), stompHeaders, new StompSessionHandlerAdapter() {
            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                exception.printStackTrace();
            }
        }).get(1, TimeUnit.SECONDS);
    }

    class TestStompFrameHandler implements StompFrameHandler {

        private final Class<?> aClass;

        public TestStompFrameHandler(Class clz) {
            this.aClass = clz;
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return aClass;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            completableFuture.complete(payload);
        }
    }

}
