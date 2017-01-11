package club.nsdn.nyasamaoptics.Items;

import club.nsdn.nyasamaoptics.NyaSamaOptics;
import club.nsdn.nyasamaoptics.Util.Font.FontLoader;
import club.nsdn.nyasamaoptics.TileEntities.HoloJet;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Created by drzzm32 on 2016.6.11.
 */
public class ItemHoloJet extends ItemToolBase {

    public Block theBlock;
    public int theFont;

    public ItemHoloJet(String name, Block block, int font) {
        super(ToolMaterial.IRON);
        theBlock = block;
        theFont = font;
        setUnlocalizedName(name);
        setTexName("holo_jet_conv");
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        player.setItemInUse(itemStack, this.getMaxItemUseDuration(itemStack));
        return itemStack;
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        Block block = world.getBlock(x, y, z);

        if (block == null)
            return false;

        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity == null)
            return false;
        if (tileEntity instanceof TileEntitySign) {
            HoloJet.TileText tileText = new HoloJet.TileText();
            tileText.content = ((TileEntitySign) tileEntity).signText[0].isEmpty() ? "DUMMY" : ((TileEntitySign) tileEntity).signText[0];

            try {
                tileText.color = Integer.parseInt(((TileEntitySign) tileEntity).signText[1].replace(" ", "").split(",")[0], 16);
            } catch (Exception e) {
                NyaSamaOptics.log.error(e.getMessage());
                tileText.color = 0xFFFFFF;
            }
            try {
                tileText.thick = Integer.parseInt(((TileEntitySign) tileEntity).signText[1].replace(" ", "").split(",")[1], 10);
            } catch (Exception e) {
                NyaSamaOptics.log.error(e.getMessage());
                tileText.thick = 4;
            }
            try {
                tileText.scaleX = Double.parseDouble(((TileEntitySign) tileEntity).signText[2].replace(" ", "").split(",")[0]);
            } catch (Exception e) {
                NyaSamaOptics.log.error(e.getMessage());
                tileText.scaleX = 1.0;
            }
            try {
                tileText.scaleY = Double.parseDouble(((TileEntitySign) tileEntity).signText[2].replace(" ", "").split(",")[1]);
            } catch (Exception e) {
                NyaSamaOptics.log.error(e.getMessage());
                tileText.scaleY = 1.0;
            }
            try {
                tileText.scaleZ = Double.parseDouble(((TileEntitySign) tileEntity).signText[2].replace(" ", "").split(",")[2]);
            } catch (Exception e) {
                NyaSamaOptics.log.error(e.getMessage());
                tileText.scaleZ = 1.0;
            }

            if (((TileEntitySign) tileEntity).signText[3].contains("Left")) {
                tileText.align = FontLoader.ALIGN_LEFT;
            } else if (((TileEntitySign) tileEntity).signText[3].contains("Right")) {
                tileText.align = FontLoader.ALIGN_RIGHT;
            } else if (((TileEntitySign) tileEntity).signText[3].contains("Center")) {
                tileText.align = FontLoader.ALIGN_CENTER;
            } else {
                tileText.align = FontLoader.ALIGN_CENTER;
            }

            int l = MathHelper.floor_double((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            if (l == 0) {
                world.setBlock(x, y, z, theBlock, 1, 2);
            }

            if (l == 1) {
                world.setBlock(x, y, z, theBlock, 2, 2);
            }

            if (l == 2) {
                world.setBlock(x, y, z, theBlock, 3, 2);
            }

            if (l == 3) {
                world.setBlock(x, y, z, theBlock, 4, 2);
            }

            world.markBlockForUpdate(x, y, z);

            tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity == null)
                return false;
            if (tileEntity instanceof HoloJet.TileText) {
                ((HoloJet.TileText) tileEntity).content = tileText.content;
                ((HoloJet.TileText) tileEntity).color = tileText.color;
                ((HoloJet.TileText) tileEntity).thick = (int)((double)tileText.thick * tileText.scaleZ);
                ((HoloJet.TileText) tileEntity).scaleX = tileText.scaleX;
                ((HoloJet.TileText) tileEntity).scaleY = tileText.scaleY;
                ((HoloJet.TileText) tileEntity).scaleZ = 1.0;
                ((HoloJet.TileText) tileEntity).align = tileText.align;

                ((HoloJet.TileText) tileEntity).font = theFont;

                return !world.isRemote;
            }
        }

        return false;
    }
}
