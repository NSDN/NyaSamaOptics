package club.nsdn.nyasamaoptics.tileblock.light;

import club.nsdn.nyasamaoptics.OpticsBlocks;
import club.nsdn.nyasamaoptics.api.ILightSource;
import club.nsdn.nyasamaoptics.util.RGBLightCore;
import club.nsdn.nyasamatelecom.api.device.DeviceBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * RGB light device, ported from the 1.12.2 {@code RGBLight}. Ten variants exist (pillar body, the
 * adsorption/fluorescent light shapes, mosaic lights, platform lights, cuball); they differ only in
 * box size, whether they are flood lights, and their registry names -- so one class serves all ten,
 * which is why {@link #registeredBlocks()} is filled by the registration code.</p>
 *
 * <p>The block entity ({@code TileEntityRGBLight}) reads its linked sender and pushes either a six-way
 * ({@code TYPE_DOT}) or a directional ({@code TYPE_LINE}) beam accordingly. Its {@code 12}-case Meta,
 * colour and enabled flag are preserved.</p>
 *
 * <p>Mapping notes:</p>
 * <ul>
 *   <li>{@code DeviceBase} is the ported telecom base, so this device automatically gets the modern
 *       {@code newBlockEntity}/{@code getTicker} pair and {@code noOcclusion()} behaviour.</li>
 *   <li>{@code shouldRenderInPass} and {@code getRenderBoundingBox} on the block entity are not
 *       ported: 1.21.1 removed per-face/render-pass hooks from {@code BlockEntity} and moved render
 *       bounds onto the renderer (verified with javap), which is where the optics renderers will
 *       supply them.</li>
 *   <li><b>Deferred (D4):</b> the 1.12.2 class carried two {@code WavefrontObject} fields
 *       ({@code modelShell}/{@code modelLight}) that its renderer used to draw the lamp. The custom OBJ
 *       pipeline is being discarded in favour of baked models, so those fields are gone; the renderer
 *       port will reference the baked models produced by the OBJ pipeline instead
 *       (see {@code docs/08-OBJ-PIPELINE.md}).</li>
 *   <li><b>Script interaction (connected):</b> the 1.12.2 script interaction
 *       ({@code onBlockActivated} running an {@code RGBLightCore} program to set the colour) is now
 *       {@link #useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand,
 *       BlockHitResult)}; the core was ported with the optics {@code util} package, and the 1.12.2
 *       boolean outcome maps to {@code SUCCESS} /
 *       {@code PASS_TO_DEFAULT_BLOCK_INTERACTION}. The beam/lamp geometry that
 *       {@link #useItemOn} does not touch is still the renderer's business (see the previous
 *       bullet).</li>
 * </ul>
 */
public class RGBLight extends DeviceBase implements ILightSource {

    /**
     * Blocks this class backs; filled by {@code OpticsBlocks} with deferred holders so the block entity
     * type can name them. Holders, not blocks: a type's supplier runs at registration time, which is
     * after the blocks exist, whereas the static table that creates them runs earlier.
     */
    private static final List<net.neoforged.neoforge.registries.DeferredBlock<RGBLight>> REGISTERED =
            new ArrayList<>();

    public static Block[] registeredBlocks() {
        return REGISTERED.stream().map(net.neoforged.neoforge.registries.DeferredBlock::get).toArray(Block[]::new);
    }

    public static void noteRegistered(net.neoforged.neoforge.registries.DeferredBlock<RGBLight> block) {
        REGISTERED.add(block);
    }

    /** Block entity: tracks the sender's powered state and drives the beam system. */
    public static class TileEntityRGBLight extends TileEntityReceiver {

        public int color = 0xFFFFFF;
        public boolean isEnabled;
        public boolean prevIsEnabled;

        private final double sizeX, sizeY, sizeZ;

        public TileEntityRGBLight(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            this(type, pos, state, 1, 1, 1);
        }

        public TileEntityRGBLight(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                  double x, double y, double z) {
            super(type, pos, state);
            this.color = 0xFFFFFF;
            this.sizeX = x;
            this.sizeY = y;
            this.sizeZ = z;
            setInfo(12, x, y, z);
        }

