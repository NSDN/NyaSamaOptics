package club.nsdn.nyasamaoptics.Proxy;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import club.nsdn.nyasamaoptics.Entity.EntityLoader;
import club.nsdn.nyasamaoptics.Event.EventRegister;
import cpw.mods.fml.common.event.*;
import club.nsdn.nyasamaoptics.Items.ItemLoader;
import club.nsdn.nyasamaoptics.Blocks.BlockLoader;
import club.nsdn.nyasamaoptics.CreativeTab.CreativeTabLoader;
import club.nsdn.nyasamaoptics.TileEntities.TileEntityLoader;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event)
    {
        new CreativeTabLoader(event);
        new BlockLoader(event);
        new ItemLoader(event);
    }

    public void init(FMLInitializationEvent event)
    {
        new TileEntityLoader(event);
        new EntityLoader(event);
        EventRegister.registerCommon();
    }

    public void postInit(FMLPostInitializationEvent event)
    {

    }

}
