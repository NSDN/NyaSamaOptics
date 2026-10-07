package club.nsdn.nyasamaoptics.tileblock.deco;

import club.nsdn.nyasamaoptics.NyaSamaOptics;
import club.nsdn.nyasamaoptics.OpticsBlocks;
import club.nsdn.nyasamaoptics.api.ILightSource;
import club.nsdn.nyasamaoptics.api.LightBeam;
import club.nsdn.nyasamaoptics.util.DecoTextCore;
import club.nsdn.nyasamaoptics.util.font.FontLoader;
import club.nsdn.nyasamatelecom.api.device.DeviceBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * Station board, ported from the 1.12.2 {@code StationBoard}.
 *
 * <p>The richest of the deco boards: on top of the two-line text of {@link TileEntityDecoText} it keeps
 * a second, independently addressable set of label/colour/scale/offset/state values (the {@code 1} and
 * {@code 2} sides), all persisted.</p>
 *
 * <p>Mapping notes:</p>
 * <ul>
 *   <li>{@code NBTTagCompound.setString/setInteger/setDouble/setBoolean} become
 *       {@code putString/putInt/putDouble/putBoolean}; the tag keys are unchanged, so existing saves
 *       keep their meaning.</li>
 *   <li>{@code onBlockPlacedBy} used the yaw alone ({@code MathHelper.floor(rotationYaw * 4.0F / 360.0F
 *       + 0.5D) & 3}) via {@code setDeviceMeta}. {@link DeviceBase#setPlacedBy} adds the pitch cases
 *       (the {@code +4} / {@code +8} orientations) that this board must not have -- it is a wall
 *       board -- so the override below reproduces the 1.12.2 arithmetic with {@link Mth#floor}.</li>
 *   <li>{@code BlockLoader.light.lightCtl(world, pos, isEnabled)} becomes
 *       {@link LightBeam#lightCtl(Level, BlockPos, LightBeam, boolean)} against
 *       {@link OpticsBlocks#dotLight()} (the {@code TYPE_DOT} beam).</li>
 *   <li>{@code breakBlock} + {@code onDestroy()} becomes
 *       {@link #onRemove(BlockState, Level, BlockPos, BlockState, boolean)}.</li>
 *   <li>{@code getMaxRenderDistanceSquared()} (4096.0) and {@code getRenderBoundingBox()} (a 5-block
 *       radius) move to the renderer; the numbers are recorded here for the renderer port to reuse
 *       verbatim.</li>
 *   <li>{@code Material.GLASS}/light level 0 become the {@code Properties} supplied by the registration
 *       table, and the 1.12.2 {@code "StationBoard"} name argument is dropped (the registry entry names
 *       the block).</li>
 * </ul>
 *
 * <p><b>Script interaction (connected, including this board's private functions):</b> 1.12.2's
 * {@code onBlockActivated} ran a {@code DecoTextCore} program read off the held NGT tablet, and
 * overrode {@code loadFunc(LinkedHashMap<String, Operator>)} to add five functions -- {@code label},
 * {@code color}, {@code scale}, {@code shift} and {@code state} -- each taking a {@code 0}/{@code 1}
 * side index and clamping it, then calling {@code refresh()}. Both halves are back: the hook is
 * {@link #useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)}
 * and the function table lives in the anonymous {@code DecoTextCore} there. The operand checks
 * ({@code src.type != RegType.STR} etc.), the {@code index < 0 ? 0 : (index > 1 ? 1 : index)} clamp and
 * the {@code refresh()} calls are reproduced exactly, including the original's unmasked
 * {@code (int) src.data} for the colour (no {@code & 0xFFFFFF} was applied here in 1.12.2 either).</p>
 *
 * <p><b>Still deferred:</b> the render bounds noted above (4096.0 view distance squared, a 5-block
 * render box) and the text drawing itself belong to the block entity renderer, ported separately.</p>
 */
public class StationBoard extends DeviceBase implements ILightSource {

    public static final MapCodec<StationBoard> CODEC = simpleCodec(StationBoard::new);

    /**
     * Block entity type of this device, owned here for the same reason as on {@code AdBoard}: the type
     * must name its blocks, and the registration pass adds this register with one line
     * ({@code StationBoard.TypeHolder.TYPES.register(modBus)}), exactly as it does for
     * {@code OpticsBlockEntities.BLOCK_ENTITIES}. The type is resolved lazily inside the supplier, so
     * it is only read once a block entity is actually created -- long after registration.
     */
    private static Supplier<BlockEntityType<?>> TYPE_SUPPLIER = () -> null;

    /** Block entity type holder of this device; see {@link #TYPE_SUPPLIER}. */
    public static final class TypeHolder {

        public static final DeferredRegister<BlockEntityType<?>> TYPES =
                DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NyaSamaOptics.MODID);

        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> HOLDER =
                TYPES.register("station_board", () -> BlockEntityType.Builder
                        .of((pos, state) -> new StationBoard.TileEntityStationBoard(TYPE_SUPPLIER.get(), pos, state),
                                StationBoard.registeredBlocks())
                        .build(null));

        static {
            TYPE_SUPPLIER = HOLDER;
        }

        private TypeHolder() {
        }

        /** The registered type once registration has run. */
        public static BlockEntityType<?> get() {
            return HOLDER.get();
        }
    }

    /** The blocks this class backs; filled by the registration pass (there is exactly one station board). */
    private static final java.util.List<Block> REGISTERED = new java.util.ArrayList<>();

    public static Block[] registeredBlocks() {
        return REGISTERED.toArray(Block[]::new);
    }

    /** Called by the registration pass with each block it registers. */
    public static void noteRegistered(Block block) {
        REGISTERED.add(block);
    }

    /** Station board block entity: the text fields plus the two independently addressable sides. */
    public static class TileEntityStationBoard extends TileEntityDecoText {

        public String label1 = "1";
        public String label2 = "2";
        public int color1 = 0x323232;
        public int color2 = 0x323232;
        public double scale1 = 1;
        public double scale2 = 1;
        public double offset1 = 0;
        public double offset2 = 0;
        public boolean state1 = false;
        public boolean state2 = false;

        public TileEntityStationBoard(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
            align = FontLoader.ALIGN_CENTER;
            setInfo(4, 1, 1, 0.25);

            content = "\u5e7b";
            color = 0x161616;
            scale = 0.3;
            font = FontLoader.FONT_LONG;

            subContent = "genso";
            subColor = 0x323232;
            subScale = 1.0;
            subOffsetZ = -0.3;

            optionalBack = 0xFAFAFA;
        }

        @Override
        public void fromNBT(CompoundTag tagCompound) {
            super.fromNBT(tagCompound);

            label1 = tagCompound.getString("label1");
            label2 = tagCompound.getString("label2");
            color1 = tagCompound.getInt("color1");
            color2 = tagCompound.getInt("color2");
            scale1 = tagCompound.getDouble("scale1");
            scale2 = tagCompound.getDouble("scale2");
            offset1 = tagCompound.getDouble("offset1");
            offset2 = tagCompound.getDouble("offset2");
            state1 = tagCompound.getBoolean("state1");
            state2 = tagCompound.getBoolean("state2");
        }

        @Override
        public CompoundTag toNBT(CompoundTag tagCompound) {
            tagCompound.putString("label1", label1);
            tagCompound.putString("label2", label2);
            tagCompound.putInt("color1", color1);
            tagCompound.putInt("color2", color2);
            tagCompound.putDouble("scale1", scale1);
            tagCompound.putDouble("scale2", scale2);
            tagCompound.putDouble("offset1", offset1);
            tagCompound.putDouble("offset2", offset2);
            tagCompound.putBoolean("state1", state1);
            tagCompound.putBoolean("state2", state2);

            return super.toNBT(tagCompound);
        }

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) {
                return;
            }
            if (blockEntity instanceof TileEntityStationBoard board) {
                if (board.getSender() != null) {
                    board.isEnabled = board.senderIsPowered();
                } else {
                    board.isEnabled = true;
                }

                LightBeam dotLight = OpticsBlocks.dotLight();
                if (dotLight != null) {
                    LightBeam.lightCtl(level, pos, dotLight, board.isEnabled);
                }

                if (board.isEnabled != board.prevIsEnabled) {
                    board.prevIsEnabled = board.isEnabled;
                    board.refresh();
                }
            }
        }
    }

    public StationBoard(BlockBehaviour.Properties properties) {
        super(properties, TypeHolder::get);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityStationBoard(type, pos, state);
    }

    /**
     * The 1.12.2 orientation rule, verbatim but for the wall-board case: yaw only, no pitch terms.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        int val = Mth.floor((double) (placer.getYRot() * 4.0F / 360.0F) + 0.5D) & 3;
        setDeviceMeta(level, pos, val);
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
     * 1.12.2 {@code onBlockActivated}, carried over, together with the anonymous core's
     * {@code loadFunc} override that adds this board's five side-indexed functions. Everything the old
     * hook did is preserved: server-side only, main-hand stack, early exit when that stack carries no
     * NGT list, and the boolean result mapped to {@code SUCCESS} /
     * {@code PASS_TO_DEFAULT_BLOCK_INTERACTION}.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityStationBoard) {
            TileEntityStationBoard text = (TileEntityStationBoard) blockEntity;
            if (!level.isClientSide) {
                ItemStack stack = player.getMainHandItem();
                if (!stack.isEmpty()) {

                    ListTag list = Util.getTagListFromNGT(stack);
                    if (list == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    String[][] code = NSASM.getCode(list);
                    new DecoTextCore(code) {
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
                        public TileEntityStationBoard getTile() {
                            return text;
                        }

                        @Override
                        public void loadFunc(LinkedHashMap<String, Operator> funcList) {
                            super.loadFunc(funcList);
                            funcList.put("label", ((dst, src) -> {
                                if (src == null) return Result.ERR;
                                if (dst == null) return Result.ERR;
                                if (src.type != RegType.STR) return Result.ERR;
                                if (dst.type != RegType.INT) return Result.ERR;

                                int index = (int) dst.data;
                                index = index < 0 ? 0 : (index > 1 ? 1 : index);
                                if (index == 0)
                                    getTile().label1 = (String) src.data;
                                else
                                    getTile().label2 = (String) src.data;

                                getTile().refresh();

                                return Result.OK;
                            }));
                            funcList.put("color", ((dst, src) -> {
                                if (src == null) return Result.ERR;
                                if (dst == null) return Result.ERR;
                                if (src.type != RegType.INT) return Result.ERR;
                                if (dst.type != RegType.INT) return Result.ERR;

                                int index = (int) dst.data;
                                index = index < 0 ? 0 : (index > 1 ? 1 : index);
                                if (index == 0)
                                    getTile().color1 = (int) src.data;
                                else
                                    getTile().color2 = (int) src.data;

                                getTile().refresh();

                                return Result.OK;
                            }));
                            funcList.put("scale", ((dst, src) -> {
                                if (src == null) return Result.ERR;
                                if (dst == null) return Result.ERR;
                                if (src.type != RegType.FLOAT && src.type != RegType.INT) return Result.ERR;
                                if (dst.type != RegType.INT) return Result.ERR;

                                int index = (int) dst.data;
                                index = index < 0 ? 0 : (index > 1 ? 1 : index);
                                if (index == 0)
                                    getTile().scale1 = src.type == RegType.FLOAT ? (float) src.data : (int) src.data;
                                else
                                    getTile().scale2 = src.type == RegType.FLOAT ? (float) src.data : (int) src.data;

                                getTile().refresh();

                                return Result.OK;
                            }));
                            funcList.put("shift", ((dst, src) -> {
                                if (src == null) return Result.ERR;
                                if (dst == null) return Result.ERR;
                                if (src.type != RegType.FLOAT && src.type != RegType.INT) return Result.ERR;
                                if (dst.type != RegType.INT) return Result.ERR;

                                int index = (int) dst.data;
                                index = index < 0 ? 0 : (index > 1 ? 1 : index);
                                if (index == 0)
                                    getTile().offset1 = src.type == RegType.FLOAT ? (float) src.data : (int) src.data;
                                else
                                    getTile().offset2 = src.type == RegType.FLOAT ? (float) src.data : (int) src.data;

                                getTile().refresh();

                                return Result.OK;
                            }));
                            funcList.put("state", ((dst, src) -> {
                                if (src == null) return Result.ERR;
                                if (dst == null) return Result.ERR;
                                if (src.type != RegType.INT) return Result.ERR;
                                if (dst.type != RegType.INT) return Result.ERR;

                                int index = (int) dst.data;
                                index = index < 0 ? 0 : (index > 1 ? 1 : index);
                                if (index == 0)
                                    getTile().state1 = (((int) src.data) != 0);
                                else
                                    getTile().state2 = (((int) src.data) != 0);

                                getTile().refresh();

                                return Result.OK;
                            }));
                        }
                    }.run();

                }
            }
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /** See {@code AdBoard#registerCodec} for why the block classes hand the codec to the shared table. */
    public static DeferredHolder<MapCodec<? extends Block>, MapCodec<? extends Block>> registerCodec(
            DeferredRegister<MapCodec<? extends Block>> blockTypes) {
        return blockTypes.register("station_board_type", () -> CODEC);
    }
}
