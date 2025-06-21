/*
 * This file is part of VarMCP - https://github.com/FlorianMichael/VarMCP
 * Copyright (C) 2020-2024 FlorianMichael/EnZaXD <florian.michael07@gmail.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package minecraft.system.via.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import minecraft.system.via.ViaLoadingBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;


import java.util.Collections;
import java.util.List;

public class AsyncVersionSlider extends Widget {
    private float dragValue = (float) ViaLoadingBase.PROTOCOLS.indexOf(ViaLoadingBase.getInstance().getTargetVersion()) / (ViaLoadingBase.PROTOCOLS.size() - 1);

    private final List<ProtocolVersion> values;
    private float sliderValue;
    public boolean dragging;

    public AsyncVersionSlider(int x, int y , int widthIn, int heightIn) {
        super(x, y, Math.max(widthIn, 110), heightIn, new StringTextComponent(""));
        this.values = ViaLoadingBase.PROTOCOLS;
        Collections.reverse(values);
        this.sliderValue = dragValue;
        this.setMessage(new StringTextComponent(values.get((int) Math.ceil(this.sliderValue * (values.size() - 1))).getName()));
    }

    /*public void func_191745_a(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        super.func_191745_a(mc, mouseX, mouseY, partialTicks);
    }*/

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    protected int getHoverState(boolean mouseOver)
    {
        return 0;
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            if (this.dragging)
            {
                this.sliderValue = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);
                this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);
                this.dragValue = sliderValue;

                // Ceil index to show correctly display string (26.999998 => 27)
                int selectedProtocolIndex = (int) Math.ceil(this.sliderValue * (values.size() - 1));
                this.setMessage(new StringTextComponent(values.get(selectedProtocolIndex).getName()));
                ViaLoadingBase.getInstance().reload(values.get(selectedProtocolIndex));
            }

            mc.getTextureManager().bindTexture(WIDGETS_LOCATION);
            GlStateManager.color(1, 1, 1, 1);
            this.mouseDragged(this.x + (int)(this.sliderValue * (float)(this.width - 8)), this.y, 0, 66, 4);
            this.mouseDragged(this.x + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.y, 196, 66, 4);
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (super.mouseClicked(mouseX, mouseY, 0))
        {
            this.sliderValue = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);
            this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);
            this.dragValue = sliderValue;
            
            int selectedProtocolIndex = (int) Math.ceil(this.sliderValue * (values.size() - 1));
            this.setMessage(new StringTextComponent(values.get(selectedProtocolIndex).getName()));
            ViaLoadingBase.getInstance().reload(values.get(selectedProtocolIndex));
            
            this.dragging = true;
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(int mouseX, int mouseY)
    {
        this.dragging = false;
    }

    public void setVersion(int protocol)
    {
        this.dragValue = (float) ViaLoadingBase.PROTOCOLS.indexOf(ProtocolVersion.getProtocol(protocol)) / (ViaLoadingBase.PROTOCOLS.size() - 1);
        this.sliderValue = this.dragValue;

        int selectedProtocolIndex = (int) Math.ceil(this.sliderValue * (values.size() - 1));
        this.setMessage(new StringTextComponent(values.get(selectedProtocolIndex).getName()));
    }
}
