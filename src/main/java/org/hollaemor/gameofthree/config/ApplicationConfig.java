package org.hollaemor.gameofthree.config;

import org.hollaemor.gameofthree.storage.InMemoryPlayerStore;
import org.hollaemor.gameofthree.storage.PlayerStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public PlayerStore playerStore() {
        return new InMemoryPlayerStore();
    }

}
