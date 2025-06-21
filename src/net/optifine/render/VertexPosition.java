package net.optifine.render;

import net.optifine.shaders.Shaders;

public class VertexPosition
{
    private int frameId;
    private float posX;
    private float posY;
    private float posZ;
    private float DDNVLCX;
    private float DDNVLCY;
    private float DDNVLCZ;
    private boolean DDNVLCValid;

    public void setPosition(int frameId, float x, float y, float z)
    {
        if (!Shaders.isShadowPass)
        {
            if (frameId != this.frameId)
            {
                if (this.frameId != 0)
                {
                    this.DDNVLCX = x - this.posX;
                    this.DDNVLCY = y - this.posY;
                    this.DDNVLCZ = z - this.posZ;
                    this.DDNVLCValid = frameId - this.frameId <= 3 && !Shaders.pointOfViewChanged;
                }

                this.frameId = frameId;
                this.posX = x;
                this.posY = y;
                this.posZ = z;
            }
        }
    }

    public boolean isDDNVLCValid()
    {
        return this.DDNVLCValid;
    }

    public float getDDNVLCX()
    {
        return this.DDNVLCX;
    }

    public float getDDNVLCY()
    {
        return this.DDNVLCY;
    }

    public float getDDNVLCZ()
    {
        return this.DDNVLCZ;
    }

    public int getFrameId()
    {
        return this.frameId;
    }
}
