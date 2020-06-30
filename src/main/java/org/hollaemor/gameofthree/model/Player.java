package org.hollaemor.gameofthree.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Data
@ToString(exclude = "opponent")
public class Player {

    private String name;

    private PlayerStatus status;

    private boolean primary;

    @Getter
    @JsonIgnore
    private Player opponent;


    public Player(String name) {
        this.name = name;
        this.status = PlayerStatus.READY;
    }

    public void setOpponent(Player opponent) {
        if (null != opponent) {
            this.opponent = opponent;
            opponent.opponent = this;
            this.status = PlayerStatus.BUSY;
            opponent.status = PlayerStatus.BUSY;
        }
    }

    public void removeOpponent() {
        opponent = null;
        status = PlayerStatus.READY;
    }
}
