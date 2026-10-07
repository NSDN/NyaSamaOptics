package club.nsdn.nyasamaoptics;

import net.minecraft.core.registries.Registries;
import club.nsdn.nyasamaoptics.tileblock.light.RGBLight;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Block entity types for nyasamaoptics.
 *
 * <p>1.12.2 needed none of this: a block simply returned a tile entity from createNewTileEntity and
 * the game accepted it. 1.21.1 requires every block entity to be registered against a
 * BlockEntityType, which is also what carries the ticker.</p>
 *
 * <p>PLACEHOLDER. The first device block entity (RGBLight.TileEntityRGBLight) is ported, but its type
 * registration is deliberately not wired up yet: NeoForge BlockEntityType.Builder.of(...) takes a
 * BlockEntitySupplier whose exact shape differs from the 1.12.2 constructor (the type appears to be
 * supplied by the framework rather than passed in), and pinning that down needs one more look at the
 * decompiled BlockEntityType#create / BlockEntitySupplier pair. Registering the type with a wrong
 * supplier would compile only by accident and fail at runtime, so it is left out and the remaining
 * work is recorded here:</p>
 * <ol>
 *   <li>confirm the BlockEntitySupplier signature;</li>
 *   <li>register one rgb_light type naming all ten RGB light blocks;</li>
 *   <li>register the ten blocks in OpticsBlocks, each passing the type supplier to DeviceBase;</li>
 *   <li>do the same for the other devices as they are ported.</li>
 * </ol>
 */
public final class OpticsBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NyaSamaOptics.MODID);

    private OpticsBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }

    /**
     * Shared by every RGB light variant.
     *
     * <p>Signature note, verified with javap against neoforge-21.1.256-merged.jar:
     * {@code BlockEntityType$BlockEntitySupplier<T>} is {@code T create(BlockPos, BlockState)} -- the
     * entity type is <b>not</b> passed in, unlike the {@code BlockEntity(BlockEntityType, BlockPos,
     * BlockState)} constructor. Vanilla therefore resolves its own type holder lazily inside the
     * supplier, which is what the lambda below does: {@code RGB_LIGHT.get()} is only evaluated when a
     * block entity is actually created, long after registration.</p>
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> RGB_LIGHT =
            BLOCK_ENTITIES.register("rgb_light", () -> BlockEntityType.Builder
                    .of((pos, state) -> new RGBLight.TileEntityRGBLight(rgbLightType(), pos, state),
                            RGBLight.registeredBlocks())
                    .build(null));

    /** Convenience accessor for the block classes and {@code DeviceBase}. */
    public static BlockEntityType<?> rgbLightType() {
        return RGB_LIGHT.get();
    }
}