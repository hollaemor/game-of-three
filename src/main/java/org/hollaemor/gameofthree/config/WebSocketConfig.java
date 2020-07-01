package org.hollaemor.gameofthree.config;

import org.hollaemor.gameofthree.storage.PlayerStore;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.RequestUpgradeStrategy;
import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;

import java.util.Optional;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final PlayerStore playerStore;

    public WebSocketConfig(PlayerStore playerStore) {
        this.playerStore = playerStore;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        RequestUpgradeStrategy upgradeStrategy = new TomcatRequestUpgradeStrategy();
        registry.addEndpoint("/game-of-three")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic/", "/queue/")
        ;
        registry.setApplicationDestinationPrefixes("/app");
        registry.setPreservePublishOrder(true);
    }


    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new AuthenticatedPlayerChannelInterceptor(playerStore));
    }

    static class AuthenticatedPlayerChannelInterceptor implements ChannelInterceptor {

        private final PlayerStore playerStore;

        public AuthenticatedPlayerChannelInterceptor(PlayerStore playerStore) {
            this.playerStore = playerStore;
        }

        public final static String USERNAME_HEADER = "username";

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                Optional.ofNullable(accessor.getFirstNativeHeader(USERNAME_HEADER))
                        .filter( username -> !StringUtils.isEmpty(username))
                        .ifPresentOrElse(username -> {
                            checkPlayerDoesNotExist(username);
                            accessor.setUser(() -> username);

                        }, () -> throwMessagingException("username is required to establish a connection"));
            }

            return message;
        }

        private void checkPlayerDoesNotExist(String username) {
            if (playerStore.exists(username)) {
                throwMessagingException("Player with username already connected!!");
            }
        }

        private void throwMessagingException(String errorMessage) {
            throw new MessagingException(errorMessage);
        }
    }

}
