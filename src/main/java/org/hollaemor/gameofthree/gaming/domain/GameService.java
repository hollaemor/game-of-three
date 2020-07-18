package org.hollaemor.gameofthree.gaming.domain;

import lombok.extern.slf4j.Slf4j;
import org.hollaemor.gameofthree.gaming.infrastructure.repository.PlayerRepository;
import org.hollaemor.gameofthree.gaming.infrastructure.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.hollaemor.gameofthree.gaming.domain.GameMessageFactory.*;

@Slf4j
@Service
public class GameService {

    private static final int DIVISOR = 3;

    private final PlayerRepository playerRepository;
    private final NotificationService notificationService;


    public GameService(PlayerRepository playerRepository, NotificationService notificationService) {
        this.playerRepository = playerRepository;
        this.notificationService = notificationService;
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
                    notificationService.notifyPlayer(player.getOpponent().getName(), buildPlayMessage(randomNumber));
                }, () -> throwPlayerNotFoundException(playerName));
    }


    public void processPlayerMove(String playerName, GameInstruction gameInstruction) {
        int addition = gameInstruction.getValue() + gameInstruction.getMove();

        checkDivisibleByDivisor(addition);

        playerRepository.findByName(playerName).ifPresentOrElse(player -> {
            checkPlayerHasOpponent(player);

            int newValueAfterDivision = addition / DIVISOR;

            logPlayerMove(playerName, gameInstruction, newValueAfterDivision);

            if (newValueAfterDivision != 1) {
                notificationService.notifyPlayer(player.getOpponent().getName(), buildPlayMessage(newValueAfterDivision));
            } else {
                notificationService.notifyPlayer(player.getName(), buildGameOverMessage(true));
                notificationService.notifyPlayer(player.getOpponent().getName(), buildGameOverMessage(false));
            }

        }, () -> throwPlayerNotFoundException(playerName));
    }

    private GameMessage processStartRequestForPlayer(Player player) {

        return ofNullable(player.getOpponent())
                .map(this::rematchWithOpponent).orElseGet(
                        () -> pairPlayerWithAvailablePlayer(player).orElseGet(GameMessageFactory::buildWaitingMessage)
                );
    }


    private GameMessage rematchWithOpponent(Player player) {
        notificationService.notifyPlayer(player.getName(), buildStartMessageForPlayer(player));
        return buildStartMessageForPlayer(player.getOpponent());
    }

    private Optional<GameMessage> pairPlayerWithAvailablePlayer(Player player) {
        return playerRepository.findAvailableForPlayer(player.getName())
                .map(availablePlayer -> {

                    availablePlayer.setPrimary(true);
                    player.setPrimary(false);

                    availablePlayer.setOpponent(player);

                    savePlayerChanges(availablePlayer);
                    notificationService.notifyPlayer(availablePlayer.getName(), buildStartMessageForPlayer(availablePlayer));

                    return buildStartMessageForPlayer(player);
                });
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

    private void checkDivisibleByDivisor(int number) {
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
