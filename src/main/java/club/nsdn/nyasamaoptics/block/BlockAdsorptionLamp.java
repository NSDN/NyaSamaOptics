package club.nsdn.nyasamaoptics.block;

import club.nsdn.nyasamaoptics.NyaSamaOptics;
import club.nsdn.nyasamaoptics.creativetab.CreativeTabLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.LinkedList;

/**
 * Created by drzzm32 on 2019.2.16.
 */
public class BlockAdsorptionLamp extends Block {

    public static enum EnumVertical implements IStringSerializable {
        UP("up"),
        DOWN("down"),
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
            case DOWN:
                switch (facing) {
                    case NORTH:
                        return getAABB(x1, y1, z1, x2, y2, z2);
                    case EAST:
                        return getAABB(1.0 - z2, y1, x1, 1.0 - z1, y2, x2);
                    case SOUTH:
                        return getAABB(1.0 - x2, y1, 1.0 - z2, 1.0 - x1, y2, 1.0 - z1);
                    case WEST:
                        return getAABB(z1, y1, 1.0 - x2, z2, y2, 1.0 - x1);
                }
                break;
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

    public BlockAdsorptionLamp() {
        super(Material.GLASS);
        setUnlocalizedName("AdsorptionLamp");
        setRegistryName(NyaSamaOptics.MODID, "adsorption_lamp");
        setHardness(2.0F);
        setResistance(blockHardness * 5.0F);
        setLightLevel(1);
        setSoundType(SoundType.GLASS);
        setCreativeTab(CreativeTabLoader.tabNyaSamaOptics);

        setDefaultState(blockState.getBaseState()
                        .withProperty(FACING, EnumFacing.NORTH)
                        .withProperty(VERTICAL, EnumVertical.DOWN)
        );
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getBoxByXYZ(state, 1, 0.125, 1);
    }

    @Override
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        EnumVertical vertical = EnumVertical.DOWN;
        if ((meta & 0x4) != 0) vertical = EnumVertical.MID;
        else if ((meta & 0x8) != 0) vertical = EnumVertical.UP;
        return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3)).withProperty(VERTICAL, vertical);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumVertical vertical = state.getValue(VERTICAL);
        int v = 0x0;
        if (vertical == EnumVertical.MID) v = 0x4;
        else if (vertical == EnumVertical.UP) v = 0x8;
        return state.getValue(FACING).getHorizontalIndex() | v;
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, VERTICAL);
    }

    @Override
    public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing facing) {
        return (facing == EnumFacing.UP && state.getValue(VERTICAL) == EnumVertical.UP) ||
                (facing == EnumFacing.DOWN && state.getValue(VERTICAL) == EnumVertical.DOWN) ||
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
        if (player.rotationPitch > 22.5F) {
            state = state.withProperty(VERTICAL, EnumVertical.DOWN);
        } else if (player.rotationPitch > -22.5F) {
            state = state.withProperty(VERTICAL, EnumVertical.MID);
        } else {
            state = state.withProperty(VERTICAL, EnumVertical.UP);
        }
        return state;
    }

}
