package club.nsdn.nyasamaoptics.TileEntities;

import club.nsdn.nyasamaoptics.CreativeTab.CreativeTabLoader;
import club.nsdn.nyasamaoptics.Util.Font.FontLoader;
import club.nsdn.nyasamaoptics.Util.Font.TextModel;
import club.nsdn.nyasamaoptics.Util.NSASM;
import club.nsdn.nyasamaoptics.Util.PillarHeadCore;
import club.nsdn.nyasamaoptics.Util.Util;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.HashSet;

/**
 * Created by drzzm on 2017.11.2.
 */
public class PillarHead extends TileEntityBase {

    public static class TileText extends TileEntity {
        @SideOnly(Side.CLIENT)
        public TextModel model;

        public String content;
        public int color;
        public int thick;
        public double scale;
        public int align;
        public int font;

        public int hash = -1;

        public TileText() {
            super();
            content = "O";
            color = 0xFFFFFF;
            thick = 4;
            scale = 1.0;
            align = FontLoader.ALIGN_DOWN;
            font = FontLoader.FONT_SONG;
        }

        @SideOnly(Side.CLIENT)
        public void createModel() {
            HashSet<Object> hashSet = new HashSet<Object>();
            hashSet.add(content);
            hashSet.add(String.valueOf(color));
            hashSet.add(String.valueOf(thick));
            hashSet.add(String.valueOf(scale));
            hashSet.add(String.valueOf(font));

            if (hash != hashSet.hashCode()) {
                model = FontLoader.getModel(font, align, content, color, thick);
                hash = hashSet.hashCode();
            }
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
            tagCompound.setInteger("thick", thick);
            tagCompound.setDouble("scale", scale);
            tagCompound.setInteger("align", align);
            tagCompound.setInteger("font", font);
            return super.toNBT(tagCompound);
        }

        @Override
        public void fromNBT(NBTTagCompound tagCompound) {
            super.fromNBT(tagCompound);
            content = tagCompound.getString("content");
            color = tagCompound.getInteger("color");
            thick = tagCompound.getInteger("thick");
            scale = tagCompound.getDouble("scale");
            align = tagCompound.getInteger("align");
            font = tagCompound.getInteger("font");
        }

        public static void updateThis(TileText tileText) {
            updateTileEntity(tileText);
        }

    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileText();
    }

    public PillarHead() {
        super(Material.glass, "PillarHead");
        setIconLocation("pillar_head");
        setLightLevel(1);
        setCreativeTab(CreativeTabLoader.tabNyaSamaOptics);
    }

    @Override
    protected void setBoundsByMeta(int meta) {
        float x1 = 0.0F, y1 = 0.0F, z1 = 0.0F, x2 = 1.0F, y2 = 1.0F, z2 = 1.0F;
        switch (meta & 3) {
            case 2:
                setBlockBounds(x1, y1, z1, x2, y2, z2);
                break;
            case 3:
                setBlockBounds(1.0F - z2, y1, x1, 1.0F - z1, y2, x2);
                break;
            case 0:
                setBlockBounds(1.0F - x2, y1, 1.0F - z2, 1.0F - x1, y2, 1.0F - z1);
                break;
            case 1:
                setBlockBounds(z1, y1, 1.0F - x2, z2, y2, 1.0F - x1);
                break;
        }
    }

    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        if (world.getTileEntity(x, y, z) instanceof TileText) {
            TileText text = (TileText) world.getTileEntity(x, y, z);
            return text.color;
        }
        return 16777215;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
        int meta = MathHelper.floor_double((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.getTileEntity(x, y, z) == null) return false;
        if (world.getTileEntity(x, y, z) instanceof TileText) {
            TileText text = (TileText) world.getTileEntity(x, y, z);
            if (!world.isRemote) {
                ItemStack stack = player.getCurrentEquippedItem();
                if (stack != null) {

                    NBTTagList list = Util.getTagListFromNGT(stack);
                    if (list == null) return false;
                    String[][] code = NSASM.getCode(list);
                    new PillarHeadCore(code) {
                        @Override
                        public World getWorld() {
                            return world;
                        }

                        @Override
                        public double getX() {
                            return x;
                        }

                        @Override
                        public double getY() {
                            return y;
                        }

                        @Override
                        public double getZ() {
                            return z;
                        }

                        @Override
                        public EntityPlayer getPlayer() {
                            return player;
                        }

                        @Override
                        public TileText getTile() {
                            return text;
                        }
                    }.run();

                }
            }
            return true;
        }

        return false;
    }

}
