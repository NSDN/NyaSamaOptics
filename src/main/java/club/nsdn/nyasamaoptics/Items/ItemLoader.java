package club.nsdn.nyasamaoptics.Items;

import club.nsdn.nyasamaoptics.Blocks.BlockLoader;
import club.nsdn.nyasamaoptics.Util.Font.FontLoader;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2017.1.6.
 */
public class ItemLoader {

    public static LinkedHashMap<String, Item> items;

    private static void register(Item item, String name) {
        GameRegistry.registerItem(item, name);
    }

    public ItemLoader(FMLPreInitializationEvent event) {
        items = new LinkedHashMap<String, Item>();

        items.put("item_holojet_lishu", new ItemHoloJet("ItemHoloJetLiShu", BlockLoader.blocks.get("block_holojet_lishu"), FontLoader.FONT_LISHU));
        items.put("item_holojet_song", new ItemHoloJet("ItemHoloJetSong", BlockLoader.blocks.get("block_holojet_song"), FontLoader.FONT_SONG));

        for (String name : items.keySet()) {
            register(items.get(name), name);
        }
    }
}
