package org.hollaemor.gameofthree.gaming.api;

import org.hollaemor.gameofthree.gaming.domain.GameInstruction;
import org.hollaemor.gameofthree.gaming.domain.GameMessage;
import org.hollaemor.gameofthree.gaming.domain.GameService;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

import static org.hollaemor.gameofthree.gaming.infrastructure.WebSocketConfig.USERNAME_HEADER;

@Controller
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/game.start")
    @SendToUser("/queue/updates")
    public GameMessage startGame(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put(USERNAME_HEADER, principal.getName());
        return gameService.startForPlayer(principal.getName());
    }

    @MessageMapping("/game.number")
    public void randomNumber(GameInstruction gameInstruction, Principal principal) {
        gameService.processRandomNumberFromPlayer(gameInstruction.getValue(), principal.getName());
    }

    @MessageMapping("/game.play")
    public void playerMove(GameInstruction gameInstruction, Principal principal) {
        gameService.processPlayerMove(principal.getName(), gameInstruction);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable throwable) {
        return throwable.getMessage();
    }
}
