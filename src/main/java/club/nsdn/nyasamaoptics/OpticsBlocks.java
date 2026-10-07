package club.nsdn.nyasamaoptics;

import club.nsdn.nyasamaoptics.block.BlockFluorescentLamp;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration for {@code nyasamaoptics} blocks.
 *
 * <p>Replaces the 1.12.2 {@code BlockLoader}, which used {@code RegistryEvent} plus a hand-built
 * {@code LinkedList<Block>} and a client-only {@code ModelRegistryEvent} handler for item icons.
 * Those are all gone in 1.21.1 (item models are JSON), so what remains is a table walk, exactly as in
 * {@code nyasamabuilding}.</p>
 *
 * <p>The 1.12.2 loader registered roughly 33 blocks: three glass panels (sign, NSDN logo, logo), two
 * lamps, a spotlight, and then the devices -- holo jet, pillar head, LED plate, the two platform
 * plates, station lamp, ad/station/text/guide boards, the two light beams and ten RGB light variants.
 * They are being ported a few at a time; the table below grows with them, and the block entity types
 * those devices need arrive in {@code OpticsBlockEntities}.</p>
 */
public final class OpticsBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NyaSamaOptics.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NyaSamaOptics.MODID);

    /** Every custom block class needs its codec here (1.20.5+ requirement). */
    public static final DeferredRegister<MapCodec<? extends Block>> BLOCK_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, NyaSamaOptics.MODID);

    private OpticsBlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_TYPES.register(modBus);
    }

    static {
        BLOCK_TYPES.register("fluorescent_lamp", () -> BlockFluorescentLamp.CODEC);

        // Glass material with a light level of 1 in 1.12.2 (its own light value is a full 15).
        DeferredBlock<BlockFluorescentLamp> fluorescentLamp = BLOCKS.register("fluorescent_lamp",
                () -> new BlockFluorescentLamp(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.NONE)
                        .sound(SoundType.GLASS)
                        .strength(2.0F, 10.0F)
                        .lightLevel(state -> 15)
                        .noOcclusion()));
        ITEMS.registerSimpleBlockItem(fluorescentLamp);
    }
}
