package club.nsdn.nyasamaoptics.tileblock.screen;

import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

/**
 * Created by drzzm on 2017.3.23.
 */
public class TilePlatformPlate extends TileEntityReceiver {

    public static final int ALIGN_CENTER = 0, ALIGN_LEFT = 1, ALIGN_RIGHT = 2;

    public String content;
    public int color;
    public double scale;
    public int align;

    public boolean isEnabled;
    public boolean prevIsEnabled;

    public TilePlatformPlate() {
        super();
        content = "DUMMY";
        color = 0x323232;
        scale = 1.0;
        align = ALIGN_CENTER;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public NBTTagCompound toNBT(NBTTagCompound tagCompound) {
        tagCompound.setString("content", content);
        tagCompound.setInteger("color", color);
        tagCompound.setDouble("scale", scale);
        tagCompound.setInteger("align", align);
        tagCompound.setBoolean("isEnabled", isEnabled);
        return super.toNBT(tagCompound);
    }

    @Override
    public void fromNBT(NBTTagCompound tagCompound) {
        super.fromNBT(tagCompound);
        content = tagCompound.getString("content");
        color = tagCompound.getInteger("color");
        scale = tagCompound.getDouble("scale");
        align = tagCompound.getInteger("align");
        isEnabled = tagCompound.getBoolean("isEnabled");
    }

    public static void updateThis(TilePlatformPlate tilePlate) {
        tilePlate.updateTileEntity(tilePlate);
    }

}