        @Override
        public CompoundTag toNBT(CompoundTag tagCompound) {
            tagCompound.putInt("color", color);
            tagCompound.putBoolean("isEnabled", isEnabled);
            return super.toNBT(tagCompound);
        }

        @Override
        public void fromNBT(CompoundTag tagCompound) {
            super.fromNBT(tagCompound);
            color = tagCompound.getInt("color");
            isEnabled = tagCompound.getBoolean("isEnabled");
        }

        /** 1.12.2 metadata orientation: &lt;4 up, &gt;7 down, 4..7 horizontal. */
        public static Direction getDirFromMeta(int meta) {
            if (meta < 4) {
                return Direction.UP;
            } else if (meta > 7) {
                return Direction.DOWN;
            }
            switch (meta) {
                case 4:
                    return Direction.NORTH;
                case 5:
                    return Direction.EAST;
                case 6:
                    return Direction.SOUTH;
                case 7:
                    return Direction.WEST;
                default:
                    return Direction.DOWN;
            }
        }

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof TileEntityRGBLight light)) {
                return;
            }

            if (light.getSender() != null) {
                light.isEnabled = light.senderIsPowered();
            } else {
                light.isEnabled = true;
            }

            if (level.getBlockState(pos).getBlock() instanceof RGBLight rgbLight) {
                if (rgbLight.isFloodLight) {
                    if (OpticsBlocks.lineLight() != null) {
                        OpticsBlocks.lineLight().lightCtl(level, pos, getDirFromMeta(light.META), 16, light.isEnabled);
                    }
                } else if (OpticsBlocks.dotLight() != null) {
                    OpticsBlocks.dotLight().lightCtl(level, pos, light.isEnabled);
                }
            }

            if (light.isEnabled != light.prevIsEnabled) {
                light.prevIsEnabled = light.isEnabled;
                light.refresh();
            }
        }
    }

    public final boolean isFloodLight;
    public final float sizeX, sizeY, sizeZ;
    public final String resource;

    public RGBLight(BlockBehaviour.Properties properties,
                    Supplier<BlockEntityType<?>> blockEntityType,
                    String resource, float x, float y, float z) {
        this(properties, blockEntityType, resource, x, y, z, false);
    }

    public RGBLight(BlockBehaviour.Properties properties,
                    Supplier<BlockEntityType<?>> blockEntityType,
                    String resource, float x, float y, float z, boolean isFloodLight) {
        super(properties, blockEntityType);
        this.isFloodLight = isFloodLight;
        this.sizeX = x;
        this.sizeY = y;
        this.sizeZ = z;
        this.resource = resource;
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityRGBLight(type, pos, state, sizeX, sizeY, sizeZ);
    }

    /**
     * Kept for parity with 1.12.2, which cleared the receiver link when the device was broken.
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof TileEntityReceiver receiver) {
            receiver.onDestroy();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * 1.12.2 {@code onBlockActivated}, carried over: the NGT tablet's program is run against this
     * light's block entity through an {@code RGBLightCore} anonymous subclass supplying
     * {@code getWorld/getX/getY/getZ/getPlayer/getLight} (no {@code getWrapper()} any more, and the
     * core asks for {@code getLight()} rather than {@code getTile()}). The server-side guard, the
     * main-hand stack and the early no-NGT-list exit are the original ones, and the boolean result maps
     * as {@code SUCCESS} / {@code PASS_TO_DEFAULT_BLOCK_INTERACTION}.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityRGBLight) {
            TileEntityRGBLight light = (TileEntityRGBLight) blockEntity;
            if (!level.isClientSide) {
                ItemStack stack = player.getMainHandItem();
                if (!stack.isEmpty()) {

                    ListTag list = Util.getTagListFromNGT(stack);
                    if (list == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    String[][] code = NSASM.getCode(list);
                    new RGBLightCore(code) {
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
                        public TileEntityRGBLight getLight() {
                            return light;
                        }
                    }.run();

                }
            }
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /*
     * The shared RGB light block entity type is registered in OpticsBlockEntities once the
     * BlockEntitySupplier shape is pinned down (see the note there); the ten RGB light blocks are
     * registered at the same time, since a type must name the blocks it belongs to.
     */
}
