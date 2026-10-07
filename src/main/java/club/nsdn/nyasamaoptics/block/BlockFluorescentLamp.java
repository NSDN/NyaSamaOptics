package club.nsdn.nyasamaoptics.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Fluorescent lamp, ported from the 1.12.2 {@code BlockFluorescentLamp}.
 *
 * <p>All orientation behaviour lives in {@link BlockOrientedLamp}; this class only supplies the box
 * size the 1.12.2 code passed to {@code getBoxByXYZ} (1.0 x 0.1875 x 0.25). See that class for the
 * port-time de-duplication note: in 1.12.2 this and {@code BlockAdsorptionLamp} were two identical
 * 185-line copies.</p>
 */
public class BlockFluorescentLamp extends BlockOrientedLamp {

    public static final MapCodec<BlockFluorescentLamp> CODEC = simpleCodec(BlockFluorescentLamp::new);

    public BlockFluorescentLamp(BlockBehaviour.Properties properties) {
        super(properties, 1.0, 0.1875, 0.25);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
