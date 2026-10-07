package club.nsdn.nyasamaoptics.api;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.thewdj.optics.ILightBeam;

/**
 * A beam of light, ported from the 1.12.2 {@code club.nsdn.nyasamaoptics.api.LightBeam}.
 *
 * <p>This is the core mechanism of the optics module: a light source block (spotlight, RGB lamp,
 * sign...) asks the beam block to lay a path of invisible "beam" blocks through the world, and the
 * beam blocks remove themselves again when their source disappears. Two variants exist, selected by
 * {@code lightType}: {@code TYPE_DOT} (beams on all six sides) and {@code TYPE_LINE} (a straight run
 * in one direction).</p>
 *
 * <p>Mapping notes -- the block is almost entirely "negative space", so most of the port is about
 * expressing that in 1.21.1 terms:</p>
 * <ul>
 *   <li>{@code Material.VINE} (plant-like, no collision, replaceable) becomes explicit properties:
 *       {@code noCollission().noOcclusion().replaceable()}. {@code replaceable()} is the modern
 *       equivalent of the old {@code canBeReplacedByLeaves} hook -- anything may overwrite a beam.</li>
 *   <li>{@code getCollisionBoundingBox} returning {@code NULL_AABB} plus {@code canCollideCheck}
 *       returning false collapse into an empty {@link Shapes#empty()} shape.</li>
 *   <li>{@code getRenderType} returning {@code INVISIBLE} becomes
 *       {@link #getRenderShape(BlockState)} returning {@link RenderShape#INVISIBLE} -- the block still
 *       exists, it simply draws nothing (no model file needed).</li>
 *   <li>{@code dropBlockAsItemWithChance} doing nothing becomes {@code noLootTable()} in the
 *       properties; hardness -1 (unbreakable) is preserved as {@code strength(-1.0F, ...)}.</li>
 *   <li>Metadata handling disappears: {@code FACING} is now
 *       {@link BlockStateProperties#FACING} (all six directions) and states serialise by name.</li>
 *   <li>{@code neighborChanged} gains a {@code movedByPiston} argument in 1.21.1, and
 *       {@code setBlockToAir}/{@code isAirBlock} become {@code removeBlock}/{@code isEmptyBlock}.</li>
 * </ul>
 *
 * <p>{@code isSideSolid} is not ported: 1.21.1's {@code BlockBehaviour} has no per-face solidity hook
 * (verified with javap), and the old override returned false everywhere -- which the empty shape
 * already expresses.</p>
 */
public class LightBeam extends Block implements ILightBeam {

    public static final MapCodec<LightBeam> CODEC = simpleCodec(LightBeam::new);

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public static final int TYPE_DOT = 0;
    public static final int TYPE_LINE = 1;

    public final int lightType;

    public LightBeam(BlockBehaviour.Properties properties) {
        this(properties, TYPE_DOT);
    }

    public LightBeam(BlockBehaviour.Properties properties, int lightType) {
        super(properties);
        this.lightType = lightType;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        checkNearby(level, pos);
    }

    public void lightCtl(Level level, BlockPos pos, boolean state) {
        lightCtl(level, pos, this, state);
    }

    public void lightCtl(Level level, BlockPos pos, Direction dir, int length, boolean state) {
        lightCtl(level, pos, this, dir, length, state);
    }

    /* ------------------------------------------------------------------ static light plumbing */

    public static Direction getDir(Level level, BlockPos pos) {
        return level.getBlockState(pos).getValue(FACING);
    }

    /** 1.12.2 used {@code EnumFacing#getIndex()}; 1.21.1 keeps the ordinal in the same 6-value order. */
    public static int getMetaFromDir(Direction dir) {
        return dir.get3DDataValue();
    }

    public static boolean isSource(Level level, BlockPos pos, Direction offset) {
        return level.getBlockState(pos.relative(offset)).getBlock() instanceof ILightSource;
    }

    public static boolean isMe(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof LightBeam;
    }

    public static boolean isMe(Level level, BlockPos pos, Direction offset) {
        return level.getBlockState(pos.relative(offset)).getBlock() instanceof LightBeam;
    }

    /**
     * Places (meta &gt;= 0) or removes (meta == -1) one beam block.
     *
     * <p>1.12.2 built the state through {@code getStateFromMeta}; the state is now selected from the
     * direction's 3D data value, which is the same ordering the old metadata used.</p>
     */
    public static boolean placeLight(Level level, BlockPos pos, LightBeam lightBeam, int meta) {
        if (meta == -1) {
            if (isMe(level, pos)) {
                level.removeBlock(pos, false);
            } else {
                return false;
            }
        } else {
            if (level.isEmptyBlock(pos) && !isMe(level, pos)) {
                level.setBlock(pos, lightBeam.defaultBlockState().setValue(FACING, Direction.from3DDataValue(meta)), 3);
            } else {
                return false;
            }
        }
        return true;
    }

    /** {@code TYPE_DOT}: beams on all six sides. */
    public static void lightCtl(Level level, BlockPos pos, LightBeam lightBeam, boolean state) {
        int meta = state ? 0 : -1;

        placeLight(level, pos.east(), lightBeam, meta);
        placeLight(level, pos.west(), lightBeam, meta);
        placeLight(level, pos.above(), lightBeam, meta);
        placeLight(level, pos.below(), lightBeam, meta);
        placeLight(level, pos.south(), lightBeam, meta);
        placeLight(level, pos.north(), lightBeam, meta);
    }

    /** {@code TYPE_LINE}: a straight run up to {@code length} blocks. */
    public static void lightCtl(Level level, BlockPos pos, LightBeam lightBeam, Direction dir, int length, boolean state) {
        int meta = state ? getMetaFromDir(dir) : -1;

        for (int i = 1; i < length; i++) {
            if (!placeLight(level, pos.relative(dir, i), lightBeam, meta)) {
                break;
            }
        }
    }

    /** Kills a beam whose source (or, for a line, the previous beam) is gone. */
    public static void checkNearby(Level level, BlockPos pos) {
        boolean keepAlive = false;

        if (level.getBlockState(pos).getBlock() instanceof LightBeam lightBeam) {
            switch (lightBeam.lightType) {
                case TYPE_DOT:
                    keepAlive |= isSource(level, pos, Direction.UP);
                    keepAlive |= isSource(level, pos, Direction.DOWN);
                    keepAlive |= isSource(level, pos, Direction.NORTH);
                    keepAlive |= isSource(level, pos, Direction.SOUTH);
                    keepAlive |= isSource(level, pos, Direction.WEST);
                    keepAlive |= isSource(level, pos, Direction.EAST);
                    break;
                case TYPE_LINE:
                default:
                    Direction dir = getDir(level, pos).getOpposite();
                    keepAlive = isSource(level, pos, dir) || isMe(level, pos, dir);
                    break;
            }
        }

        if (!keepAlive) {
            level.removeBlock(pos, false);
        }
    }
}
