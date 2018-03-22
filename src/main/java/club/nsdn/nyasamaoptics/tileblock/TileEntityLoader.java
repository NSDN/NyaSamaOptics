package club.nsdn.nyasamaoptics.tileblock;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import club.nsdn.nyasamaoptics.tileblock.light.*;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class TileEntityLoader {

    public TileEntityLoader(FMLInitializationEvent event) {

        GameRegistry.registerTileEntity(
                HoloJet.TileText.class,
                "tileText");

        GameRegistry.registerTileEntity(
                RGBLight.TileLight.class,
                "tileLight");

        GameRegistry.registerTileEntity(
                PillarHead.TileText.class,
                "tilePillarHead");

        GameRegistry.registerTileEntity(
                HoloJetRev.TileText.class,
                "tileHoloJetRev");

        GameRegistry.registerTileEntity(
                LEDPlate.TilePlate.class,
                "tileLEDPlate");

    }

}
