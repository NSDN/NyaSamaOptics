package club.nsdn.nyasamaoptics.Items;

import club.nsdn.nyasamaoptics.Blocks.BlockLoader;
import club.nsdn.nyasamaoptics.Util.Font.FontLoader;
import club.nsdn.nyasamaoptics.Util.Font.HoloJet;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Created by drzzm32 on 2016.6.7.
 */
public class ItemHoloJetDaBiaoSong extends ItemToolBase {

    public ItemHoloJetDaBiaoSong() {
        super(ToolMaterial.IRON);
        setUnlocalizedName("ItemHoloJetDaBiaoSong");
        setTexName("holo_jet");
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
            tileText.content = ((TileEntitySign) tileEntity).signText[0];
            tileText.color = Integer.parseInt(((TileEntitySign) tileEntity).signText[1].split(", ")[0], 16);
            tileText.thick = Integer.parseInt(((TileEntitySign) tileEntity).signText[1].split(", ")[1], 10);
            tileText.scaleX = Double.parseDouble(((TileEntitySign) tileEntity).signText[2].split(", ")[0]);
            tileText.scaleY = Double.parseDouble(((TileEntitySign) tileEntity).signText[2].split(", ")[1]);
            tileText.scaleZ = Double.parseDouble(((TileEntitySign) tileEntity).signText[2].split(", ")[2]);
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
                world.setBlock(x, y, z, BlockLoader.blockHoloJetDaBiaoSong, 1, 2);
            }

            if (l == 1) {
                world.setBlock(x, y, z, BlockLoader.blockHoloJetDaBiaoSong, 2, 2);
            }

            if (l == 2) {
                world.setBlock(x, y, z, BlockLoader.blockHoloJetDaBiaoSong, 3, 2);
            }

            if (l == 3) {
                world.setBlock(x, y, z, BlockLoader.blockHoloJetDaBiaoSong, 4, 2);
            }

            world.markBlockForUpdate(x, y, z);

            tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity == null)
                return false;
            if (tileEntity instanceof HoloJet.TileText) {
                ((HoloJet.TileText) tileEntity).content = tileText.content;
                ((HoloJet.TileText) tileEntity).color = tileText.color;
                ((HoloJet.TileText) tileEntity).thick = tileText.thick;
                ((HoloJet.TileText) tileEntity).scaleX = tileText.scaleX;
                ((HoloJet.TileText) tileEntity).scaleY = tileText.scaleY;
                ((HoloJet.TileText) tileEntity).scaleZ = tileText.scaleZ;
                ((HoloJet.TileText) tileEntity).align = tileText.align;

                ((HoloJet.TileText) tileEntity).font = FontLoader.FONT_DABIAOSONG;

                return !world.isRemote;
            }
        }

        return false;
    }
}
