package club.nsdn.nyasamaoptics.tileblock.light;

import club.nsdn.nyasamaoptics.block.BlockLoader;
import club.nsdn.nyasamaoptics.util.RGBLightCore;
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
import net.minecraftforge.client.model.obj.WavefrontObject;

import java.util.Random;

/**
 * Created by drzzm on 2017.10.3.
 */
public class RGBLight extends TileBlock {

    public static class TileLight extends TileEntityReceiver {

        public int color;
        public boolean isEnabled;
        public boolean prevIsEnabled;

        public TileLight() {
            super();
            color = 0xFFFFFF;
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
            tagCompound.setInteger("color", color);
            tagCompound.setBoolean("isEnabled", isEnabled);
            return super.toNBT(tagCompound);
        }

        @Override
        public void fromNBT(NBTTagCompound tagCompound) {
            super.fromNBT(tagCompound);
            color = tagCompound.getInteger("color");
            isEnabled = tagCompound.getBoolean("isEnabled");
        }

    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileLight();
    }

    public float x, y, z;
    public String resource;
    @SideOnly(Side.CLIENT)
    public WavefrontObject modelShell;
    @SideOnly(Side.CLIENT)
    public WavefrontObject modelLight;

    public RGBLight(String name, String resource, float x, float y, float z) {
        super(Material.glass, name);
        setIconLocation(resource);
        setLightLevel(0);
        this.x = x; this.y = y; this.z = z;
        this.resource = resource;
    }

    @Override
    protected void setBoundsByMeta(int meta) {
        setBoundsByXYZ(meta, 0.5F - x / 2, 0.0F, 0.5F - z / 2, 0.5F + x / 2, y, 0.5F + z / 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        if (world.getTileEntity(x, y, z) instanceof TileLight) {
            TileLight light = (TileLight) world.getTileEntity(x, y, z);
            return light.color;
        }
        return 16777215;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.getTileEntity(x, y, z) == null) return false;
        if (world.getTileEntity(x, y, z) instanceof TileLight) {
            TileLight light = (TileLight) world.getTileEntity(x, y, z);
            if (!world.isRemote) {
                ItemStack stack = player.getCurrentEquippedItem();
                if (stack != null) {

                    NBTTagList list = Util.getTagListFromNGT(stack);
                    if (list == null) return false;
                    String[][] code = NSASM.getCode(list);
                    new RGBLightCore(code) {
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
                        public TileLight getLight() {
                            return light;
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
        if (world.getTileEntity(x, y, z) instanceof TileLight) {
            TileLight light = (TileLight) world.getTileEntity(x, y, z);

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
