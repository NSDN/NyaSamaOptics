package club.nsdn.nyasamaoptics.TileEntities;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import club.nsdn.nyasamaoptics.Renderers.TileEntity.*;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class TileEntityModelBinder {

    public TileEntityModelBinder(FMLInitializationEvent event) {

        ClientRegistry.bindTileEntitySpecialRenderer(
                HoloJet.TileText.class,
                new HoloJetRenderer());

        ClientRegistry.bindTileEntitySpecialRenderer(
                RGBLight.TileLight.class,
                new LightRenderer());

        ClientRegistry.bindTileEntitySpecialRenderer(
                PillarHead.TileText.class,
                new PillarHeadRenderer());

        ClientRegistry.bindTileEntitySpecialRenderer(
                HoloJetRev.TileText.class,
                new HoloJetRevRenderer());

    }

}
