package club.nsdn.nyasamaoptics.tileblock.screen;

import club.nsdn.nyasamaoptics.OpticsBlocks;
import club.nsdn.nyasamaoptics.api.ILightSource;
import club.nsdn.nyasamatelecom.api.device.DeviceBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Full-height platform plate, ported from the 1.12.2 {@code PlatformPlateFull} (registry id
 * {@code platform_plate_full}).
 *
 * <p>The device is the {@link TileEntityPlatformPlate} text panel in its full box
 * ({@code setInfo(12, 1, 1, 0.625)}). Its {@code updateSignal} is the 1.12.2 one verbatim: read the
 * enabled flag from the linked sender (or default to enabled when nothing is linked), push it into
 * the beam system, and redraw when the flag changes. It differs from {@link PlatformPlateHalf} only
 * in the box and the registry name, which is why both are kept as separate classes -- 1.12.2 kept
 * them separate and their block entity types stay distinguishable.</p>
 *
 * <p>Mapping notes:</p>
 * <ul>
 *   <li>{@code DeviceBase} supplies the modern {@code newBlockEntity}/{@code getTicker} pair and the
 *       12-case {@code META} orientation; {@code createNewTileEntity} becomes
 *       {@link #createBlockEntity}.</li>
 *   <li>{@code BlockLoader.light} (the {@code TYPE_DOT} beam) is now {@code OpticsBlocks.dotLight()},
 *       resolved lazily with a null guard (see {@code RGBLight}).</li>
 *   <li>{@code breakBlock} becomes {@link #onRemove} and detaches the receiver link via
 *       {@code onDestroy}, as in 1.12.2.</li>
 *   <li><b>Deferred:</b> 1.12.2's {@code onBlockActivated} ran an {@code util.PlatformPlateCore}
 *       program (NGT tablet) to set the panel text; that core and the optics network wrapper are not
 *       ported yet, so the hook is left out and recorded at the bottom of this class.</li>
 * </ul>
 *
 * <p>Registration: 1.12.2 used {@code Material.GLASS} with light level 0 and the fixed id
 * {@code platform_plate_full}; the block and its block entity type are registered by
 * {@code OpticsBlocks} / {@code OpticsBlockEntities}, the type naming {@link #registeredBlocks()}.</p>
 */
public class PlatformPlateFull extends DeviceBase implements ILightSource {

    public static final int ALIGN_CENTER = 0, ALIGN_LEFT = 1, ALIGN_RIGHT = 2;

    /**
     * Blocks this class backs; filled by {@code OpticsBlocks} with deferred holders so the block
     * entity type can name them (same shape as {@code RGBLight}).
     */
    private static final List<DeferredBlock<PlatformPlateFull>> REGISTERED = new ArrayList<>();

    public static Block[] registeredBlocks() {
        return REGISTERED.stream().map(DeferredBlock::get).toArray(Block[]::new);
    }

    public static void noteRegistered(DeferredBlock<PlatformPlateFull> block) {
        REGISTERED.add(block);
    }

    public static class TileEntityPlatformPlateFull extends TileEntityPlatformPlate {

        public TileEntityPlatformPlateFull(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
            setInfo(12, 1, 1, 0.625);
        }

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof TileEntityPlatformPlateFull light)) {
                return;
            }

            if (light.getSender() != null) {
                light.isEnabled = light.senderIsPowered();
            } else {
                light.isEnabled = true;
            }

            if (OpticsBlocks.dotLight() != null) {
                OpticsBlocks.dotLight().lightCtl(level, pos, light.isEnabled);
            }

            if (light.isEnabled != light.prevIsEnabled) {
                light.prevIsEnabled = light.isEnabled;
                light.refresh();
            }
        }

    }

    /**
     * @param properties      block properties; 1.12.2 used {@code Material.GLASS} with light level 0
     * @param blockEntityType the registered type, supplied lazily (see {@code DeviceBase})
     */
    public PlatformPlateFull(BlockBehaviour.Properties properties,
                             Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityPlatformPlateFull(type, pos, state);
    }

    /** 1.12.2 {@code breakBlock}: detach the receiver link before the block goes away. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof TileEntityReceiver receiver) {
            receiver.onDestroy();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /*
     * NOT PORTED HERE: 1.12.2's onBlockActivated ran a PlatformPlateCore program (NGT tablet) to set
     * the panel's content, colour, scale and alignment. PlatformPlateCore lives in the optics util
     * package and needs the optics network wrapper, neither of which is ported yet, so the hook is
     * deferred rather than dropped. In 1.21.1 the hook to add is
     * useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)
     * returning ItemInteractionResult; 1.12.2 returned true for a matching block entity and false
     * otherwise, i.e. SUCCESS / PASS_TO_DEFAULT_BLOCK_INTERACTION.
     */
}
