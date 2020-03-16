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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by drzzm32 on 2019.12.24.
 */
public class AdBoard extends DeviceBase implements ILightSource {

    public static class TileEntityAdBoard extends TileEntityDecoText {

        public TileEntityAdBoard() {
            super();
            align = FontLoader.ALIGN_CENTER;
            setInfo(12, 1, 0.125, 1);

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
        public double getMaxRenderDistanceSquared() {
            return 1024.0;
        }

        @Nonnull
        @Override
        public AxisAlignedBB getRenderBoundingBox() {
            int r = 3;
            return Block.FULL_BLOCK_AABB.expand(r,r,r).expand(-r, -r, -r);
        }

        @Override
        public void updateSignal(World world, BlockPos pos) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity == null) return;
            if (tileEntity instanceof TileEntityAdBoard) {
                TileEntityAdBoard board = (TileEntityAdBoard) tileEntity;

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
        return new TileEntityAdBoard();
    }

    public AdBoard() {
        super(Material.GLASS, "AdBoard");
        setRegistryName(NyaSamaOptics.MODID, "ad_board");
        setLightLevel(0);
        setCreativeTab(CreativeTabLoader.tabNyaSamaOptics);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)  {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityAdBoard) {
            TileEntityAdBoard text = (TileEntityAdBoard) tileEntity;
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
                        public TileEntityAdBoard getTile() {
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
