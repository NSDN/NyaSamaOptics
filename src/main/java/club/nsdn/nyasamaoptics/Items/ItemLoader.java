package club.nsdn.nyasamaoptics.Items;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import club.nsdn.nyasamaoptics.Blocks.BlockLoader;
import club.nsdn.nyasamaoptics.Util.Font.FontLoader;
import net.minecraft.item.Item;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ItemLoader {

    public static Item itemHoloJetLiShu;
    public static Item itemHoloJetDaBiaoSong;

    private static void register(Item item, String name) {
        GameRegistry.registerItem(item, name);
    }

    public ItemLoader(FMLPreInitializationEvent event) {
        itemHoloJetLiShu = new ItemHoloJet("ItemHoloJetLiShu", BlockLoader.blockHoloJetLiShu, FontLoader.FONT_LISHU);
        register(itemHoloJetLiShu, "item_holojet_lishu");

        itemHoloJetDaBiaoSong = new ItemHoloJet("ItemHoloJetDaBiaoSong", BlockLoader.blockHoloJetDaBiaoSong, FontLoader.FONT_DABIAOSONG);
        register(itemHoloJetDaBiaoSong, "item_holojet_dabiaosong");
    }
}
