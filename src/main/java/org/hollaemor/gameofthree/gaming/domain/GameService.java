package org.hollaemor.gameofthree.gaming.domain;

import lombok.extern.slf4j.Slf4j;
import org.hollaemor.gameofthree.gaming.infrastructure.repository.PlayerRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import static java.util.Optional.ofNullable;
import static org.hollaemor.gameofthree.gaming.domain.GameMessageFactory.*;

@Slf4j
@Service
public class GameService {

    private static final String UPDATE_QUEUE = "/queue/updates";
    private static final int DIVISOR = 3;

    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;


    public GameService(PlayerRepository playerRepository, SimpMessagingTemplate messagingTemplate) {
        this.playerRepository = playerRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public GameMessage startForPlayer(String playerName) {
        return playerRepository.findByName(playerName)
                .map(this::processStartRequestForPlayer)
                .orElseThrow(() -> makePlayerNotFoundException(playerName));
    }

    public void processRandomNumberFromPlayer(int randomNumber, String playerName) {
        playerRepository.findByName(playerName)
                .ifPresentOrElse(player -> {
                    checkPlayerHasOpponent(player);
                    messagingTemplate.convertAndSendToUser(player.getOpponent().getName(), UPDATE_QUEUE, buildPlayMessage(randomNumber));
                }, () -> throwPlayerNotFoundException(playerName));
    }


    public void processPlayerMove(String playerName, GameInstruction gameInstruction) {
        int addition = gameInstruction.getValue() + gameInstruction.getMove();

        checkDivisibleByThree(addition);

        playerRepository.findByName(playerName).ifPresentOrElse(player -> {
            checkPlayerHasOpponent(player);

            int newValueAfterDivision = addition / DIVISOR;

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

        return ofNullable(player.getOpponent())
                .map(opponent -> {
                    notifyPlayer(opponent.getName(), buildStartMessageForPlayer(opponent));
                    return buildStartMessageForPlayer(player);
                }).orElseGet(
                        () -> playerRepository.findAvailableForPlayer(player.getName())
                                .map(availablePlayer -> {
                                    availablePlayer.setPrimary(true);
                                    player.setPrimary(false);

                                    availablePlayer.setOpponent(player);

                                    savePlayerChanges(availablePlayer);
                                    notifyPlayer(availablePlayer.getName(), buildStartMessageForPlayer(availablePlayer));

                                    return buildStartMessageForPlayer(player);
                                }).orElseGet(GameMessageFactory::buildWaitingMessage)
                );
    }


    private void notifyPlayer(String playerName, GameMessage message) {
        messagingTemplate.convertAndSendToUser(playerName, UPDATE_QUEUE, message);
    }

    private void savePlayerChanges(Player player) {
        playerRepository.save(player);
        ofNullable(player.getOpponent()).ifPresent(playerRepository::save);
    }


    private void checkPlayerHasOpponent(Player player) {
        if (!player.hasOpponent()) {
            throw new OpponentDoesNotExistException("You have not been paired with an opponent");
        }
    }

    private void checkDivisibleByThree(int number) {
        if (number % DIVISOR != 0) {
            throw new InvalidCombinationException(String.format("%d is not divisible by %d", number, DIVISOR));
        }
    }

    private void logPlayerMove(String playerName, GameInstruction gameInstruction, int updatedGameValue) {
        log.debug("{} got value: {} and added {} to get {}. Result after division by {}: {}",
                playerName,
                gameInstruction.getValue(),
                gameInstruction.getMove(),
                gameInstruction.getValue() + gameInstruction.getMove(),
                DIVISOR,
                updatedGameValue);
    }


    private void throwPlayerNotFoundException(String playerName) {
        throw makePlayerNotFoundException(playerName);
    }

    private PlayerNotFoundException makePlayerNotFoundException(String playerName) {
        return new PlayerNotFoundException(String.format("Player not found: %s", playerName));
    }
}
