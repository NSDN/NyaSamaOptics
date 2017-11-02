package club.nsdn.nyasamaoptics.Blocks;

import club.nsdn.nyasamaoptics.TileEntities.HoloJet;
import club.nsdn.nyasamaoptics.TileEntities.PillarHead;
import club.nsdn.nyasamaoptics.TileEntities.RGBLight;
import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2017.1.6.
 */
public class BlockLoader {

    public static LinkedHashMap<String, Block> blocks;

    private static void register(Block block, String name) {
        GameRegistry.registerBlock(block, name);
    }

    public BlockLoader(FMLPreInitializationEvent event) {
        blocks = new LinkedHashMap<String, Block>();

        blocks.put("nyasamaoptics_block_sign", new BlockSign());
        blocks.put("nyasamaoptics_nsdn_logo", new BlockNSDNLogo());
        blocks.put("nyasamaoptics_logo", new BlockNyaSamaOpticsLogo());

        blocks.put("block_holojet_lishu", new HoloJet("BlockHoloJetLiShu"));
        blocks.put("block_holojet_song", new HoloJet("BlockHoloJetSong"));

        blocks.put("block_pillar_head", new PillarHead());
        blocks.put("block_pillar_body", new RGBLight("PillarBody", "pillar_body", 1.0F, 1.0F, 0.5F));

        blocks.put("block_adsorption_lamp_large", new RGBLight("BlockAdsorptionLampLarge", "adsorption_lamp_large", 1.0F, 0.125F, 1.0F));
        blocks.put("block_adsorption_lamp_mono", new RGBLight("BlockAdsorptionLampMono", "adsorption_lamp_mono", 0.25F, 0.125F, 0.25F));
        blocks.put("block_adsorption_lamp_multi", new RGBLight("BlockAdsorptionLampMulti", "adsorption_lamp_multi", 1.0F, 0.125F, 0.25F));
        blocks.put("block_fluorescent_light", new RGBLight("BlockFluorescentLight", "fluorescent_light", 1.0F, 0.1875F,0.25F));
        blocks.put("block_fluorescent_light_flock", new RGBLight("BlockFluorescentLightFlock", "fluorescent_light_flock", 1.0F, 0.25F, 0.625F));
        blocks.put("block_mosaic_light_mono", new RGBLight("BlockMosaicLightMono", "mosaic_light_mono", 1.0F, 0.5F, 1.0F));
        blocks.put("block_mosaic_light_mono_small", new RGBLight("BlockMosaicLightMonoSmall", "mosaic_light_mono_small", 0.625F, 0.25F, 0.625F));
        blocks.put("block_mosaic_light_multi", new RGBLight("BlockMosaicLightMulti", "mosaic_light_multi", 1.0F, 0.5F, 1.0F));
        blocks.put("block_mosaic_light_multi_small", new RGBLight("BlockMosaicLightMultiSmall", "mosaic_light_multi_small", 1.0F, 0.25F, 0.625F));
        blocks.put("block_platform_light_full", new RGBLight("BlockPlatformLightFull", "platform_light_full", 1.0F, 1.0F, 0.625F));
        blocks.put("block_platform_light_half", new RGBLight("BlockPlatformLightHalf", "platform_light_half", 1.0F, 0.5F, 0.5F));

        for (String name : blocks.keySet()) {
            register(blocks.get(name), name);
        }
    }

}