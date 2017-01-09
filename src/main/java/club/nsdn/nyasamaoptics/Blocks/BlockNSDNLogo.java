package club.nsdn.nyasamaoptics.Blocks;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import club.nsdn.nyasamaoptics.CreativeTab.CreativeTabLoader;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockNSDNLogo extends Block {

    public BlockNSDNLogo() {
        super(Material.glass);
        setBlockName("NyaSamaOpticsNSDNLogo");
        setBlockTextureName("nyasamaoptics:nsdn_logo");
        setHardness(2.0F);
        setLightLevel(1);
        setStepSound(Block.soundTypeGlass);
        setResistance(10.0F);
        setCreativeTab(CreativeTabLoader.tabNyaSamaOptics);
    }

}
