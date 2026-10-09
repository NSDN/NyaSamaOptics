package club.nsdn.nyasamaoptics.tileblock.holo;

import club.nsdn.nyasamaoptics.NyaSamaOptics;
import club.nsdn.nyasamaoptics.api.ILightSource;
import club.nsdn.nyasamaoptics.util.PillarHeadCore;
import club.nsdn.nyasamaoptics.util.font.FontLoader;
import club.nsdn.nyasamatelecom.api.device.DeviceBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Pillar head, ported from the 1.12.2 {@code PillarHead}.
 *
 * <p>The cap of a pillar, carrying a downward-aligned holographic text on all four sides (its
 * orientation metadata is yaw-only, so the glyphs are laid around the pillar rather than on one face).
 * Shares {@link TileEntityHoloText} with the holo jet and the deco boards.</p>
 *
 * <p>Mapping notes:</p>
 * <ul>
 *   <li>{@code Material.GLASS} with light level 0 and the 1.12.2 {@code "PillarHead"} name become the
 *       {@code Properties} supplied by the registration table (the registry entry names the block).</li>
 *   <li>{@code onBlockPlacedBy} used the yaw alone ({@code MathHelper.floor(rotationYaw * 4.0F / 360.0F
 *       + 0.5D) & 3}) via {@code setDeviceMeta}. {@link DeviceBase#setPlacedBy} additionally selects the
 *       level/ceiling groups ({@code +4}/{@code +8}), which this block must not have, so the override
 *       below reproduces the 1.12.2 arithmetic with {@link Mth#floor}.</li>
 *   <li>{@code breakBlock} + {@code onDestroy()} becomes
 *       {@link #onRemove(BlockState, Level, BlockPos, BlockState, boolean)}.</li>
 *   <li>{@code getMaxRenderDistanceSquared()} (4096.0) and {@code getRenderBoundingBox()} (a 5-block
 *       radius) move to the renderer; the numbers are recorded here for the renderer port to reuse
 *       verbatim.</li>
 * </ul>
 *
 * <p><b>Script interaction (connected):</b> 1.12.2's {@code onBlockActivated} read an NSASM program off
 * the held NGT tablet and ran a {@code PillarHeadCore} against this block entity; that is
 * {@link #useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)}
 * now, returning {@code ItemInteractionResult} -- the old {@code true} for a matching block entity is
 * {@code SUCCESS}, both {@code false} exits are {@code PASS_TO_DEFAULT_BLOCK_INTERACTION}. The
 * alignment literal is <b>gone</b>: the constructor now names
 * {@code club.nsdn.nyasamaoptics.util.font.FontLoader.ALIGN_DOWN} directly.</p>
 *
 * <p><b>Still deferred (renderer):</b> the render bounds noted above (4096.0 view distance squared, a
 * 5-block render box) and the drawing of {@link TileEntityHoloText#model} belong to the block entity
 * renderer, ported separately.</p>
 */
public class PillarHead extends DeviceBase implements ILightSource {

    public static final MapCodec<PillarHead> CODEC = simpleCodec(PillarHead::new);

    /**
     * Block entity type of this device, owned here for the same reason as on {@code AdBoard}: the type
     * must name its blocks, and the registration pass adds this register with one line
     * ({@code PillarHead.TypeHolder.TYPES.register(modBus)}), exactly as it does for
     * {@code OpticsBlockEntities.BLOCK_ENTITIES}. The type is resolved lazily inside the supplier, so
     * it is only read once a block entity is actually created -- long after registration.
     */
    private static Supplier<BlockEntityType<?>> TYPE_SUPPLIER = () -> null;

    /** Block entity type holder of this device; see {@link #TYPE_SUPPLIER}. */
    public static final class TypeHolder {

        public static final DeferredRegister<BlockEntityType<?>> TYPES =
                DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NyaSamaOptics.MODID);

        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> HOLDER =
                TYPES.register("pillar_head", () -> BlockEntityType.Builder
                        .of((pos, state) -> new PillarHead.TileEntityPillarHead(TYPE_SUPPLIER.get(), pos, state),
                                PillarHead.registeredBlocks())
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
     * The blocks this class backs; filled by the registration pass (there is exactly one pillar head)
     * with the {@code DeferredBlock} holder, and resolved by {@link #registeredBlocks()} -- which the
     * block entity type supplier only calls at registration time.
     */
    private static final java.util.List<net.neoforged.neoforge.registries.DeferredBlock<PillarHead>> REGISTERED = new java.util.ArrayList<>();

    public static Block[] registeredBlocks() {
        return REGISTERED.stream().map(net.neoforged.neoforge.registries.DeferredBlock::get).toArray(Block[]::new);
    }

    /** Called by the registration pass with the holder of each block it registers. */
    public static void noteRegistered(net.neoforged.neoforge.registries.DeferredBlock<PillarHead> block) {
        REGISTERED.add(block);
    }

    /** Pillar head block entity: the shared holographic text state, aligned downward on a 1x1x1 box. */
    public static class TileEntityPillarHead extends TileEntityHoloText {

        public TileEntityPillarHead(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
            align = FontLoader.ALIGN_DOWN;
            setInfo(4, 1, 1, 1);
        }
    }

    public PillarHead(BlockBehaviour.Properties properties) {
        super(properties, TypeHolder::get);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityPillarHead(type, pos, state);
    }

    /**
     * The 1.12.2 orientation rule, verbatim: yaw only, no pitch terms.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        int val = Mth.floor((double) (placer.getYRot() * 4.0F / 360.0F) + 0.5D) & 3;
        setDeviceMeta(level, pos, val);
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
     * hologram through a {@code PillarHeadCore} anonymous subclass supplying
     * {@code getWorld/getX/getY/getZ/getPlayer/getTile} (no {@code getWrapper()} any more). The
     * server-side guard, the main-hand stack and the early no-NGT-list exit are the original ones, and
     * the boolean result maps as {@code SUCCESS} / {@code PASS_TO_DEFAULT_BLOCK_INTERACTION}.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityPillarHead) {
            TileEntityPillarHead text = (TileEntityPillarHead) blockEntity;
            if (!level.isClientSide) {
                ItemStack stack = player.getMainHandItem();
                if (!stack.isEmpty()) {

                    ListTag list = Util.getTagListFromNGT(stack);
                    if (list == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    String[][] code = NSASM.getCode(list);
                    new PillarHeadCore(code) {
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
                        public TileEntityPillarHead getTile() {
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
        return blockTypes.register("pillar_head_type", () -> CODEC);
    }
}
