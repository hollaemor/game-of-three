package org.hollaemor.gameofthree.gaming.domain;

import lombok.Data;
import lombok.ToString;

import static java.util.Optional.ofNullable;

@Data
@ToString(exclude = "opponent")
public class Player {

    private String name;

    private PlayerStatus status;

    private boolean primary;

    private Player opponent;


    public Player(String name) {
        this.name = name;
        this.status = PlayerStatus.AVAILABLE;
    }

    public void setOpponent(Player opponent) {
        ofNullable(opponent)
                .ifPresent(opp -> {
                    this.opponent = opp;
                    opp.opponent = this;
                    this.status = PlayerStatus.PAIRED;
                    opp.status = PlayerStatus.PAIRED;
                });
    }

    public boolean hasOpponent() {
        return null != opponent;
    }

    public void removeOpponent() {
        opponent = null;
        status = PlayerStatus.AVAILABLE;
    }
}
