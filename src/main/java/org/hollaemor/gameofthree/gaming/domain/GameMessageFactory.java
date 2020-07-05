package org.hollaemor.gameofthree.gaming.domain;

public class GameMessageFactory {

    public static GameMessage buildWaitingMessage() {
        return GameMessage.builder()
                .gameStatus(GameStatus.WAITING)
                .primaryPlayer(true)
                .content("Waiting for available player")
                .build();
    }

    public static GameMessage buildPlayMessage(int value) {
        return GameMessage.builder()
                .gameStatus(GameStatus.PLAY)
                .value(value)
                .build();
    }

    public static GameMessage buildGameOverMessage(boolean winner) {
        return GameMessage.builder()
                .gameStatus(GameStatus.GAMEOVER)
                .winner(winner)
                .build();
    }

    public static GameMessage buildStartMessageForPlayer(Player player) {
        return GameMessage
                .builder()
                .gameStatus(GameStatus.START)
                .opponent(player.getOpponent().getName())
                .primaryPlayer(player.isPrimary())
                .content(String.format("%s requested a game session", player.getOpponent().getName()))
                .build();
    }

    public static GameMessage buildDisconnectMessage(String disconnectedPlayerName) {
        return GameMessage.builder()
                .gameStatus(GameStatus.DISCONNECT)
                .content(String.format("%s disconnected from game", disconnectedPlayerName))
                .build();
    }
}
