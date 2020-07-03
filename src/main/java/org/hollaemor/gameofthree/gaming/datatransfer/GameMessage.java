package org.hollaemor.gameofthree.gaming.datatransfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameMessage {

    @Getter
    private GameStatus gameStatus;

    @Getter
    private String content;

    @Getter
    private String opponent;

    @Getter
    private boolean primaryPlayer;

    @Getter
    private int value;

    @Getter
    private int play;

    @Getter
    private boolean winner;
}
