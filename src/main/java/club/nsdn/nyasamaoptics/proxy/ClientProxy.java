package club.nsdn.nyasamaoptics.proxy;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import club.nsdn.nyasamaoptics.block.BlockLoader;
import club.nsdn.nyasamaoptics.event.EventRegister;
import club.nsdn.nyasamaoptics.renderer.tileblock.LightRenderer;
import club.nsdn.nyasamaoptics.tileblock.light.RGBLight;
import club.nsdn.nyasamaoptics.util.font.FontLoader;
import cpw.mods.fml.common.event.*;
import club.nsdn.nyasamaoptics.tileblock.TileEntityModelBinder;
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

        for (String id : BlockLoader.blocks.keySet()) { //Load RGB Lights models
            if (BlockLoader.blocks.get(id) instanceof RGBLight) {
                RGBLight light = (RGBLight) BlockLoader.blocks.get(id);
                LightRenderer.loadModel(light);
            }
        }

        EventRegister.registerClient();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
    }


}
