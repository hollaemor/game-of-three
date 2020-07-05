package org.hollaemor.gameofthree.gaming.domain;

public class OpponentDoesNotExistException  extends RuntimeException{

    public OpponentDoesNotExistException(String message) {
        super(message);
    }
}
