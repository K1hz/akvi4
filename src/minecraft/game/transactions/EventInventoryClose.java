package minecraft.game.transactions;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EventInventoryClose extends CancelEvent {

    public int windowId;

}
