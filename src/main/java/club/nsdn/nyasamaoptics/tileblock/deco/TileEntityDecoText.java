package club.nsdn.nyasamaoptics.tileblock.deco;

import club.nsdn.nyasamaoptics.tileblock.holo.TileEntityHoloText;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by drzzm32 on 2019.12.21.
 */
public class TileEntityDecoText extends TileEntityHoloText {

    public static final int SUB_ALIGN_CENTER = 0, SUB_ALIGN_LEFT = 1, SUB_ALIGN_RIGHT = 2;
    
    public String subContent;
    public int subColor;
    public double subScale;
    public int subAlign;
    
    public double offsetX, offsetY, offsetZ;
    public double subOffsetX, subOffsetY, subOffsetZ;
    
    public int optionalBack;

    public TileEntityDecoText() {
        super();

        thick = 1;

        subContent = " ";
        subColor = 0xFFFFFF;
        subScale = 1.0;
        subAlign = SUB_ALIGN_CENTER;

        offsetX = offsetY = offsetZ = 0;
        subOffsetX = subOffsetY = subOffsetZ = 0;
        
        optionalBack = 0x000000;
    }

    @Override
    public NBTTagCompound toNBT(NBTTagCompound tagCompound) {
        tagCompound.setString("subContent", subContent);
        tagCompound.setInteger("subColor", subColor);
        tagCompound.setDouble("subScale", subScale);
        tagCompound.setInteger("subAlign", subAlign);

        tagCompound.setDouble("offsetX", offsetX);
        tagCompound.setDouble("offsetY", offsetY);
        tagCompound.setDouble("offsetZ", offsetZ);
        tagCompound.setDouble("subOffsetX", subOffsetX);
        tagCompound.setDouble("subOffsetY", subOffsetY);
        tagCompound.setDouble("subOffsetZ", subOffsetZ);

        tagCompound.setInteger("optionalBack", optionalBack);
        
        return super.toNBT(tagCompound);
    }

    @Override
    public void fromNBT(NBTTagCompound tagCompound) {
        super.fromNBT(tagCompound);

        subContent = tagCompound.getString("subContent");
        subColor = tagCompound.getInteger("subColor");
        subScale = tagCompound.getDouble("subScale");
        subAlign = tagCompound.getInteger("subAlign");

        offsetX = tagCompound.getDouble("offsetX");
        offsetY = tagCompound.getDouble("offsetY");
        offsetZ = tagCompound.getDouble("offsetZ");
        subOffsetX = tagCompound.getDouble("subOffsetX");
        subOffsetY = tagCompound.getDouble("subOffsetY");
        subOffsetZ = tagCompound.getDouble("subOffsetZ");

        optionalBack = tagCompound.getInteger("optionalBack");
    }

    public static void updateThis(TileEntityDecoText decoText) {
        decoText.refresh();
    }

    @Override
    public void updateSignal(World world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) return;
        if (tileEntity instanceof TileEntityHoloText) {
            TileEntityHoloText holoText = (TileEntityHoloText) tileEntity;

            if (holoText.getSender() != null) {
                holoText.isEnabled = holoText.senderIsPowered();
            } else {
                holoText.isEnabled = true;
            }

            if (holoText.isEnabled != holoText.prevIsEnabled) {
                holoText.prevIsEnabled = holoText.isEnabled;
                holoText.refresh();
            }
        }
    }

}
