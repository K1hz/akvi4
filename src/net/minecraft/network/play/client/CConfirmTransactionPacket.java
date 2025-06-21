package net.minecraft.network.play.client;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import minecraft.system.via.ViaLoadingBase;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;

import java.io.IOException;

public class CConfirmTransactionPacket implements IPacket<IServerPlayNetHandler>
{
    private int windowId;
    private short uid;
    private boolean accepted;

    public CConfirmTransactionPacket()
    {
    }

    public CConfirmTransactionPacket(int windowIdIn, short uidIn, boolean acceptedIn)
    {
        this.windowId = windowIdIn;
        this.uid = uidIn;
        this.accepted = acceptedIn;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(IServerPlayNetHandler handler)
    {
        handler.processConfirmTransaction(this);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException {
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            this.windowId = buf.readInt();
        } else {
            this.windowId = buf.readUnsignedByte();
            this.uid = buf.readShort();
            this.accepted = buf.readBoolean();
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException {
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            buf.writeInt(this.windowId);
        } else {
            buf.writeByte(this.windowId);
            buf.writeShort(this.uid);
            buf.writeByte(this.accepted ? 1 : 0);
        }
    }

    public int getWindowId()
    {
        return this.windowId;
    }

    public short getUid()
    {
        return this.uid;
    }
}
