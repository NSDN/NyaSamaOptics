package club.nsdn.nyasamaoptics.tileblock.holo;

import club.nsdn.nyasamaoptics.NyaSamaOptics;
import club.nsdn.nyasamaoptics.api.ILightSource;
import club.nsdn.nyasamatelecom.api.device.DeviceBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Holo jet (reversed), ported from the 1.12.2 {@code HoloJetRev}.
 *
 * <p>A holographic text display whose glyphs are enabled by its linked transmitter; the "rev" suffix
 * distinguishes it from the (unported) jet in the same family. The block itself is thin: all the state
 * lives in {@link TileEntityHoloText}, whose default content/colour/thickness/scale it keeps and for
 * which it only fixes the alignment and the box size.</p>
 *
 * <p>Mapping notes:</p>
 * <ul>
 *   <li>{@code Material.GLASS} with light level 0, plus the 1.12.2 {@code "HoloJetRev"} name argument,
 *       become the {@code Properties} supplied by the registration table (the registry entry names the
 *       block), exactly as on the other device ports.</li>
 *   <li>{@code breakBlock} + {@code onDestroy()} becomes
 *       {@link #onRemove(BlockState, Level, BlockPos, BlockState, boolean)}.</li>
 *   <li>{@code getMaxRenderDistanceSquared()} (65536.0 -- by far the largest in the module, 256 blocks)
 *       moves to the renderer; the number is recorded here for the renderer port to reuse
 *       <b>verbatim</b>. 1.12.2 declared no {@code getRenderBoundingBox} here, so the inherited
 *       {@code INFINITE_EXTENT_AABB} of {@link TileEntityHoloText} applied; that is also the renderer's
 *       business now.</li>
 *   <li>The 1.12.2 constructor never touched the orientation metadata, so
 *       {@link #setPlacedBy(Level, BlockPos, BlockState, LivingEntity, ItemStack)} resets it to 0:
 *       {@code DeviceBase}'s inherited placement hook would otherwise rotate the hologram by the
 *       player's pitch, which the original did not do.</li>
 * </ul>
 *
 * <p><b>Deferred (script interaction):</b> 1.12.2's {@code onBlockActivated} read an NSASM program off
 * the held NGT tablet and ran a {@code HoloJetRevCore} against this block entity.
 * {@code HoloJetRevCore} belongs to the optics {@code util} package, which is not ported yet, so the
 * hook is omitted rather than stubbed; in 1.21.1 it becomes
 * {@code useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)}
 * returning {@code ItemInteractionResult}.</p>
 */
public class HoloJetRev extends DeviceBase implements ILightSource {

    public static final MapCodec<HoloJetRev> CODEC = simpleCodec(HoloJetRev::new);

    /**
     * Block entity type of this device, owned here for the same reason as on {@code AdBoard}: the type
     * must name its blocks, and the registration pass adds this register with one line
     * ({@code HoloJetRev.TypeHolder.TYPES.register(modBus)}), exactly as it does for
     * {@code OpticsBlockEntities.BLOCK_ENTITIES}. The type is resolved lazily inside the supplier, so
     * it is only read once a block entity is actually created -- long after registration.
     */
    private static Supplier<BlockEntityType<?>> TYPE_SUPPLIER = () -> null;

    /** Block entity type holder of this device; see {@link #TYPE_SUPPLIER}. */
    public static final class TypeHolder {

        public static final DeferredRegister<BlockEntityType<?>> TYPES =
                DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NyaSamaOptics.MODID);

        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> HOLDER =
                TYPES.register("holo_jet_rev", () -> BlockEntityType.Builder
                        .of((pos, state) -> new HoloJetRev.TileEntityHoloJetRev(TYPE_SUPPLIER.get(), pos, state),
                                HoloJetRev.registeredBlocks())
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

    /** The blocks this class backs; filled by the registration pass (there is exactly one holo jet (rev)). */
    private static final java.util.List<Block> REGISTERED = new java.util.ArrayList<>();

    public static Block[] registeredBlocks() {
        return REGISTERED.toArray(Block[]::new);
    }

    /** Called by the registration pass with each block it registers. */
    public static void noteRegistered(Block block) {
        REGISTERED.add(block);
    }

    /** Holo jet block entity: the shared holographic text state, with the jet's alignment and box. */
    public static class TileEntityHoloJetRev extends TileEntityHoloText {

        public TileEntityHoloJetRev(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
            // 1.12.2: align = FontLoader.ALIGN_CENTER (== 4)
            align = 4;
            setInfo(12, 1, 0.25, 0.5);
        }
    }

    public HoloJetRev(BlockBehaviour.Properties properties) {
        super(properties, TypeHolder::get);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityHoloJetRev(type, pos, state);
    }

    /**
     * The 1.12.2 jet carried no orientation metadata, so the inherited placement orientation from
     * {@link DeviceBase#setPlacedBy} is overridden back to {@code 0} (see the class note).
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        setDeviceMeta(level, pos, 0);
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
     * NOT PORTED HERE: onBlockActivated ran a HoloJetRevCore program (read off an NGT tablet) to set
     * this hologram's text -- waiting on the optics util package, as the class note says.
     */

    /** See {@code AdBoard#registerCodec} for why the block classes hand the codec to the shared table. */
    public static DeferredHolder<MapCodec<? extends Block>, MapCodec<? extends Block>> registerCodec(
            DeferredRegister<MapCodec<? extends Block>> blockTypes) {
        return blockTypes.register("holo_jet_rev_type", () -> CODEC);
    }
}
