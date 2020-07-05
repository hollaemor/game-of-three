package org.hollaemor.gameofthree.gaming.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
public class GameInstruction {

    @Getter
    private int value;

    @Getter
    private int move;
}
