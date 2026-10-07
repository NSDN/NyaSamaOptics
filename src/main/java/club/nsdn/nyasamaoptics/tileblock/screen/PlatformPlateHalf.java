package club.nsdn.nyasamaoptics.tileblock.screen;

import club.nsdn.nyasamaoptics.OpticsBlocks;
import club.nsdn.nyasamaoptics.api.ILightSource;
import club.nsdn.nyasamaoptics.util.PlatformPlateCore;
import club.nsdn.nyasamatelecom.api.device.DeviceBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Half-height platform plate, ported from the 1.12.2 {@code PlatformPlateHalf} (registry id
 * {@code platform_plate_half}).
 *
 * <p>Identical to {@link PlatformPlateFull} except for the box it describes
 * ({@code setInfo(12, 1, 0.5, 0.5)}) and the registry id; the two are kept as separate classes
 * because 1.12.2 kept them separate and their block entity classes stay distinguishable. Everything
 * else -- the receiver-driven {@code isEnabled} logic, the beam push, the redraw-on-change -- is the
 * 1.12.2 {@code updateSignal} verbatim.</p>
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
 *   <li>The block entity's render bounds are not ported: 1.12.2 inherited {@code shouldRenderInPass}
 *       and {@code getRenderBoundingBox} from {@link TileEntityPlatformPlate}, and 1.21.1 removed
 *       both from {@code BlockEntity} (javap-verified). They move to the renderer.</li>
 *   <li><b>Script interaction (connected):</b> 1.12.2's {@code onBlockActivated} ran an
 *       {@code util.PlatformPlateCore} program (NGT tablet) to set the panel text; the core is ported,
 *       so the hook is {@link #useItemOn} now -- no optics network wrapper is needed any more, because
 *       the ported {@code NSASM} base no longer has {@code getWrapper()}.</li>
 * </ul>
 *
 * <p>Registration: 1.12.2 used {@code Material.GLASS} with light level 0 and the fixed id
 * {@code platform_plate_half}; the block and its block entity type are registered by
 * {@code OpticsBlocks} / {@code OpticsBlockEntities}, the type naming {@link #registeredBlocks()}.</p>
 */
public class PlatformPlateHalf extends DeviceBase implements ILightSource {

    public static final int ALIGN_CENTER = 0, ALIGN_LEFT = 1, ALIGN_RIGHT = 2;

    /**
     * Blocks this class backs; filled by {@code OpticsBlocks} with deferred holders so the block
     * entity type can name them (same shape as {@code RGBLight}).
     */
    private static final List<DeferredBlock<PlatformPlateHalf>> REGISTERED = new ArrayList<>();

    public static Block[] registeredBlocks() {
        return REGISTERED.stream().map(DeferredBlock::get).toArray(Block[]::new);
    }

    public static void noteRegistered(DeferredBlock<PlatformPlateHalf> block) {
        REGISTERED.add(block);
    }

    public static class TileEntityPlatformPlateHalf extends TileEntityPlatformPlate {

        public TileEntityPlatformPlateHalf(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
            setInfo(12, 1, 0.5, 0.5);
        }

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof TileEntityPlatformPlateHalf light)) {
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
    public PlatformPlateHalf(BlockBehaviour.Properties properties,
                             Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityPlatformPlateHalf(type, pos, state);
    }

    /** 1.12.2 {@code breakBlock}: detach the receiver link before the block goes away. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof TileEntityReceiver receiver) {
            receiver.onDestroy();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * 1.12.2 {@code onBlockActivated}, carried over: the program read off the NGT tablet is run against
     * this panel through a {@code PlatformPlateCore} anonymous subclass supplying
     * {@code getWorld/getX/getY/getZ/getPlayer/getTile} (no {@code getWrapper()} any more). The
     * server-side guard, the main-hand stack and the early no-NGT-list exit are the original ones, and
     * the boolean result maps as {@code SUCCESS} / {@code PASS_TO_DEFAULT_BLOCK_INTERACTION}.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityPlatformPlateHalf) {
            TileEntityPlatformPlateHalf text = (TileEntityPlatformPlateHalf) blockEntity;
            if (!level.isClientSide) {
                ItemStack stack = player.getMainHandItem();
                if (!stack.isEmpty()) {

                    ListTag list = Util.getTagListFromNGT(stack);
                    if (list == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    String[][] code = NSASM.getCode(list);
                    new PlatformPlateCore(code) {
                        @Override
                        public Level getWorld() {
                            return level;
                        }

                        @Override
                        public double getX() {
                            return pos.getX();
                        }

                        @Override
                        public double getY() {
                            return pos.getY();
                        }

                        @Override
                        public double getZ() {
                            return pos.getZ();
                        }

                        @Override
                        public Player getPlayer() {
                            return player;
                        }

                        @Override
                        public TileEntityPlatformPlate getTile() {
                            return text;
                        }
                    }.run();

                }
            }
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
