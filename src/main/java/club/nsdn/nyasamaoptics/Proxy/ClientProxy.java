package club.nsdn.nyasamaoptics.Proxy;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import club.nsdn.nyasamaoptics.Entity.EntityModelBinder;
import club.nsdn.nyasamaoptics.Event.EventRegister;
import cpw.mods.fml.common.event.*;
import club.nsdn.nyasamaoptics.TileEntities.TileEntityModelBinder;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        org.lwjgl.opengl.Display.setTitle("moded Minecraft by NSDN-MC ver1.0");
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        super.init(event);
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
