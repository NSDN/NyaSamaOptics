package club.nsdn.nyasamaoptics.Proxy;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import club.nsdn.nyasamaoptics.Entity.EntityModelBinder;
import club.nsdn.nyasamaoptics.Event.EventRegister;
import club.nsdn.nyasamaoptics.Util.Font.FontLoader;
import club.nsdn.nyasamaoptics.Util.Font.FontRenderer;
import cpw.mods.fml.common.event.*;
import club.nsdn.nyasamaoptics.TileEntities.TileEntityModelBinder;
import net.minecraft.client.Minecraft;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        super.init(event);
        new FontLoader(Minecraft.getMinecraft().getResourceManager());
        new TileEntityModelBinder(event);
        new EntityModelBinder(event);
        EventRegister.registerClient();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
    }


}
