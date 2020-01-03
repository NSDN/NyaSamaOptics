package club.nsdn.nyasamaoptics.tileblock.screen;

import club.nsdn.nyasamaoptics.NyaSamaOptics;
import club.nsdn.nyasamaoptics.api.ILightSource;
import club.nsdn.nyasamaoptics.block.BlockLoader;
import club.nsdn.nyasamaoptics.creativetab.CreativeTabLoader;
import club.nsdn.nyasamaoptics.util.StationLampCore;
import club.nsdn.nyasamatelecom.api.device.DeviceBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;

/**
 * Created by drzzm32 on 2019.1.30.
 */
public class StationLamp extends DeviceBase implements ILightSource {

    public static final int ALIGN_CENTER = 0, ALIGN_LEFT = 1, ALIGN_RIGHT = 2;

    public static class TileEntityStationLamp extends TileEntityReceiver {

        public static class LampInfo {
            public String content;
            public int color;
            public int back;
            public double scale;

            public static LampInfo get(String content, int color, int back, double scale) {
                LampInfo info = new LampInfo();
                info.content = content;
                info.color = color;
                info.back = back;
                info.scale = scale;
                return info;
            }

            public static LampInfo def() {
                return get("DUMMY", 0xEE1111, 0x000000, 1.0);
            }
        }
        public LinkedList<LampInfo> lampInfo;
        public String logo;

        public boolean isEnabled;
        public boolean prevIsEnabled;

        public TileEntityStationLamp() {
            super();

            lampInfo = new LinkedList<>();
            lampInfo.add(LampInfo.def());
            logo = "null";

            setInfo(4, 1, 1.375, 1);
        }

        @Override
        public boolean shouldRenderInPass(int pass) {
            return true;
        }

        @Override
        @SideOnly(Side.CLIENT)
        @Nonnull
        public AxisAlignedBB getRenderBoundingBox()
        {
            return INFINITE_EXTENT_AABB;
        }

        @Override
        public NBTTagCompound toNBT(NBTTagCompound tagCompound) {
            int i = 0;
            for (LampInfo info : lampInfo) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("content", info.content);
                tag.setInteger("color", info.color);
                tag.setInteger("back", info.back);
                tag.setDouble("scale", info.scale);
                tagCompound.setTag("lampInfo" + i, tag);
                i += 1;
            }
            tagCompound.setString("logo", logo);
            tagCompound.setBoolean("isEnabled", isEnabled);

            return super.toNBT(tagCompound);
        }

        @Override
        public void fromNBT(NBTTagCompound tagCompound) {
            super.fromNBT(tagCompound);

            if (tagCompound.hasKey("lampInfo0")) {
                lampInfo.clear();
                for (int i = 0; tagCompound.hasKey("lampInfo" + i); i++) {
                    NBTTagCompound tag = tagCompound.getCompoundTag("lampInfo" + i);
                    if (!tag.hasKey("content"))
                        continue;
                    String content = tag.getString("content");
                    int color = tag.getInteger("color");
                    int back = tag.getInteger("back");
                    double scale = tag.getDouble("scale");
                    lampInfo.add(LampInfo.get(content, color, back, scale));
                }
            }
            logo = tagCompound.getString("logo");
            isEnabled = tagCompound.getBoolean("isEnabled");
        }

        public static void updateThis(TileEntityStationLamp tileEntityStationLamp) {
            tileEntityStationLamp.refresh();
        }

        @Override
        public void updateSignal(World world, BlockPos pos) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity == null) return;
            if (tileEntity instanceof TileEntityStationLamp) {
                TileEntityStationLamp lamp = (TileEntityStationLamp) tileEntity;

                if (lamp.getSender() != null) {
                    lamp.isEnabled = lamp.senderIsPowered();
                } else {
                    lamp.isEnabled = true;
                }

                BlockLoader.light.lightCtl(world, pos, lamp.isEnabled);

                if (lamp.isEnabled != lamp.prevIsEnabled) {
                    lamp.prevIsEnabled = lamp.isEnabled;
                    lamp.refresh();
                }
            }
        }

    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return createNewTileEntity();
    }

    @Override
    public TileEntity createNewTileEntity() {
        return new TileEntityStationLamp();
    }

    public StationLamp() {
        super(Material.GLASS, "StationLamp");
        setRegistryName(NyaSamaOptics.MODID, "station_lamp");
        setLightLevel(0);
        setCreativeTab(CreativeTabLoader.tabNyaSamaOptics);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
        int val = MathHelper.floor((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        setDeviceMeta(world, pos, val);
    }

    @Override
    public boolean isSideSolid(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing facing) {
        return facing == EnumFacing.DOWN;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)  {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityStationLamp) {
            TileEntityStationLamp text = (TileEntityStationLamp) tileEntity;
            if (!world.isRemote) {
                ItemStack stack = player.getHeldItemMainhand();
                if (!stack.isEmpty()) {

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
                            return pos.getX();
                        }

                        @Override
                        public double getY() {
                            return pos.getY();
                        }

                        @Override
                        public double getZ() {
                            return pos.getZ();
                        }

                        @Override
                        public EntityPlayer getPlayer() {
                            return player;
                        }

                        @Override
                        public TileEntityStationLamp getTile() {
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
    public void breakBlock(World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null) {
            if (tileEntity instanceof TileEntityReceiver) {
                ((TileEntityReceiver) tileEntity).onDestroy();
            }
        }
        super.breakBlock(world, pos, state);
    }

}
