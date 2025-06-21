package net.minecraft.network.play.server;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import minecraft.system.via.ViaLoadingBase;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class SConfirmTransactionPacket implements IPacket<IClientPlayNetHandler>
{
    private int windowId;
    private short actionNumber;
    private boolean accepted;

    public SConfirmTransactionPacket()
    {
    }

    public SConfirmTransactionPacket(int windowIdIn, short actionNumberIn, boolean acceptedIn)
    {
        this.windowId = windowIdIn;
        this.actionNumber = actionNumberIn;
        this.accepted = acceptedIn;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(IClientPlayNetHandler handler)
    {
        handler.handleConfirmTransaction(this);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException {
        /*this.windowId = buf.readUnsignedByte();
        this.actionNumber = buf.readShort();
        this.accepted = buf.readBoolean();*/
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            this.windowId = buf.readInt();
        } else {
            this.windowId = buf.readUnsignedByte();
            this.actionNumber = buf.readShort();
            this.accepted = buf.readBoolean();
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException {
        /*buf.writeByte(this.windowId);
        buf.writeShort(this.actionNumber);
        buf.writeBoolean(this.accepted);*/ // 1.17
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            buf.writeInt(this.windowId);
        } else {
            buf.writeByte(this.windowId);
            buf.writeShort(this.actionNumber);
            buf.writeByte(this.accepted ? 1 : 0);
        }
    }

    public int getWindowId()
    {
        return this.windowId;
    }

    public short getActionNumber()
    {
        return this.actionNumber;
    }

    public boolean wasAccepted()
    {
        return this.accepted;
    }
}
