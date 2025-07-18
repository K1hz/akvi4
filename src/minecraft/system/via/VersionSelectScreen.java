package minecraft.system.via;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import minecraft.game.advantage.make.color.ColoringSystem;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;


public class VersionSelectScreen
        extends
        TextFieldWidget {


    public VersionSelectScreen(FontRenderer p_i232260_1_, int p_i232260_2_, int p_i232260_3_, int p_i232260_4_, int p_i232260_5_, ITextComponent p_i232260_6_) {
        super(p_i232260_1_, p_i232260_2_, p_i232260_3_, p_i232260_4_, p_i232260_5_, p_i232260_6_);
        setText("1.16.5");
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if (ProtocolVersion.getClosest(getText()) == null) {
            setTextColor(ColoringSystem.getColor(200,20,20,255));
        } else {
            minecraft.system.via.ViaLoadingBase.getInstance().reload(ProtocolVersion.getClosest(getText()));
            setTextColor(ColoringSystem.getColor(255,255,255,255));
        }

    }
}

