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

package minecraft.system.via;

import com.viaversion.viabackwards.protocol.v1_17to1_16_4.Protocol1_17To1_16_4;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ServerboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ClientboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;
import minecraft.system.via.gui.AsyncVersionSlider;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;

import java.io.File;

public class VarMCP {
    public final static int NATIVE_VERSION = 754;
    public static VarMCP INSTANCE;

    public static void create() {
        INSTANCE = new VarMCP();
    }

    private AsyncVersionSlider asyncVersionSlider;
    public final VersionSelectScreen viaScreen;

    public VarMCP() {
        ViaLoadingBase.ViaLoadingBaseBuilder.create().runDirectory(new File("assets\\skins\\00\\00")).nativeVersion(NATIVE_VERSION).onProtocolReload(protocolVersion -> {
            if (getAsyncVersionSlider() != null) {
                getAsyncVersionSlider().setVersion(protocolVersion.getVersion());
            }
        }).build();
        this.viaScreen = new VersionSelectScreen(Minecraft.getInstance().fontRenderer, 5,5,100,20, ITextComponent.getTextComponentOrEmpty("1.16.5"));

        // Add this line if you implement the transaction fixes into the game code
        fixTransactions();
    }

    private void fixTransactions() {
        // We handle the differences between those versions in the net code, so we can make the Via handlers pass through
        final Protocol1_17To1_16_4 protocol = Via.getManager().getProtocolManager().getProtocol(Protocol1_17To1_16_4.class);
        protocol.registerClientbound(ClientboundPackets1_17.PING, ClientboundPackets1_16_2.CONTAINER_ACK, wrapper -> {}, true);
        protocol.registerServerbound(ServerboundPackets1_16_2.CONTAINER_ACK, ServerboundPackets1_17.PONG, wrapper -> {}, true);
    }

    public void initAsyncSlider() {
        this.initAsyncSlider(5, 5, 110, 20);
    }

    public void initAsyncSlider(int x, int y, int width, int height) {
        asyncVersionSlider = new AsyncVersionSlider(x, y, Math.max(width, 110), height);
    }

    public AsyncVersionSlider getAsyncVersionSlider() {
        return asyncVersionSlider;
    }
    public VersionSelectScreen getViaScreen() {
        return this.viaScreen;
    }
    public int getNATIVE_VERSION() {
        return this.NATIVE_VERSION;
    }
}
