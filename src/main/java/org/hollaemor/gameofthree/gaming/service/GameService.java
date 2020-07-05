package org.hollaemor.gameofthree.gaming.service;

import lombok.extern.slf4j.Slf4j;
import org.hollaemor.gameofthree.gaming.exception.InvalidCombinationException;
import org.hollaemor.gameofthree.gaming.exception.OpponentDoesNotExistException;
import org.hollaemor.gameofthree.gaming.exception.PlayerNotFoundException;
import org.hollaemor.gameofthree.gaming.datatransfer.GameInstruction;
import org.hollaemor.gameofthree.gaming.datatransfer.GameMessage;
import org.hollaemor.gameofthree.gaming.datatransfer.GameStatus;
import org.hollaemor.gameofthree.gaming.domain.Player;
import org.hollaemor.gameofthree.gaming.storage.PlayerStore;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class GameService {

    private static final String UPDATE_QUEUE = "/queue/updates";

    private final PlayerStore playerStore;
    private final SimpMessagingTemplate messagingTemplate;


    public GameService(PlayerStore playerStore, SimpMessagingTemplate messagingTemplate) {
        this.playerStore = playerStore;
        this.messagingTemplate = messagingTemplate;
    }

    public GameMessage startForPlayer(String playerName) {
        return playerStore.findByName(playerName)
                .map(this::processStartRequestForPlayer)
                .orElseThrow(() -> makePlayerNotFoundException(playerName));
    }

    public void processRandomNumberFromPlayer(int randomNumber, String playerName) {
        playerStore.findByName(playerName)
                .ifPresentOrElse(player -> {
                    checkOpponentExists(player);
                    messagingTemplate.convertAndSendToUser(player.getOpponent().getName(), UPDATE_QUEUE, buildPlayMessage(randomNumber));
                }, () -> throwPlayerNotFoundException(playerName));
    }


    public void processPlayerMove(String playerName, GameInstruction gameInstruction) {
        int addition = gameInstruction.getValue() + gameInstruction.getMove();

        checkDivisibleByThree(addition);

        playerStore.findByName(playerName).ifPresentOrElse(player -> {
            checkOpponentExists(player);

            int newValueAfterDivision = addition / 3;

            logPlayerMove(playerName, gameInstruction, newValueAfterDivision);

            if (newValueAfterDivision != 1) {
                notifyPlayer(player.getOpponent().getName(), buildPlayMessage(newValueAfterDivision));
            } else {
                notifyPlayer(player.getName(), buildGameOverMessage(true));
                notifyPlayer(player.getOpponent().getName(), buildGameOverMessage(false));
            }

        }, () -> throwPlayerNotFoundException(playerName));
    }

    private GameMessage processStartRequestForPlayer(Player player) {

        return Optional.ofNullable(player.getOpponent())
                .map(opponent -> {
                    notifyPlayer(opponent.getName(), buildStartMessageForPlayer(opponent));
                    return buildStartMessageForPlayer(player);
                }).orElseGet(
                        () -> playerStore.findAvailableForPlayer(player.getName())
                                .map(availablePlayer -> {
                                    availablePlayer.setPrimary(true);
                                    player.setPrimary(false);

                                    availablePlayer.setOpponent(player);

                                    savePlayerChanges(availablePlayer);
                                    notifyPlayer(availablePlayer.getName(), buildStartMessageForPlayer(availablePlayer));

                                    return buildStartMessageForPlayer(player);
                                }).orElseGet(this::buildWaitingMessage)
                );
    }


    private void notifyPlayer(String playerName, GameMessage message) {
        messagingTemplate.convertAndSendToUser(playerName, UPDATE_QUEUE, message);
    }

    private void savePlayerChanges(Player player) {
        playerStore.save(player);
        Optional.ofNullable(player.getOpponent()).ifPresent(playerStore::save);
    }

    private GameMessage buildStartMessageForPlayer(Player player) {
        return GameMessage.builder()
                .gameStatus(GameStatus.START)
                .opponent(player.getOpponent().getName())
                .primaryPlayer(player.isPrimary())
                .content(String.format("%s requested a game session", player.getOpponent().getName()))
                .build();
    }

    private GameMessage buildWaitingMessage() {
        return GameMessage.builder()
                .primaryPlayer(true)
                .gameStatus(GameStatus.WAITING)
                .content("Waiting for available player")
                .build();
    }

    private GameMessage buildPlayMessage(int value) {
        return GameMessage.builder()
                .gameStatus(GameStatus.PLAY)
                .value(value)
                .build();
    }

    private GameMessage buildGameOverMessage(boolean winner) {
        return GameMessage.builder()
                .gameStatus(GameStatus.GAMEOVER)
                .winner(winner)
                .build();
    }

    private void checkOpponentExists(Player player) {
        if (Objects.isNull(player.getOpponent())) {
            throw new OpponentDoesNotExistException("You have not been paired with an opponent");
        }
    }

    private void checkDivisibleByThree(int number) {
        if (number % 3 != 0) {
            throw new InvalidCombinationException(String.format("%d is not divisible by 3", number));
        }
    }

    private void logPlayerMove(String playerName, GameInstruction gameInstruction, int updatedGameValue) {
        log.debug("Player [{}] got value: {} and added {}. Result after division: {}",
                playerName,
                gameInstruction.getValue(),
                gameInstruction.getMove(),
                updatedGameValue);
    }


    private void throwPlayerNotFoundException(String playerName) {
        throw makePlayerNotFoundException(playerName);
    }

    private PlayerNotFoundException makePlayerNotFoundException(String playerName) {
        return new PlayerNotFoundException(String.format("Player not found: %s", playerName));
    }
}
