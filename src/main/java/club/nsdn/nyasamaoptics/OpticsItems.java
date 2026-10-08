package club.nsdn.nyasamaoptics;

import club.nsdn.nyasamaoptics.item.NSOConv;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item registration for {@code nyasamaoptics}.
 *
 * <p>Replaces the 1.12.2 {@code ItemLoader}, which used {@code RegistryEvent.Register<Item>} plus a
 * hand-built {@code LinkedList<Item>} and a client-only {@code ModelRegistryEvent} handler for the
 * icons. As in {@code nyasamabuilding}, the model handler has no successor -- item models are plain
 * JSON under {@code assets/<modid>/models/item/} in 1.21.1 -- so only the registry is left.</p>
 *
 * <p>1.12.2 registered exactly one optics item, {@code NSOConv}; it is the only entry here. Its
 * properties reproduce what the old {@code ToolBase} constructor forced on every tool:
 * {@code setMaxStackSize(1)} and {@code setMaxDamage(ToolMaterial.IRON.getMaxUses())}, which
 * {@link NSOConv} still applies through {@code ToolBase(properties, Tiers.IRON)}.</p>
 */
public final class OpticsItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NyaSamaOptics.MODID);

    /** 1.12.2 {@code ItemLoader}: {@code items.add(new NSOConv())}, registry name {@code nso_conv}. */
    public static final DeferredItem<NSOConv> NSO_CONV =
            ITEMS.register("nso_conv", () -> new NSOConv(new Item.Properties()));

    private OpticsItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
