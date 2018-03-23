package club.nsdn.nyasamaoptics.tileblock;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import club.nsdn.nyasamaoptics.renderer.tileblock.*;
import club.nsdn.nyasamaoptics.tileblock.light.*;
import club.nsdn.nyasamaoptics.tileblock.screen.*;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import java.util.LinkedHashMap;

public class TileEntityModelBinder {

    public static LinkedHashMap<Class<? extends TileEntity>, TileEntitySpecialRenderer> renderers;

    private static void bind(TileEntitySpecialRenderer renderer, Class<? extends TileEntity> tileEntity) {
        ClientRegistry.bindTileEntitySpecialRenderer(tileEntity, renderer);
    }

    public TileEntityModelBinder(FMLInitializationEvent event) {
        renderers = new LinkedHashMap<Class<? extends TileEntity>, TileEntitySpecialRenderer>();

        renderers.put(HoloJet.TileText.class, new HoloJetRenderer());
        renderers.put(RGBLight.TileLight.class, new LightRenderer());
        renderers.put(PillarHead.TileText.class, new PillarHeadRenderer());
        renderers.put(HoloJetRev.TileText.class, new HoloJetRevRenderer());
        renderers.put(LEDPlate.TilePlate.class, new LEDPlateRenderer());
        renderers.put(PlatformPlateFull.TilePlate.class, new PlatformPlateRenderer(false));
        renderers.put(PlatformPlateHalf.TilePlate.class, new PlatformPlateRenderer(true));

        for (Class<? extends TileEntity> tile : renderers.keySet()) {
            bind(renderers.get(tile), tile);
        }
    }

}
