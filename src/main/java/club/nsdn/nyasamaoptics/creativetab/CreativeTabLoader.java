package club.nsdn.nyasamaoptics.creativetab;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import net.minecraft.item.Item;
import cpw.mods.fml.common.event.*;
import net.minecraft.creativetab.CreativeTabs;
import club.nsdn.nyasamaoptics.block.BlockLoader;

public class CreativeTabLoader {

    public static CreativeTabs tabNyaSamaOptics;

    public CreativeTabLoader(FMLPreInitializationEvent event) {
        tabNyaSamaOptics = new CreativeTabs("tabNyaSamaOptics") {
            @Override
            public Item getTabIconItem() {
                return Item.getItemFromBlock(BlockLoader.logo);
            }
        };
    }

}
