package club.nsdn.nyasamaoptics.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Fluorescent lamp, ported from the 1.12.2 {@code BlockFluorescentLamp}.
 *
 * <p>This is the first optics block, and it establishes the pattern used by the whole module: the
 * device's orientation lives on the <b>block state</b> (a vertical enum plus a horizontal facing)
 * rather than on a block entity, so the 12-case orientation math that
 * {@code TileEntityBase.setBoundsByXYZ} does for block entities is mirrored here on the block side --
 * in 1.12.2 through {@code getBoundingBox} and now through a precomputed {@link VoxelShape} table.</p>
 *
 * <p>Mapping notes:</p>
 * <ul>
 *   <li>{@code PropertyEnum<EnumVertical>} with {@code IStringSerializable} becomes
 *       {@link EnumProperty} with {@link StringRepresentable}; {@code BlockHorizontal.FACING} becomes
 *       {@link BlockStateProperties#HORIZONTAL_FACING}.</li>
 *   <li>Metadata packing ({@code getStateFromMeta}/{@code getMetaFromState}, including the
 *       {@code 0x4}/{@code 0x8} bits) disappears -- 1.21.1 serialises states by name.</li>
 *   <li>{@code getStateForPlacement(world, pos, facing, hitX.., meta, player)} becomes
 *       {@link #getStateForPlacement(BlockPlaceContext)}; the pitch thresholds (22.5) are unchanged.</li>
 *   <li>The 1.12.2 AABB cache ({@code static LinkedList<AxisAlignedBB> AABBs} with an equality scan)
 *       is not needed: shapes are built once into a table and never allocated per query.</li>
 *   <li>{@code isSideSolid} is not ported: verified earlier with javap that {@code BlockBehaviour} has
 *       no per-face solidity hook in 1.21.1 (it follows from the collision shape). The 1.12.2 override
 *       made a few faces solid that are not solid by shape, so this is a known deviation, noted rather
 *       than faked by inflating the collision box.</li>
 * </ul>
 */
public class BlockFluorescentLamp extends Block {

    public static final MapCodec<BlockFluorescentLamp> CODEC = simpleCodec(BlockFluorescentLamp::new);

    /** Vertical placement of the lamp; names kept from 1.12.2 for diff readability. */
    public enum Vertical implements StringRepresentable {
        UP("up"),
        DOWN("down"),
        MID("mid");

        private final String name;

        Vertical(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final EnumProperty<Vertical> VERTICAL = EnumProperty.create("vertical", Vertical.class);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** [vertical ordinal][facing 2D index]; built once because getShape is on the collision path. */
    private static final VoxelShape[][] SHAPES = new VoxelShape[3][4];

    static {
        for (Vertical vertical : Vertical.values()) {
            for (Direction facing : new Direction[] { Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST }) {
                SHAPES[vertical.ordinal()][facing.get2DDataValue()] = box(vertical, facing, 1.0, 0.1875, 0.25);
            }
        }
    }

    public BlockFluorescentLamp(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(VERTICAL, Vertical.DOWN));
    }

    /** The 1.12.2 {@code getBoxByXYZ} math, verbatim, for the three vertical cases this block has. */
    private static VoxelShape box(Vertical vertical, Direction facing, double x, double y, double z) {
        double x1 = 0.5 - x / 2, y1 = 0, z1 = 0.5 - z / 2;
        double x2 = 0.5 + x / 2, y2 = y, z2 = 0.5 + z / 2;

        switch (vertical) {
            case DOWN:
                switch (facing) {
                    case NORTH:
                        return Shapes.box(x1, y1, z1, x2, y2, z2);
                    case EAST:
                        return Shapes.box(1.0 - z2, y1, x1, 1.0 - z1, y2, x2);
                    case SOUTH:
                        return Shapes.box(1.0 - x2, y1, 1.0 - z2, 1.0 - x1, y2, 1.0 - z1);
                    case WEST:
                    default:
                        return Shapes.box(z1, y1, 1.0 - x2, z2, y2, 1.0 - x1);
                }
            case MID:
                switch (facing) {
                    case NORTH:
                        return Shapes.box(1.0 - x2, z1, 1.0 - y2, 1.0 - x1, z2, 1.0 - y1);
                    case EAST:
                        return Shapes.box(y1, z1, 1.0 - x2, y2, z2, 1.0 - x1);
                    case SOUTH:
                        return Shapes.box(x1, z1, y1, x2, z2, y2);
                    case WEST:
                    default:
                        return Shapes.box(1.0 - y2, z1, x1, 1.0 - y1, z2, x2);
                }
            case UP:
            default:
                switch (facing) {
                    case NORTH:
                        return Shapes.box(x1, 1.0 - y2, z1, x2, 1.0 - y1, z2);
                    case EAST:
                        return Shapes.box(1.0 - z2, 1.0 - y2, x1, 1.0 - z1, 1.0 - y1, x2);
                    case SOUTH:
                        return Shapes.box(1.0 - x2, 1.0 - y2, 1.0 - z2, 1.0 - x1, 1.0 - y1, 1.0 - z1);
                    case WEST:
                    default:
                        return Shapes.box(z1, 1.0 - y2, 1.0 - x2, z2, 1.0 - y1, 1.0 - x1);
                }
        }
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VERTICAL);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(VERTICAL).ordinal()][state.getValue(FACING).get2DDataValue()];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        float pitch = context.getPlayer() == null ? 0.0F : context.getPlayer().getXRot();

        Vertical vertical;
        if (pitch > 22.5F) {
            vertical = Vertical.DOWN;
        } else if (pitch > -22.5F) {
            vertical = Vertical.MID;
        } else {
            vertical = Vertical.UP;
        }
        return defaultBlockState().setValue(FACING, facing).setValue(VERTICAL, vertical);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
