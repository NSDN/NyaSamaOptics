package club.nsdn.nyasamaoptics.tileblock.deco;

import club.nsdn.nyasamaoptics.NyaSamaOptics;
import club.nsdn.nyasamatelecom.api.device.DeviceBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Guide board (the "exit" sign with an arrow above it), ported from the 1.12.2 {@code GuideBoard}.
 *
 * <p>Unlike the ad/station boards this one is oriented: 1.12.2 kept a {@code BlockHorizontal.FACING}
 * property and rotated a fixed plate box with it. That property is kept as
 * {@link BlockStateProperties#HORIZONTAL_FACING} and the plate box is reproduced in
 * {@link #getShape} -- with the facing-independent {@code getBoxByXYZ} arithmetic, which for the
 * GuideBoard's symmetric box yields the same numbers for all four facings (the same observation
 * {@code BlockOrientedLamp} records).</p>
 *
 * <p>Mapping notes:</p>
 * <ul>
 *   <li>{@code getStateFromMeta}/{@code getMetaFromState} are gone: the facing is a block state
 *       property now, so the metadata round-trip has no counterpart. Placement keeps the 1.12.2 rule
 *       ({@code player.getHorizontalFacing().getOpposite()}).</li>
 *   <li>{@code getBoundingBox} becomes {@link #getShape} (collision included through
 *       {@link #getCollisionShape}); the old module-level {@code LinkedList<AxisAlignedBB> AABBs}
 *       interning cache goes away with the {@code AxisAlignedBB} type -- the two shapes here are
 *       computed once and kept, which is what the cache was for.</li>
 *   <li>{@code isOpaqueCube}/{@code isFullCube}/{@code getBlockLayer}/{@code getRenderType} have no
 *       overrides any more: the first two are {@code noOcclusion()} in the properties, the render type
 *       defaults to {@code MODEL}, and the render layer is decided by the render type in 1.21.1.</li>
 *   <li>{@code isSideSolid} is not ported -- 1.21.1's {@code BlockBehaviour} exposes no per-face
 *       solidity hook (javap-verified), and there is no honest substitute.</li>
 *   <li>{@code getMaxRenderDistanceSquared()} (1024.0) and {@code getRenderBoundingBox()} (3-block
 *       radius) move to the renderer; the numbers are recorded here for the renderer port to reuse
 *       verbatim.</li>
 *   <li>The 1.12.2 constructor took {@code name}, {@code id}, {@code light} and {@code noPillar}. The
 *       name/id are the registry entry's business in 1.21.1 and the light level is a property, so what
 *       remains is {@code noPillar}, set through {@link #withNoPillar(boolean)} because the block is
 *       built by a {@code (Properties)} constructor codec reference (see the class note on the
 *       constructor shape used by these device ports).</li>
 * </ul>
 *
 * <p><b>Deferred (script interaction):</b> 1.12.2's {@code onBlockActivated} ran a {@code DecoTextCore}
 * program (read off an NGT tablet) against this board. {@code DecoTextCore} belongs to the optics
 * {@code util} package, which is not ported yet, so the hook is omitted rather than stubbed; in 1.21.1
 * it becomes {@code useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand,
 * BlockHitResult)} returning {@code ItemInteractionResult}.</p>
 */
public class GuideBoard extends DeviceBase {

    public static final MapCodec<GuideBoard> CODEC = simpleCodec(GuideBoard::new);

    /**
     * Block entity type of this device, owned here for the same reason as on {@code AdBoard}: the type
     * must name its blocks, and the registration pass adds this register with one line
     * ({@code GuideBoard.TypeHolder.TYPES.register(modBus)}). The type is resolved lazily inside the
     * supplier, after registration.
     */
    private static Supplier<BlockEntityType<?>> TYPE_SUPPLIER = () -> null;

    /** Block entity type holder of this device; see {@link #TYPE_SUPPLIER}. */
    public static final class TypeHolder {

        public static final DeferredRegister<BlockEntityType<?>> TYPES =
                DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NyaSamaOptics.MODID);

        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> HOLDER =
                TYPES.register("guide_board", () -> BlockEntityType.Builder
                        .of((pos, state) -> new GuideBoard.TileEntityGuideBoard(TYPE_SUPPLIER.get(), pos, state),
                                GuideBoard.registeredBlocks())
                        .build(null));

        static {
            TYPE_SUPPLIER = HOLDER;
        }

        private TypeHolder() {
        }

        /** The registered type once registration has run. */
        public static BlockEntityType<?> get() {
            return HOLDER.get();
        }
    }

    /** The blocks this class backs; filled by the registration pass (there are light and dark boards). */
    private static final java.util.List<Block> REGISTERED = new java.util.ArrayList<>();

    public static Block[] registeredBlocks() {
        return REGISTERED.toArray(Block[]::new);
    }

    /** Called by the registration pass with each block it registers. */
    public static void noteRegistered(Block block) {
        REGISTERED.add(block);
    }

    /** The board block entity, matching the 1.12.2 nested class of the same name. */
    public static class TileEntityGuideBoard extends TileEntityDecoText {

        public TileEntityGuideBoard(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
            // 1.12.2: align = FontLoader.ALIGN_CENTER (== 4)
            align = 4;
            setInfo(4, 1, 0.75, 0.25);

            content = "\u9000\u51fa";
            scale = 0.15;
            // 1.12.2: font = FontLoader.FONT_HEI (== 3)
            font = 3;
            offsetX = 0.15;

            subContent = "\u2191";
            subScale = 4.0;
            subOffsetX = -0.275;
            subOffsetZ = -0.05;
        }
    }

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final Direction[] HORIZONTALS =
            { Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST };

    /** [facing 2D index][noPillar 0/1]; built once because getShape is on the collision path. */
    private static final VoxelShape[][] SHAPES = new VoxelShape[4][2];

    static {
        for (Direction facing : HORIZONTALS) {
            SHAPES[facing.get2DDataValue()][0] = box(facing, false);
            SHAPES[facing.get2DDataValue()][1] = box(facing, true);
        }
    }

    /** 1.12.2 {@code noPillar}: the plate loses a quarter of its height and starts higher up. */
    private boolean noPillar;

    /** 1.12.2 {@code getBoxByXYZ(state, 1, 0.75, 0.25)}, verbatim. */
    private static VoxelShape box(Direction facing, boolean noPillar) {
        double x = 1, y = 0.75, z = 0.25;
        double x1 = 0.5 - x / 2, y1 = 0, z1 = 0.5 - z / 2;
        double x2 = 0.5 + x / 2, y2 = y, z2 = 0.5 + z / 2;

        VoxelShape shape;
        switch (facing) {
            case NORTH:
                shape = Shapes.box(x1, 1.0 - y2, z1, x2, 1.0 - y1, z2);
                break;
            case EAST:
                shape = Shapes.box(1.0 - z2, 1.0 - y2, x1, 1.0 - z1, 1.0 - y1, x2);
                break;
            case SOUTH:
                shape = Shapes.box(1.0 - x2, 1.0 - y2, 1.0 - z2, 1.0 - x1, 1.0 - y1, 1.0 - z1);
                break;
            case WEST:
            default:
                shape = Shapes.box(z1, 1.0 - y2, 1.0 - x2, z2, 1.0 - y1, 1.0 - x1);
                break;
        }

        if (noPillar) {
            // 1.12.2 contracted the AxisAlignedBB by 0.25 on the y axis (contract(0, 0.25, 0)).
            shape = Shapes.box(
                    shape.min(Direction.Axis.X), shape.min(Direction.Axis.Y) + 0.25, shape.min(Direction.Axis.Z),
                    shape.max(Direction.Axis.X), shape.max(Direction.Axis.Y) - 0.25, shape.max(Direction.Axis.Z));
        }
        return shape;
    }

    public GuideBoard(BlockBehaviour.Properties properties) {
        super(properties, TypeHolder::get);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    /** The 1.12.2 constructor's {@code noPillar} argument, applied to the shared codec instance. */
    public GuideBoard withNoPillar(boolean noPillar) {
        this.noPillar = noPillar;
        return this;
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
        return SHAPES[state.getValue(FACING).get2DDataValue()][noPillar ? 1 : 0];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    /**
     * 1.12.2 relied on {@code DeviceBase.onBlockPlacedBy} for the orientation metadata; that hook still
     * runs, but the facing is now a state property and the two must agree, so the block entity's
     * {@code META} is re-derived from the facing.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        setDeviceMeta(level, pos, state.getValue(FACING).get2DDataValue());
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityGuideBoard(type, pos, state);
    }

    /**
     * Kept for parity with 1.12.2, which cleared the receiver link when the device was broken.
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof TileEntityReceiver receiver) {
            receiver.onDestroy();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /*
     * NOT PORTED HERE: onBlockActivated ran a DecoTextCore program (read off an NGT tablet) to rewrite
     * this board's text -- waiting on the optics util package, as the class note says.
     */

    /**
     * Registers this block's {@code MapCodec}, as {@code AdBoard#registerCodec} does; the
     * {@code _type} suffix is what keeps the key out of the way of the block entity type registered by
     * {@link TypeHolder} under {@code guide_board} in the block entity type registry.
     */
    public static DeferredHolder<MapCodec<? extends Block>, MapCodec<? extends Block>> registerCodec(
            DeferredRegister<MapCodec<? extends Block>> blockTypes) {
        return blockTypes.register("guide_board_type", () -> CODEC);
    }
}
