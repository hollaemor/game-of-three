package org.hollaemor.gameofthree.gaming.domain;

public class InvalidCombinationException extends RuntimeException{

    public InvalidCombinationException(String message) {
        super(message);
    }
}
