package club.nsdn.nyasamaoptics.Blocks;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import club.nsdn.nyasamaoptics.CreativeTab.CreativeTabLoader;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockNyaSamaOpticsLogo extends Block {

    public BlockNyaSamaOpticsLogo() {
        super(Material.glass);
        setBlockName("NyaSamaOpticsLogo");
        setBlockTextureName("nyasamaoptics:nsdn_o_logo");
        setHardness(2.0F);
        setLightLevel(1);
        setStepSound(Block.soundTypeGlass);
        setResistance(10.0F);
        setCreativeTab(CreativeTabLoader.tabNyaSamaOptics);
    }

}
