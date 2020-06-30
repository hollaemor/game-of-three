package org.hollaemor.gameofthree.datatransfer;

import lombok.Builder;
import lombok.Getter;

@Builder
public class GameInstruction {

    @Getter
    private int value;

    @Getter
    private int move;
}
