package club.nsdn.nyasamaoptics.tileblock.deco;

import club.nsdn.nyasamaoptics.NyaSamaOptics;
import club.nsdn.nyasamaoptics.api.ILightSource;
import club.nsdn.nyasamaoptics.block.BlockLoader;
import club.nsdn.nyasamaoptics.creativetab.CreativeTabLoader;
import club.nsdn.nyasamaoptics.util.DecoTextCore;
import club.nsdn.nyasamaoptics.util.font.FontLoader;
import club.nsdn.nyasamatelecom.api.device.DeviceBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2020.1.3.
 */
public class StationBoard extends DeviceBase implements ILightSource {

    public static class TileEntityStationBoard extends TileEntityDecoText {

        public String label1 = "1";
        public String label2 = "2";
        public int color1 = 0x323232;
        public int color2 = 0x323232;
        public double scale1 = 1;
        public double scale2 = 1;
        public double offset1 = 0;
        public double offset2 = 0;
        public boolean state1 = false;
        public boolean state2 = false;

        public TileEntityStationBoard() {
            super();
            align = FontLoader.ALIGN_CENTER;
            setInfo(4, 1, 1, 0.25);

            content = "\u5e7b";
            color = 0x161616;
            scale = 0.3;
            font = FontLoader.FONT_LONG;

            subContent = "genso";
            subColor = 0x323232;
            subScale = 1.0;
            subOffsetZ = -0.3;

            optionalBack = 0xFAFAFA;
        }

        @Override
        public void fromNBT(NBTTagCompound tagCompound) {
            super.fromNBT(tagCompound);

            label1 = tagCompound.getString("label1");
            label2 = tagCompound.getString("label2");
            color1 = tagCompound.getInteger("color1");
            color2 = tagCompound.getInteger("color2");
            scale1 = tagCompound.getDouble("scale1");
            scale2 = tagCompound.getDouble("scale2");
            offset1 = tagCompound.getDouble("offset1");
            offset2 = tagCompound.getDouble("offset2");
            state1 = tagCompound.getBoolean("state1");
            state2 = tagCompound.getBoolean("state2");
        }

        @Override
        public NBTTagCompound toNBT(NBTTagCompound tagCompound) {
            tagCompound.setString("label1", label1);
            tagCompound.setString("label2", label2);
            tagCompound.setInteger("color1", color1);
            tagCompound.setInteger("color2", color2);
            tagCompound.setDouble("scale1", scale1);
            tagCompound.setDouble("scale2", scale2);
            tagCompound.setDouble("offset1", offset1);
            tagCompound.setDouble("offset2", offset2);
            tagCompound.setBoolean("state1", state1);
            tagCompound.setBoolean("state2", state2);

            return super.toNBT(tagCompound);
        }

        @Override
        public double getMaxRenderDistanceSquared() {
            return 4096.0;
        }

        @Nonnull
        @Override
        public AxisAlignedBB getRenderBoundingBox() {
            int r = 5;
            return Block.FULL_BLOCK_AABB.expand(r,r,r).expand(-r, -r, -r);
        }

