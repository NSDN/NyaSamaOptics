package club.nsdn.nyasamaoptics.tileblock.screen;

import club.nsdn.nyasamaoptics.block.BlockLoader;
import club.nsdn.nyasamaoptics.creativetab.CreativeTabLoader;
import club.nsdn.nyasamaoptics.tileblock.TileBlock;
import club.nsdn.nyasamaoptics.util.StationLampCore;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

/**
 * Created by drzzm on 2019.1.21.
 */
public class StationLamp extends TileBlock {

    public static final int ALIGN_CENTER = 0, ALIGN_LEFT = 1, ALIGN_RIGHT = 2;

    public static class TileLamp extends TileEntityReceiver {

        public String content;
        public int color;
        public int back;
        public double scale;
        public String logo;

        public boolean isEnabled;
        public boolean prevIsEnabled;

        public TileLamp() {
            super();
            content = "DUMMY";
            color = 0xEE1111;
            back = 0x000000;
            scale = 1.0;
            logo = "null";
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
            tagCompound.setInteger("back", back);
            tagCompound.setDouble("scale", scale);
            tagCompound.setString("logo", logo);
            tagCompound.setBoolean("isEnabled", isEnabled);
            return super.toNBT(tagCompound);
        }

        @Override
        public void fromNBT(NBTTagCompound tagCompound) {
            super.fromNBT(tagCompound);
            content = tagCompound.getString("content");
            color = tagCompound.getInteger("color");
            back = tagCompound.getInteger("back");
            scale = tagCompound.getDouble("scale");
            logo = tagCompound.getString("logo");
            isEnabled = tagCompound.getBoolean("isEnabled");
        }

        public static void updateThis(TileLamp tileLamp) {
            tileLamp.updateTileEntity(tileLamp);
        }

    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileLamp();
    }

    public StationLamp() {
        super(Material.glass, "StationLamp");
        setIconLocation("station_lamp");
        setLightLevel(0);
        setCreativeTab(CreativeTabLoader.tabNyaSamaOptics);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
        int dir = MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, dir + 1, 2);
    }

    @Override
    protected void setBoundsByMeta(int meta) {
        float x = 1.0F, y = 1.375F, z = 1.0F;
        setBoundsByXYZ(meta, 0.5F - x / 2, 0.0F, 0.5F - z / 2, 0.5F + x / 2, y, 0.5F + z / 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        if (world.getTileEntity(x, y, z) instanceof TileLamp) {
            TileLamp plate = (TileLamp) world.getTileEntity(x, y, z);
            return plate.back;
        }
        return 16777215;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return side == ForgeDirection.DOWN;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.getTileEntity(x, y, z) == null) return false;
        if (world.getTileEntity(x, y, z) instanceof TileLamp) {
            TileLamp text = (TileLamp) world.getTileEntity(x, y, z);
            if (!world.isRemote) {
                ItemStack stack = player.getCurrentEquippedItem();
                if (stack != null) {

                    NBTTagList list = Util.getTagListFromNGT(stack);
                    if (list == null) return false;
                    String[][] code = NSASM.getCode(list);
                    new StationLampCore(code) {
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
                        public TileLamp getTile() {
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

    public void updateSignal(World world, int x , int y, int z) {
        if (world.getTileEntity(x, y, z) == null) return;
        if (world.getTileEntity(x, y, z) instanceof TileLamp) {
            TileLamp lamp = (TileLamp) world.getTileEntity(x, y, z);

            if (lamp.getSender() != null) {
                lamp.isEnabled = lamp.senderIsPowered();
            } else {
                lamp.isEnabled = true;
            }

            BlockLoader.light.lightCtl(world, x, y, z, lamp.isEnabled);

            if (lamp.isEnabled != lamp.prevIsEnabled) {
                lamp.prevIsEnabled = lamp.isEnabled;
                world.markBlockForUpdate(x, y, z);
            }

            world.scheduleBlockUpdate(x, y, z, this, 1);
        }
    }

}
