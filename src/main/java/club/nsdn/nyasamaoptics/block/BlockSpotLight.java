package club.nsdn.nyasamaoptics.block;

import club.nsdn.nyasamaoptics.OpticsBlocks;
import club.nsdn.nyasamaoptics.api.ILightSource;
import club.nsdn.nyasamaoptics.api.LightBeam;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.LinkedList;

/**
 * Spotlight, ported from the 1.12.2 {@code BlockSpotLight}.
 *
 * <p>This is the first place where optics meets telecom: the spotlight looks at its six neighbours,
 * sums the state of any {@link TileEntityReceiver} among them, and uses that (or a redstone signal) to
 * decide whether it is powered -- then drives a {@link LightBeam#TYPE_LINE} run of up to 32 blocks
 * with {@code lineLight.lightCtl(...)}.</p>
 *
 * <p>Mapping notes:</p>
 * <ul>
 *   <li>{@code onBlockAdded} + {@code tickRate} + {@code updateTick} become {@code onPlace} plus
 *       {@link #tick(BlockState, ServerLevel, BlockPos, RandomSource)}. 1.12.2's {@code tickRate}
 *       returned 20 while every schedule used a delay of 1, so it had no effect and is not carried
 *       over (noted rather than silently dropped).</li>
 *   <li>{@code getLightValue(state, world, pos)} becomes {@code lightLevel(state -> ...)} in the block
 *       properties; {@code world.isBlockPowered} becomes {@code level.hasNeighborSignal}, and
 *       {@code canProvidePower()} becomes {@code state.isSignalSource()}.</li>
 *   <li>{@code setBlockState} + {@code notifyBlockUpdate} + {@code markBlockRangeForRenderUpdate}
 *       collapse into {@code setBlockAndUpdate} + {@code sendBlockUpdated}.</li>
 *   <li>{@code BlockLoader.lineLight} is a {@code DeferredBlock} now, resolved through
 *       {@link OpticsBlocks#lineLight()}.</li>
 *   <li>{@code isSideSolid} is not ported -- no per-face solidity hook exists in 1.21.1 (verified with
 *       javap), and unlike the lamps this one cannot be expressed by the collision shape.</li>
 * </ul>
 */
public class BlockSpotLight extends Block implements ILightSource {

    public static final MapCodec<BlockSpotLight> CODEC = simpleCodec(BlockSpotLight::new);

    /** Vertical placement: ceiling-mounted or wall/floor-mounted. */
    public enum Vertical implements StringRepresentable {
        UP("up"),
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
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final Direction[] HORIZONTALS =
            { Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST };

    /** [vertical ordinal][facing 2D index]; built once because getShape is on the collision path. */
    private static final VoxelShape[][] SHAPES = new VoxelShape[2][4];

    static {
        for (Vertical vertical : Vertical.values()) {
            for (Direction facing : HORIZONTALS) {
                SHAPES[vertical.ordinal()][facing.get2DDataValue()] = box(vertical, facing, 0.625, 0.5, 0.625);
            }
        }
    }

    public BlockSpotLight(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(VERTICAL, Vertical.UP)
                .setValue(POWERED, true));
    }

    /** The 1.12.2 {@code getBoxByXYZ} math, verbatim, for this block's two vertical cases. */
    private static VoxelShape box(Vertical vertical, Direction facing, double x, double y, double z) {
        double x1 = 0.5 - x / 2, y1 = 0, z1 = 0.5 - z / 2;
        double x2 = 0.5 + x / 2, y2 = y, z2 = 0.5 + z / 2;

        switch (vertical) {
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
        builder.add(FACING, VERTICAL, POWERED);
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
        Vertical vertical = pitch > 0.0F ? Vertical.MID : Vertical.UP;
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

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        level.scheduleTick(pos, this, 1);
    }

    /** Collects a neighbouring receiver's state: 1 powered, 0 unpowered, -1 not a receiver. */
    public void getSenderState(LinkedList<Integer> list, Level level, BlockPos pos) {
        int result = -1;
        if (level.getBlockEntity(pos) instanceof TileEntityReceiver receiver) {
            if (receiver.getSender() != null) {
                result = receiver.senderIsPowered() ? 1 : 0;
            }
        }
        list.add(result);
    }

    public int getNearbyReceiverState(Level level, BlockPos pos) {
        LinkedList<Integer> list = new LinkedList<>();
        getSenderState(list, level, pos.above());
        getSenderState(list, level, pos.below());
        getSenderState(list, level, pos.north());
        getSenderState(list, level, pos.south());
        getSenderState(list, level, pos.west());
        getSenderState(list, level, pos.east());

        int result = 0;
        for (int i : list) {
            result += i;
        }
        if (result == -6) {
            return -1;
        }
        result = 0;
        for (int i : list) {
            if (i >= 0) {
                result += i;
            }
        }
        return result;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(FACING);
        if (state.getValue(VERTICAL) == Vertical.UP) {
            facing = Direction.DOWN;
        }

        boolean powered = state.getValue(POWERED);
        LightBeam lineLight = OpticsBlocks.lineLight();
        if (lineLight != null) {
            lineLight.lightCtl(level, pos, facing, 32, powered);
        }

        int nearby = getNearbyReceiverState(level, pos);
        if (nearby >= 0) {
            powered = nearby > 0;
        } else if (level.getBlockState(pos.relative(facing.getOpposite())).isSignalSource()) {
            powered = level.hasNeighborSignal(pos);
        } else {
            powered = true;
        }

        BlockState updated = state.setValue(POWERED, powered);
        level.setBlockAndUpdate(pos, updated);
        level.sendBlockUpdated(pos, state, updated, 2);
        level.scheduleTick(pos, this, 1);
    }
}
