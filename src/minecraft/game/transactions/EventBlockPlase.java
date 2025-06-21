package minecraft.game.transactions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

@Getter
@AllArgsConstructor
public class EventBlockPlase extends EventUpdate {
    public BlockPos position;
    public Block block;
    public ItemStack stack;
}