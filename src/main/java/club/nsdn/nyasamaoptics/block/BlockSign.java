package club.nsdn.nyasamaoptics.block;

import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Glass sign panel, ported from the 1.12.2 {@code BlockSign}.
 *
 * <p>1.12.2 extended {@code BlockGlass} with no behaviour of its own -- the class existed for its
 * registry name and material. 1.21.1 has no bare {@code BlockGlass}; the plain glass behaviour is
 * {@link TransparentBlock}, which already carries the render/occlusion rules and a codec, so the
 * subclass is intentionally empty.</p>
 */
public class BlockSign extends TransparentBlock {

    public BlockSign(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
