package club.nsdn.nyasamaoptics.tileblock;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import club.nsdn.nyasamaoptics.renderer.tileblock.*;
import club.nsdn.nyasamaoptics.tileblock.light.HoloJet;
import club.nsdn.nyasamaoptics.tileblock.light.HoloJetRev;
import club.nsdn.nyasamaoptics.tileblock.light.PillarHead;
import club.nsdn.nyasamaoptics.tileblock.light.RGBLight;
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
