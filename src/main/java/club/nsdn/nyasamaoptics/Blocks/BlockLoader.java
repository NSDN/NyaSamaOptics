package club.nsdn.nyasamaoptics.Blocks;

/**
 * Created by drzzm32 on 2017.1.6.
 */


import club.nsdn.nyasamaoptics.Util.Font.HoloJet;
import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class BlockLoader {

    public static Block blockSign;
    public static Block blockNSDNLogo;
    public static Block blockNyaSamaOpticsLogo;

    public static Block blockHoloJetLiShu;
    public static Block blockHoloJetDaBiaoSong;

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

        blockHoloJetLiShu = new HoloJet("BlockHoloJetLiShu");
        register(blockHoloJetLiShu, "block_holojet_lishu");

        blockHoloJetDaBiaoSong = new HoloJet("BlockHoloJetDaBiaoSong");
        register(blockHoloJetDaBiaoSong, "block_holojet_dabiaosong");
    }

}