package club.nsdn.nyasamaoptics.tileblock;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import club.nsdn.nyasamaoptics.tileblock.light.*;
import club.nsdn.nyasamaoptics.tileblock.screen.*;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.tileentity.TileEntity;

import java.util.LinkedHashMap;

public class TileEntityLoader {

    public static LinkedHashMap<String, Class<? extends TileEntity>> tileEntities;

    private static void register(Class<? extends TileEntity> tileEntity, String name) {
        GameRegistry.registerTileEntity(tileEntity, name);
    }

    public TileEntityLoader(FMLInitializationEvent event) {
        tileEntities = new LinkedHashMap<String, Class<? extends TileEntity>>();

        tileEntities.put("tileText", HoloJet.TileText.class);
        tileEntities.put("tileLight", RGBLight.TileLight.class);
        tileEntities.put("tilePillarHead", PillarHead.TileText.class);
        tileEntities.put("tileHoloJetRev", HoloJetRev.TileText.class);
        tileEntities.put("tileLEDPlate", LEDPlate.TilePlate.class);
        tileEntities.put("tilePlatformPlateFull", PlatformPlateFull.TilePlate.class);
        tileEntities.put("tilePlatformPlateHalf", PlatformPlateHalf.TilePlate.class);
        tileEntities.put("tileStationLamp", StationLamp.TileLamp.class);

        for (String name : tileEntities.keySet()) {
            register(tileEntities.get(name), name);
        }
    }

}
