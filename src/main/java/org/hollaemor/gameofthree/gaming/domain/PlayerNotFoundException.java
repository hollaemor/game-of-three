package org.hollaemor.gameofthree.gaming.domain;

public class PlayerNotFoundException  extends RuntimeException{

    public PlayerNotFoundException(String message) {
        super(message);
    }
}
