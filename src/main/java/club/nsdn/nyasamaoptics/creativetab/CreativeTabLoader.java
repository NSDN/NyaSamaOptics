package club.nsdn.nyasamaoptics.creativetab;

import club.nsdn.nyasamaoptics.NyaSamaOptics;
import club.nsdn.nyasamaoptics.OpticsBlocks;
import club.nsdn.nyasamaoptics.OpticsItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The module's creative tab, ported from the 1.12.2 {@code CreativeTabLoader} (24 lines).
 *
 * <p>1.12.2 built a {@code CreativeTabs} anonymous subclass inside a constructor that was called from
 * {@code CommonProxy.preInit}; the tab's unlocalised name was the literal {@code tabNyaSamaOptics},
 * hence the translation key {@code itemGroup.tabNyaSamaOptics} (present in both {@code en_us.json} and
 * {@code zh_cn.json} -- the 1.12.2 value in each was carried over unchanged). The icon came from the
 * block loader, not from an item constant:</p>
 *
 * <pre>
 * 1.12.2 CreativeTabLoader:19   itemBlocks.get(BlockLoader.logo).getDefaultInstance()
 * 1.12.2 BlockLoader:82         logo = new BlockLogo()          // registry id "nso_logo"
 * </pre>
 *
 * <p>so the icon here is {@code OpticsBlocks.LOGO} (id {@code nso_logo}), the one block the 1.12.2
 * loader kept in a dedicated field for this purpose. In 1.21.1 the tab is a registry object instead of
 * an anonymous class, so this class becomes a static holder registered from the mod constructor, the
 * same shape as {@code nyasamaelectricity.creativetab.CreativeTabLoader}.</p>
 *
 * <p><b>What replaces {@code setCreativeTab}:</b> the tab enumerates its items itself. The 1.12.2
 * membership is reproduced from the two registration lists rather than guessed:</p>
 * <ul>
 *   <li>{@code ItemLoader} registered exactly one item, {@code NSOConv}, and its constructor called
 *       {@code setCreativeTab(tabNyaSamaOptics)} -- so it is in the tab;</li>
 *   <li>every {@code BlockLoader} block reached the tab through {@code setCreativeTab} in its own
 *       constructor <b>except the two {@code LightBeam} blocks</b>, whose constructor only calls
 *       {@code setUnlocalizedName}/{@code setRegistryName} (1.12.2 {@code api/LightBeam.java:33-53}).
 *       They are also the only two blocks with no {@code tile.*.name} entry in either 1.12.2 language
 *       file. They stay registered items -- as in 1.12.2, where they were reachable through
 *       {@code /give} and the search tab -- but they are kept out of the tab, which is what 1.12.2
 *       did; putting them in would add two entries that display a raw translation key.</li>
 * </ul>
 * <p>That gives 36 tab entries: 35 block items + {@code nso_conv}, out of the 37 registered block
 * items (2 light beams) and 1 registered item.</p>
 */
public class CreativeTabLoader {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NyaSamaOptics.MODID);

    /** 1.12.2 {@code tabNyaSamaOptics}. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            TABS.register("tab_nyasamaoptics", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tabNyaSamaOptics"))
                    .icon(() -> new ItemStack(OpticsBlocks.LOGO.get()))
                    .displayItems((parameters, output) -> {
                        // 1.12.2 ItemLoader: items.add(new NSOConv()), which setCreativeTab on itself.
                        for (DeferredHolder<Item, ? extends Item> item : OpticsItems.ITEMS.getEntries()) {
                            output.accept(item.get());
                        }

                        // 1.12.2 BlockLoader's ItemBlocks, minus the two LightBeam blocks (see the
                        // class note). Compared by the block's own item rather than by id so the rule
                        // breaks loudly if the two beam blocks are ever renamed or re-registered.
                        Item dotBeam = OpticsBlocks.dotLight() == null ? null : OpticsBlocks.dotLight().asItem();
                        Item lineBeam = OpticsBlocks.lineLight() == null ? null : OpticsBlocks.lineLight().asItem();
                        for (DeferredHolder<Item, ? extends Item> item : OpticsBlocks.ITEMS.getEntries()) {
                            Item value = item.get();
                            if (value == dotBeam || value == lineBeam) {
                                continue;
                            }
                            output.accept(value);
                        }
                    })
                    .build());

    private CreativeTabLoader() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
