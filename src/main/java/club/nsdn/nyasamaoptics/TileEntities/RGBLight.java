package club.nsdn.nyasamaoptics.TileEntities;

import club.nsdn.nyasamaoptics.Util.NSASM;
import club.nsdn.nyasamaoptics.Util.RGBLightCore;
import club.nsdn.nyasamaoptics.Util.Util;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.WavefrontObject;

import java.util.LinkedHashMap;

/**
 * Created by drzzm on 2017.10.3.
 */
public class RGBLight extends TileEntityBase {

    public static class TileLight extends TileEntity {

        public int color;

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
            return super.toNBT(tagCompound);
        }

        @Override
        public void fromNBT(NBTTagCompound tagCompound) {
            super.fromNBT(tagCompound);
            color = tagCompound.getInteger("color");
        }

    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileLight();
    }

    protected float x, y, z;
    public String resource;
    @SideOnly(Side.CLIENT)
    public WavefrontObject modelShell;
    @SideOnly(Side.CLIENT)
    public WavefrontObject modelLight;

    public RGBLight(String name, String resource, float x, float y, float z) {
        super(Material.glass, name);
        setIconLocation(resource);
        setLightLevel(1);
        this.x = x; this.y = y; this.z = z;
        this.resource = resource;
    }

    @Override
    protected void setBoundsByMeta(int meta) {
        setBoundsByXYZ(meta, 0.5F - x / 2, 0.0F, 0.5F - z / 2, 0.5F + x / 2, y, 0.5F + z / 2);
    }

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

}
