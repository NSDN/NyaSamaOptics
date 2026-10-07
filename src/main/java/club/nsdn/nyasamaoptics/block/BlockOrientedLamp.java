package club.nsdn.nyasamaoptics.block;

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
 * Shared base for the optics lamps that are oriented by a vertical case plus a horizontal facing.
 *
 * <p><b>Port-time refactoring, deliberately recorded.</b> In 1.12.2 {@code BlockAdsorptionLamp} and
 * {@code BlockFluorescentLamp} were two 185-line copies of the same class: identical state handling,
 * identical 12-case orientation math, identical placement rules, differing only in the three numbers
 * passed to {@code getBoxByXYZ} and in their registry names. Copying that duplication into the port
 * would have meant maintaining the orientation table twice, so the shared half lives here and each
 * lamp supplies its own box size. The observable behaviour is unchanged.</p>
 *
 * <p>The orientation math is the same 12-case table as {@code TileEntityBase.setBoundsByXYZ} in the
 * telecom framework -- here applied on the block side (three vertical cases x four facings), because
 * these lamps carry their orientation in the block state rather than in a block entity.</p>
 *
 * <p>Also noted: 1.12.2's {@code isSideSolid} override (a few faces solid beyond the collision shape)
 * is not ported, because 1.21.1's {@code BlockBehaviour} exposes no per-face solidity hook -- verified
 * with javap. Faking it by inflating the collision box would change where players stand.</p>
 */
public abstract class BlockOrientedLamp extends Block {

    /** Vertical placement; names kept from 1.12.2 for diff readability. */
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

    private static final Direction[] HORIZONTALS =
            { Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST };

    /** [vertical ordinal][facing 2D index]; built once per block instance. */
    private final VoxelShape[][] shapes = new VoxelShape[3][4];

    protected BlockOrientedLamp(BlockBehaviour.Properties properties, double x, double y, double z) {
        super(properties);
        for (Vertical vertical : Vertical.values()) {
            for (Direction facing : HORIZONTALS) {
                shapes[vertical.ordinal()][facing.get2DDataValue()] = box(vertical, facing, x, y, z);
            }
        }
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(VERTICAL, Vertical.DOWN));
    }

    /** The 1.12.2 {@code getBoxByXYZ} math, verbatim. */
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VERTICAL);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes[state.getValue(VERTICAL).ordinal()][state.getValue(FACING).get2DDataValue()];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    /** Pitch picks the vertical case (22.5 thresholds unchanged from 1.12.2), yaw the facing. */
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
