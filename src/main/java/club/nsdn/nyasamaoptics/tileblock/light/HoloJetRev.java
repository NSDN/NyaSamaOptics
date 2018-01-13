package club.nsdn.nyasamaoptics.tileblock.light;

import club.nsdn.nyasamaoptics.block.BlockLoader;
import club.nsdn.nyasamaoptics.creativetab.CreativeTabLoader;
import club.nsdn.nyasamaoptics.util.font.FontLoader;
import club.nsdn.nyasamaoptics.util.font.TextModel;
import club.nsdn.nyasamaoptics.util.HoloJetRevCore;
import club.nsdn.nyasamaoptics.tileblock.TileBlock;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Random;

/**
 * Created by drzzm on 2017.11.3.
 */
public class HoloJetRev extends TileBlock {

    public static class TileText extends TileEntityReceiver {
        @SideOnly(Side.CLIENT)
        public TextModel model;

        public String content;
        public int color;
        public int thick;
        public double scale;
        public int align;
        public int font;

        public boolean isEnabled;
        public boolean prevIsEnabled;

        public int hash = -1;

        public TileText() {
            super();
            content = "O";
            color = 0xFFFFFF;
            thick = 4;
            scale = 1.0;
            align = FontLoader.ALIGN_CENTER;
            font = FontLoader.FONT_SONG;
        }

        @SideOnly(Side.CLIENT)
        public void createModel() {
            HashSet<Object> hashSet = new HashSet<Object>();
            hashSet.add(content);
            hashSet.add(String.valueOf(color));
            hashSet.add(String.valueOf(thick));
            hashSet.add(String.valueOf(scale));
            hashSet.add(String.valueOf(align));
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
            tagCompound.setBoolean("isEnabled", isEnabled);
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
            isEnabled = tagCompound.getBoolean("isEnabled");
        }

        public static void updateThis(TileText tileText) {
            tileText.updateTileEntity(tileText);
        }

    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileText();
    }

    public HoloJetRev() {
        super(Material.glass, "HoloJetRev");
        setIconLocation("holo_jet_rev");
        setLightLevel(0);
        setCreativeTab(CreativeTabLoader.tabNyaSamaOptics);
    }

    @Override
    protected void setBoundsByMeta(int meta) {
        float x = 1.0F, y = 0.25F, z = 0.5F;
        setBoundsByXYZ(meta, 0.5F - x / 2, 0.0F, 0.5F - z / 2, 0.5F + x / 2, y, 0.5F + z / 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        if (world.getTileEntity(x, y, z) instanceof TileText) {
            TileText text = (TileText) world.getTileEntity(x, y, z);
            return text.color;
        }
        return 16777215;
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
                    new HoloJetRevCore(code) {
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

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        world.scheduleBlockUpdate(x, y, z, this, 1);
    }

    @Override
    public void onBlockPreDestroy(World world, int x, int y, int z, int meta) {
        super.onBlockPreDestroy(world, x, y, z, meta);
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity != null) {
            if (tileEntity instanceof TileEntityReceiver) {
                ((TileEntityReceiver) tileEntity).onDestroy();
            }
        }
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
        return false;
    }

    @Override
    public int tickRate(World world) {
        return 10;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (!world.isRemote) {
            updateSignal(world, x, y, z);
        }
    }

    public void b2b(World world, int x, int y, int z, Block dst, Block src) {
        if (src == Blocks.air) {
            if (world.getBlock(x, y, z).isAir(world, x, y, z)) {
                world.setBlock(x, y, z, dst);
            }
        } else {
            if (world.getBlock(x, y, z) == src) {
                world.setBlock(x, y, z, dst);
            }
        }
    }

    public void updateSignal(World world, int x , int y, int z) {
        if (world.getTileEntity(x, y, z) == null) return;
        if (world.getTileEntity(x, y, z) instanceof TileText) {
            TileText light = (TileText) world.getTileEntity(x, y, z);

            if (light.getSender() != null) {
                light.isEnabled = light.senderIsPowered();
            } else {
                light.isEnabled = true;
            }

            if (light.isEnabled) {
                b2b(world, x + 1, y, z, BlockLoader.light, Blocks.air);
                b2b(world, x - 1, y, z, BlockLoader.light, Blocks.air);
                b2b(world, x, y + 1, z, BlockLoader.light, Blocks.air);
                b2b(world, x, y - 1, z, BlockLoader.light, Blocks.air);
                b2b(world, x, y, z + 1, BlockLoader.light, Blocks.air);
                b2b(world, x, y, z - 1, BlockLoader.light, Blocks.air);
            } else {
                b2b(world, x + 1, y, z, Blocks.air, BlockLoader.light);
                b2b(world, x - 1, y, z, Blocks.air, BlockLoader.light);
                b2b(world, x, y + 1, z, Blocks.air, BlockLoader.light);
                b2b(world, x, y - 1, z, Blocks.air, BlockLoader.light);
                b2b(world, x, y, z + 1, Blocks.air, BlockLoader.light);
                b2b(world, x, y, z - 1, Blocks.air, BlockLoader.light);
            }

            if (light.isEnabled != light.prevIsEnabled) {
                light.prevIsEnabled = light.isEnabled;
                world.markBlockForUpdate(x, y, z);
            }

            world.scheduleBlockUpdate(x, y, z, this, 1);
        }
    }

}
