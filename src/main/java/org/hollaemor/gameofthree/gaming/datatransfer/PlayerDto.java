package org.hollaemor.gameofthree.gaming.datatransfer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hollaemor.gameofthree.gaming.domain.PlayerStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDto {

    private String name;
    private PlayerStatus status;
}
