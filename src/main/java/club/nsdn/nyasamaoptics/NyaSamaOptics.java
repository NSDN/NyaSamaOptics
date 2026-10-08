package club.nsdn.nyasamaoptics;

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
 * <p>{@code OpticsBlocks.register} and {@code OpticsItems.register} are both listed in
 * {@code docs/12-HANDOVER.md} as the calls that wire this module's registries in. The module's block
 * entity types stay unwired for now ({@link OpticsBlockEntities#register}): the devices that would
 * need them are still being ported, and the handover note records the remaining work.</p>
 */
@Mod(NyaSamaOptics.MODID)
public class NyaSamaOptics {

    public static final String MODID = "nyasamaoptics";
    public static final Logger LOGGER = LoggerFactory.getLogger("NyaSamaOptics");

    public NyaSamaOptics(IEventBus modBus, ModContainer container) {
        OpticsBlocks.register(modBus);
        OpticsItems.register(modBus);
        LOGGER.info("[{}] v3 skeleton loaded (NeoForge, MC 1.21.1)", MODID);
    }
}