        @Override
        public void updateSignal(World world, BlockPos pos) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity == null) return;
            if (tileEntity instanceof TileEntityStationBoard) {
                TileEntityStationBoard board = (TileEntityStationBoard) tileEntity;

                if (board.getSender() != null) {
                    board.isEnabled = board.senderIsPowered();
                } else {
                    board.isEnabled = true;
                }

                BlockLoader.light.lightCtl(world, pos, board.isEnabled);

                if (board.isEnabled != board.prevIsEnabled) {
                    board.prevIsEnabled = board.isEnabled;
                    board.refresh();
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
        return new TileEntityStationBoard();
    }

    public StationBoard() {
        super(Material.GLASS, "StationBoard");
        setRegistryName(NyaSamaOptics.MODID, "station_board");
        setLightLevel(0);
        setCreativeTab(CreativeTabLoader.tabNyaSamaOptics);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
        int val = MathHelper.floor((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        setDeviceMeta(world, pos, val);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)  {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityStationBoard) {
            TileEntityStationBoard text = (TileEntityStationBoard) tileEntity;
            if (!world.isRemote) {
                ItemStack stack = player.getHeldItemMainhand();
                if (!stack.isEmpty()) {

                    NBTTagList list = Util.getTagListFromNGT(stack);
                    if (list == null) return false;
                    String[][] code = NSASM.getCode(list);
                    new DecoTextCore(code) {
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
                        public TileEntityStationBoard getTile() {
                            return text;
                        }

                        @Override
                        public void loadFunc(LinkedHashMap<String, Operator> funcList) {
                            super.loadFunc(funcList);
                            funcList.put("label", ((dst, src) -> {
                                if (src == null) return Result.ERR;
                                if (dst == null) return Result.ERR;
                                if (src.type != RegType.STR) return Result.ERR;
                                if (dst.type != RegType.INT) return Result.ERR;

                                int index = (int) dst.data;
                                index = index < 0 ? 0 : (index > 1 ? 1 : index);
                                if (index == 0)
                                    getTile().label1 = (String) src.data;
                                else
                                    getTile().label2 = (String) src.data;

                                getTile().refresh();

                                return Result.OK;
                            }));
                            funcList.put("color", ((dst, src) -> {
                                if (src == null) return Result.ERR;
                                if (dst == null) return Result.ERR;
                                if (src.type != RegType.INT) return Result.ERR;
                                if (dst.type != RegType.INT) return Result.ERR;

                                int index = (int) dst.data;
                                index = index < 0 ? 0 : (index > 1 ? 1 : index);
                                if (index == 0)
                                    getTile().color1 = (int) src.data;
                                else
                                    getTile().color2 = (int) src.data;

                                getTile().refresh();

                                return Result.OK;
                            }));
                            funcList.put("scale", ((dst, src) -> {
                                if (src == null) return Result.ERR;
                                if (dst == null) return Result.ERR;
                                if (src.type != RegType.FLOAT && src.type != RegType.INT) return Result.ERR;
                                if (dst.type != RegType.INT) return Result.ERR;

                                int index = (int) dst.data;
                                index = index < 0 ? 0 : (index > 1 ? 1 : index);
                                if (index == 0)
                                    getTile().scale1 = src.type == RegType.FLOAT ? (float) src.data : (int) src.data;
                                else
                                    getTile().scale2 = src.type == RegType.FLOAT ? (float) src.data : (int) src.data;

                                getTile().refresh();

                                return Result.OK;
                            }));
                            funcList.put("shift", ((dst, src) -> {
                                if (src == null) return Result.ERR;
                                if (dst == null) return Result.ERR;
                                if (src.type != RegType.FLOAT && src.type != RegType.INT) return Result.ERR;
                                if (dst.type != RegType.INT) return Result.ERR;

                                int index = (int) dst.data;
                                index = index < 0 ? 0 : (index > 1 ? 1 : index);
                                if (index == 0)
                                    getTile().offset1 = src.type == RegType.FLOAT ? (float) src.data : (int) src.data;
                                else
                                    getTile().offset2 = src.type == RegType.FLOAT ? (float) src.data : (int) src.data;

                                getTile().refresh();

                                return Result.OK;
                            }));
                            funcList.put("state", ((dst, src) -> {
                                if (src == null) return Result.ERR;
                                if (dst == null) return Result.ERR;
                                if (src.type != RegType.INT) return Result.ERR;
                                if (dst.type != RegType.INT) return Result.ERR;

                                int index = (int) dst.data;
                                index = index < 0 ? 0 : (index > 1 ? 1 : index);
                                if (index == 0)
                                    getTile().state1 = (((int) src.data) != 0);
                                else
                                    getTile().state2 = (((int) src.data) != 0);

                                getTile().refresh();

                                return Result.OK;
                            }));
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
