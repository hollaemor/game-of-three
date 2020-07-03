package org.hollaemor.gameofthree.gaming.config;

import org.hollaemor.gameofthree.gaming.storage.InMemoryPlayerStore;
import org.hollaemor.gameofthree.gaming.storage.PlayerStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public PlayerStore playerStore() {
        return new InMemoryPlayerStore();
    }
}
