package club.nsdn.nyasamaoptics.Blocks;

/**
 * Created by drzzm32 on 2017.1.6.
 */


import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class BlockLoader {

    public static Block blockSign;
    public static Block blockNSDNLogo;
    public static Block blockNyaSamaOpticsLogo;

    private static void register(Block block, String name) {
        GameRegistry.registerBlock(block, name);
    }

    public BlockLoader(FMLPreInitializationEvent event) {
        blockSign = new BlockSign();
        register(blockSign, "nyasamaoptics_block_sign");

        blockNSDNLogo = new BlockNSDNLogo();
        register(blockNSDNLogo, "nyasamaoptics_nsdn_logo");

        blockNyaSamaOpticsLogo = new BlockNyaSamaOpticsLogo();
        register(blockNyaSamaOpticsLogo, "nyasamaoptics_logo");

    }

}