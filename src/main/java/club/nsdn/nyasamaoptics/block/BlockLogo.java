package club.nsdn.nyasamaoptics.block;

import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Logo glass panel, ported from the 1.12.2 {@code BlockLogo} (see {@link BlockSign} for why the class
 * is empty). 1.12.2 kept this instance in {@code BlockLoader.logo} for the logo renderer, so the
 * separate class name is preserved rather than folded into the other panels.
 */
public class BlockLogo extends TransparentBlock {

    public BlockLogo(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
