package minecraft.game.transactions;

import com.mojang.blaze3d.matrix.MatrixStack;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class EventWorld {
    private MatrixStack stack;
    private float partialTicks;

    public EventWorld(MatrixStack stack, float partialTicks)
    {
        this.stack = stack;
        this.partialTicks = partialTicks;
    }


}
