package club.nsdn.nyasamaoptics;

import club.nsdn.nyasamaoptics.creativetab.CreativeTabLoader;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NyaSama Optics entry point (Minecraft 1.21.1 / NeoForge).
 *
 * <p>The 1.12.2 shape used {@code @Mod.Instance}, {@code @SidedProxy} and
 * {@code @Mod.EventHandler} preInit/init/postInit; as in {@code nyasamabuilding} the proxies are
 * gone and registration happens on the mod bus, which is what the constructor does now.</p>
 *
 * <p>{@code OpticsBlocks.register}, {@code OpticsItems.register} and
 * {@code OpticsBlockEntities.register} are the calls that wire this module's registries in. The last
 * one was missing through seven crashes: the block entity types were written down but never handed to
 * the mod bus, so every {@code TypeHolder} in {@code tileblock/**} stayed unbound (crash 8,
 * {@code EntityRenderersEvent$RegisterRenderers}).</p>
 *
 * <p>{@code CreativeTabLoader.register} restores the 1.12.2 {@code tabNyaSamaOptics}: 1.12.2 called its
 * constructor from {@code CommonProxy.preInit}, and the deferred register takes that place.</p>
 */
@Mod(NyaSamaOptics.MODID)
public class NyaSamaOptics {

    public static final String MODID = "nyasamaoptics";
    public static final Logger LOGGER = LoggerFactory.getLogger("NyaSamaOptics");

    public NyaSamaOptics(IEventBus modBus, ModContainer container) {
        OpticsBlocks.register(modBus);
        OpticsItems.register(modBus);
        // The block entity types were the one registry this constructor never handed to the mod bus:
        // OpticsBlockEntities.register(modBus) existed but had no caller, so none of the six types it
        // owns were ever registered and OpticsRenderers died with
        //   NullPointerException: Trying to access unbound value:
        //       ResourceKey[minecraft:block_entity_type / nyasamaoptics:ad_board]
        // when EntityRenderersEvent$RegisterRenderers dispatched (crash 8). Place is the same as
        // nyasamatelecom / opticzCraft / nyasamabuilding: a mod-bus DeferredRegister is wired from the
        // @Mod constructor, after the blocks and items and before the creative tab.
        OpticsBlockEntities.register(modBus);
        CreativeTabLoader.register(modBus);
        LOGGER.info("[{}] v3 skeleton loaded (NeoForge, MC 1.21.1)", MODID);
    }
}
