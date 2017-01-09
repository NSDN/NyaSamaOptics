package club.nsdn.nyasamaoptics.TileEntities;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import club.nsdn.nyasamaoptics.Renderers.TileEntity.*;
import club.nsdn.nyasamaoptics.Util.Font.FontRenderer;
import club.nsdn.nyasamaoptics.Util.Font.HoloJet;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class TileEntityModelBinder {

    public TileEntityModelBinder(FMLInitializationEvent event) {

        ClientRegistry.bindTileEntitySpecialRenderer(
                HoloJet.TileText.class,
                new FontRenderer());

    }

}
