package club.nsdn.nyasamaoptics.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Adsorption lamp, ported from the 1.12.2 {@code BlockAdsorptionLamp}.
 *
 * <p>Box size 1.0 x 0.125 x 1.0 -- the only thing that distinguished it from
 * {@code BlockFluorescentLamp}, which is why both now share {@link BlockOrientedLamp}.</p>
 */
public class BlockAdsorptionLamp extends BlockOrientedLamp {

    public static final MapCodec<BlockAdsorptionLamp> CODEC = simpleCodec(BlockAdsorptionLamp::new);

    public BlockAdsorptionLamp(BlockBehaviour.Properties properties) {
        super(properties, 1.0, 0.125, 1.0);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
