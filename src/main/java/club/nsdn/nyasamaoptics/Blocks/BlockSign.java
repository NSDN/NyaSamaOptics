package club.nsdn.nyasamaoptics.Blocks;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import club.nsdn.nyasamaoptics.CreativeTab.CreativeTabLoader;

public class BlockSign extends Block {

    public BlockSign() {
        super(Material.glass);
        setBlockName("NyaSamaTelecomBlockSign");
        setBlockTextureName("nyasamaoptics:logo");
        setHardness(2.0F);
        setLightLevel(1);
        setStepSound(Block.soundTypeGlass);
        setResistance(10.0F);
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

}