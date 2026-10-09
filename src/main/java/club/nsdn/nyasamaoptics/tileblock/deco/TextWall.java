package club.nsdn.nyasamaoptics.tileblock.deco;

import club.nsdn.nyasamaoptics.NyaSamaOptics;
import club.nsdn.nyasamaoptics.util.DecoTextCore;
import club.nsdn.nyasamaoptics.util.font.FontLoader;
import club.nsdn.nyasamatelecom.api.device.DeviceBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Text wall (the "人间 / The World" plate), ported from the 1.12.2 {@code TextWall}.
 *
 * <p>Same shape of class as {@link GuideBoard} -- an oriented thin plate with a two-line
 * {@link TileEntityDecoText} behind it -- but with the wall-mounted box arithmetic (the plate lies flat
 * against the block face) and without the {@code noPillar} variant, which came from 1.12.2's
 * {@code getBoxByXYZ(state, 0.25, 0.0625, 0.25)}.</p>
 *
 * <p>Mapping notes are the same as {@link GuideBoard}'s, with these differences:</p>
 * <ul>
 *   <li>The 1.12.2 constructor took {@code name}, {@code id} and {@code light}; the first two are the
 *       registry entry's business in 1.21.1 and the third is a property, so only
 *       {@code (Properties)} remains -- which is also what lets {@code simpleCodec} name it.</li>
 *   <li>{@code getMaxRenderDistanceSquared()} (4096.0) and {@code getRenderBoundingBox()} (an 8-block
 *       radius -- the largest of the boards) move to the renderer; the numbers are recorded here for
 *       the renderer port to reuse verbatim.</li>
 *   <li>{@code isSideSolid}, {@code isOpaqueCube}, {@code isFullCube}, {@code getRenderType} and
 *       {@code getBlockLayer} are dropped for the reasons given on {@link GuideBoard}
 *       ({@code noOcclusion()} in the properties, {@code MODEL} is the default render shape, no
 *       per-face solidity hook exists in 1.21.1).</li>
 *   <li>{@code getStateFromMeta}/{@code getMetaFromState} have no counterpart: {@code FACING} is a block
 *       state property.</li>
 * </ul>
 *
 * <p><b>Script interaction (connected):</b> 1.12.2's {@code onBlockActivated} ran a {@code DecoTextCore}
 * program (read off an NGT tablet) against this wall; that is
 * {@link #useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)}
 * now, returning {@code ItemInteractionResult} -- the old {@code true} for a matching block entity
 * becomes {@code SUCCESS}, both {@code false} exits become
 * {@code PASS_TO_DEFAULT_BLOCK_INTERACTION}.</p>
 *
 * <p><b>Still deferred:</b> the render bounds noted above (4096.0 view distance squared, an 8-block
 * render box) and the text drawing itself belong to the block entity renderer, ported separately.</p>
 */
public class TextWall extends DeviceBase {

    public static final MapCodec<TextWall> CODEC = simpleCodec(TextWall::new);

    /**
     * Block entity type of this device, owned here for the same reason as on {@code AdBoard}: the type
     * must name its blocks, and the registration pass adds this register with one line
     * ({@code TextWall.TypeHolder.TYPES.register(modBus)}), exactly as it does for
     * {@code OpticsBlockEntities.BLOCK_ENTITIES}. The type is resolved lazily inside the supplier, so
     * it is only read once a block entity is actually created -- long after registration.
     */
    private static Supplier<BlockEntityType<?>> TYPE_SUPPLIER = () -> null;

    /** Block entity type holder of this device; see {@link #TYPE_SUPPLIER}. */
    public static final class TypeHolder {

        public static final DeferredRegister<BlockEntityType<?>> TYPES =
                DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NyaSamaOptics.MODID);

        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> HOLDER =
                TYPES.register("text_wall", () -> BlockEntityType.Builder
                        .of((pos, state) -> new TextWall.TileEntityTextWall(TYPE_SUPPLIER.get(), pos, state),
                                TextWall.registeredBlocks())
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

    /**
     * The blocks this class backs; filled by the registration pass (the 1.12.2 loader had a light and
     * a dark wall) with the {@code DeferredBlock} holders, and resolved by {@link #registeredBlocks()}
     * -- which the block entity type supplier only calls at registration time.
     */
    private static final java.util.List<net.neoforged.neoforge.registries.DeferredBlock<TextWall>> REGISTERED = new java.util.ArrayList<>();

    public static Block[] registeredBlocks() {
        return REGISTERED.stream().map(net.neoforged.neoforge.registries.DeferredBlock::get).toArray(Block[]::new);
    }

    /** Called by the registration pass with the holder of each block it registers. */
    public static void noteRegistered(net.neoforged.neoforge.registries.DeferredBlock<TextWall> block) {
        REGISTERED.add(block);
    }

    /** The wall block entity, matching the 1.12.2 nested class of the same name. */
    public static class TileEntityTextWall extends TileEntityDecoText {

        public TileEntityTextWall(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
            align = FontLoader.ALIGN_CENTER;
            setInfo(4, 0.25, 0.0625, 0.25);

            content = "\u4eba\u95f4";
            scale = 1.0;
            font = FontLoader.FONT_YAN;
            offsetZ = 1.5;

            subContent = "The World";
            subScale = 2.0;
            subOffsetZ = 0.5;
        }
    }

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final Direction[] HORIZONTALS =
            { Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST };

    /** [facing 2D index]; built once because getShape is on the collision path. */
    private static final VoxelShape[] SHAPES = new VoxelShape[4];

    static {
        for (Direction facing : HORIZONTALS) {
            SHAPES[facing.get2DDataValue()] = box(facing);
        }
    }

    /** 1.12.2 {@code getBoxByXYZ(state, 0.25, 0.0625, 0.25)}, verbatim. */
    private static VoxelShape box(Direction facing) {
        double x = 0.25, y = 0.0625, z = 0.25;
        double x1 = 0.5 - x / 2, y1 = 0, z1 = 0.5 - z / 2;
        double x2 = 0.5 + x / 2, y2 = y, z2 = 0.5 + z / 2;

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
    }

    public TextWall(BlockBehaviour.Properties properties) {
        super(properties, TypeHolder::get);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
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
        return SHAPES[state.getValue(FACING).get2DDataValue()];
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
     * As on {@link GuideBoard}: 1.12.2 let {@code DeviceBase.onBlockPlacedBy} set the orientation
     * metadata, and the block entity's {@code META} is re-derived here from the facing state so the two
     * agree.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        setDeviceMeta(level, pos, state.getValue(FACING).get2DDataValue());
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityTextWall(type, pos, state);
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

    /**
     * 1.12.2 {@code onBlockActivated}, carried over: the NGT tablet's program is run against this
     * wall's block entity through a {@code DecoTextCore} anonymous subclass supplying
     * {@code getWorld/getX/getY/getZ/getPlayer/getTile} (no {@code getWrapper()} any more). The
     * server-side guard, the main-hand stack and the early no-NGT-list exit are the original ones, and
     * the boolean result maps as {@code SUCCESS} / {@code PASS_TO_DEFAULT_BLOCK_INTERACTION}.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityTextWall) {
            TileEntityTextWall text = (TileEntityTextWall) blockEntity;
            if (!level.isClientSide) {
                ItemStack stack = player.getMainHandItem();
                if (!stack.isEmpty()) {

                    ListTag list = Util.getTagListFromNGT(stack);
                    if (list == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    String[][] code = NSASM.getCode(list);
                    new DecoTextCore(code) {
                        @Override
                        public Level getWorld() {
                            return level;
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
                        public Player getPlayer() {
                            return player;
                        }

                        @Override
                        public TileEntityTextWall getTile() {
                            return text;
                        }
                    }.run();

                }
            }
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /** See {@code AdBoard#registerCodec} for why the block classes hand the codec to the shared table. */
    public static DeferredHolder<MapCodec<? extends Block>, MapCodec<? extends Block>> registerCodec(
            DeferredRegister<MapCodec<? extends Block>> blockTypes) {
        return blockTypes.register("text_wall_type", () -> CODEC);
    }
}
