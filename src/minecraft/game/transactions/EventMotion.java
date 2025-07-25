package minecraft.game.transactions;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventMotion extends CancelEvent{
    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;

    Runnable postMotion;
}
