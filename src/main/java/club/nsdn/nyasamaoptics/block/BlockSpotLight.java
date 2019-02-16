package club.nsdn.nyasamaoptics.block;

import club.nsdn.nyasamaoptics.NyaSamaOptics;
import club.nsdn.nyasamaoptics.api.ILightSource;
import club.nsdn.nyasamaoptics.api.LightBeam;
import club.nsdn.nyasamaoptics.creativetab.CreativeTabLoader;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by drzzm32 on 2019.2.16.
 */
public class BlockSpotLight extends Block implements ILightSource {

    public static enum EnumVertical implements IStringSerializable {
        UP("up"),
        MID("mid");

        private final String name;

        EnumVertical(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }
    }

    public static final PropertyEnum<EnumVertical> VERTICAL = PropertyEnum.create("vertical", EnumVertical.class);
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyBool POWERED = PropertyBool.create("powered");

    static LinkedList<AxisAlignedBB> AABBs = new LinkedList<>();
    static AxisAlignedBB getAABB(double x1, double y1, double z1, double x2, double y2, double z2) {
        AxisAlignedBB aabb = new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
        for (AxisAlignedBB i : AABBs) {
            if (i.equals(aabb)) return i;
        }
        AABBs.add(aabb);
        return aabb;
    }

    protected AxisAlignedBB getBoxByXYZ(IBlockState state, double x, double y, double z) {
        double x1 = 0.5 - x / 2, y1 = 0, z1 = 0.5 - z / 2;
        double x2 = 0.5 + x / 2, y2 = y, z2 = 0.5 + z / 2;
        EnumFacing facing = state.getValue(FACING);
        EnumVertical vertical = state.getValue(VERTICAL);

        switch (vertical) {
            case MID:
                switch (facing) {
                    case NORTH:
                        return getAABB(1.0 - x2, z1, 1.0 - y2, 1.0 - x1, z2, 1.0 - y1);
                    case EAST:
                        return getAABB(y1, z1, 1.0 - x2, y2, z2, 1.0 - x1);
                    case SOUTH:
                        return getAABB(x1, z1, y1, x2, z2, y2);
                    case WEST:
                        return getAABB(1.0 - y2, z1, x1, 1.0 - y1, z2, x2);
                }
                break;
            case UP:
                switch (facing) {
                    case NORTH:
                        return getAABB(x1, 1.0 - y2, z1, x2, 1.0 - y1, z2);
                    case EAST:
                        return getAABB(1.0 - z2, 1.0 - y2, x1, 1.0 - z1, 1.0 - y1, x2);
                    case SOUTH:
                        return getAABB(1.0 - x2, 1.0 - y2, 1.0 - z2, 1.0 - x1, 1.0 - y1, 1.0 - z1);
                    case WEST:
                        return getAABB(z1, 1.0 - y2, 1.0 - x2, z2, 1.0 - y1, 1.0 - x1);
                }
        }

        return Block.FULL_BLOCK_AABB;
    }

    public BlockSpotLight() {
        super(Material.GLASS);
        setUnlocalizedName("SpotLight");
        setRegistryName(NyaSamaOptics.MODID, "spot_light");
        setHardness(2.0F);
        setResistance(blockHardness * 5.0F);
        setLightLevel(1);
        setSoundType(SoundType.GLASS);
        setCreativeTab(CreativeTabLoader.tabNyaSamaOptics);

        setDefaultState(blockState.getBaseState()
                        .withProperty(FACING, EnumFacing.NORTH)
                        .withProperty(VERTICAL, EnumVertical.UP)
                        .withProperty(POWERED, true)
        );
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getBoxByXYZ(state, 0.625, 0.5, 0.625);
    }

    @Override
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        EnumVertical vertical = EnumVertical.MID;
        boolean powered = (meta & 0x8) != 0;
        if ((meta & 0x4) != 0) vertical = EnumVertical.UP;
        return getDefaultState()
                .withProperty(FACING, EnumFacing.getHorizontal(meta & 3))
                .withProperty(VERTICAL, vertical)
                .withProperty(POWERED, powered);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumVertical vertical = state.getValue(VERTICAL);
        int v = 0x0;
        if (vertical == EnumVertical.UP) v |= 0x4;
        if (state.getValue(POWERED)) v |= 0x8;
        return state.getValue(FACING).getHorizontalIndex() | v;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, VERTICAL, POWERED);
    }

    @Override
    public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing facing) {
        return (facing == EnumFacing.UP && state.getValue(VERTICAL) == EnumVertical.UP) ||
                (facing.getOpposite() == state.getValue(FACING) && state.getValue(VERTICAL) == EnumVertical.MID);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase player) {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, player);
        state = state.withProperty(FACING, player.getHorizontalFacing().getOpposite());
        if (player.rotationPitch > 0.0F) {
            state = state.withProperty(VERTICAL, EnumVertical.MID);
        } else {
            state = state.withProperty(VERTICAL, EnumVertical.UP);
        }
        return state;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        world.scheduleUpdate(pos, this, 1);
    }

    @Override
    public int tickRate(World world) {
        return 20;
    }

    public void getSenderState(LinkedList<Integer> list, World world, BlockPos pos) {
        int result = -1;

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityReceiver) {
            if (((TileEntityReceiver) tileEntity).getSender() != null)
                result = ((TileEntityReceiver) tileEntity).senderIsPowered() ? 1 : 0;
        }

        list.add(result);
    }

    public int getNearbyReceiverState(World world, BlockPos pos) {
        LinkedList<Integer> list = new LinkedList<>();
        getSenderState(list, world, pos.up());
        getSenderState(list, world, pos.down());
        getSenderState(list, world, pos.north());
        getSenderState(list, world, pos.south());
        getSenderState(list, world, pos.west());
        getSenderState(list, world, pos.east());

        int result = 0;
        for (int i : list) result += i;
        if (result == -6) return -1;
        result = 0;
        for (int i : list) {
            if (i >= 0) result += i;
        }
        return result;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!world.isRemote) {
            EnumFacing facing = state.getValue(FACING);
            if (state.getValue(VERTICAL) == EnumVertical.UP)
                facing = EnumFacing.DOWN;

            boolean powered = state.getValue(POWERED);
            BlockLoader.lineLight.lightCtl(world, pos, facing, 32, powered);

            if (getNearbyReceiverState(world, pos) >= 0) {
                powered = getNearbyReceiverState(world, pos) > 0;
            } else if (world.getBlockState(pos.offset(facing.getOpposite())).canProvidePower())
                powered = world.isBlockPowered(pos);
            else powered = true;

            world.setBlockState(pos, state.withProperty(POWERED, powered));
            world.notifyBlockUpdate(pos, state, state, 2);
            world.markBlockRangeForRenderUpdate(pos, pos);
            world.scheduleUpdate(pos, this, 1);
        }
    }

}
