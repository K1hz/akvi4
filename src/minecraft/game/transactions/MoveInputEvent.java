package minecraft.game.transactions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MoveInputEvent extends CancelEvent {
    private float forward, strafe;
    private boolean jump, sneaking;
    private double sneakSlowDownMultiplier;

}