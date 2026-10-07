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
import net.minecraft.nbt.ListTag;
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

import java.util.function.Supplier;

/**
 * Ad board (the "genso" sign), ported from the 1.12.2 {@code AdBoard}.
 *
 * <p>A two-line decorative text plate that is switched on by its linked transmitter. Follows
 * {@code RGBLight} as the device template: the block extends {@link DeviceBase} and nests its own
 * {@link TileEntityReceiver} subclass.</p>
 *
 * <p>Mapping notes:</p>
 * <ul>
 *   <li>{@code Material.GLASS} with light level 0 becomes {@code BlockBehaviour.Properties} passed in
 *       by the registration table (map colour, {@code SoundType.GLASS}, {@code noOcclusion()}). The old
 *       {@code setLightLevel(0)} means the properties need no {@code lightLevel(...)}.</li>
 *   <li>The 1.12.2 constructor argument {@code "AdBoard"} was only the unlocalised name; in 1.21.1 the
 *       name comes from the registry entry, so {@code super(properties, blockEntityType)} is all that
 *       is left (the same collapse {@code DeviceBase} documents).</li>
 *   <li>{@code breakBlock} + {@code TileEntityReceiver.onDestroy()} becomes
 *       {@link #onRemove(BlockState, Level, BlockPos, BlockState, boolean)}, exactly as in
 *       {@code RGBLight}.</li>
 *   <li>{@code BlockLoader.light.lightCtl(world, pos, isEnabled)} becomes
 *       {@link LightBeam#lightCtl(Level, BlockPos, LightBeam, boolean)} against
 *       {@link OpticsBlocks#dotLight()} (the {@code TYPE_DOT} beam).</li>
 *   <li>{@code getMaxRenderDistanceSquared()} (1024.0) and {@code getRenderBoundingBox()} (a 3-block
 *       radius) are not ported: 1.21.1's {@code BlockEntity} has neither method -- verified with javap
 *       against neoforge-21.1.256-merged.jar. Both move to the renderer
 *       ({@code BlockEntityRenderer#getViewDistance} / {@code shouldRender}); the numbers are recorded
 *       here so the renderer port can reuse them <b>verbatim</b>: view distance squared 1024.0,
 *       render box radius 3.</li>
 *   <li>Font/alignment defaults now reference the ported font utility directly:
 *       {@code FontLoader.ALIGN_CENTER} and {@code FontLoader.FONT_LONG} take the place of the
 *       literals this class carried while {@code util.font} was still missing.</li>
 * </ul>
 *
 * <p><b>Script interaction (connected):</b> 1.12.2's {@code onBlockActivated} read an NSASM program off
 * the held NGT tablet and ran a {@code DecoTextCore} against this block entity to rewrite its text.
 * That is {@link #useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand,
 * BlockHitResult)} now, returning {@code ItemInteractionResult} (not {@code InteractionResult}): the
 * 1.12.2 {@code true} -- the block entity matched -- maps to {@code SUCCESS}, and the {@code false}
 * cases (wrong block entity, or a held stack with no NGT list) map to
 * {@code PASS_TO_DEFAULT_BLOCK_INTERACTION}.</p>
 *
 * <p><b>Still deferred (renderer):</b> the render bounds recorded above
 * ({@code getMaxRenderDistanceSquared} 1024.0, a 3-block render box) and the drawing of the text model
 * itself belong to the block entity renderer, which is ported separately.</p>
 */
public class AdBoard extends DeviceBase implements ILightSource {

    public static final MapCodec<AdBoard> CODEC = simpleCodec(AdBoard::new);

    /**
     * Block entity type of this device. It lives here, next to the block and the block entity it
     * belongs to, rather than only in the shared table: 1.21.1 requires the type to name the blocks it
     * belongs to in {@code BlockEntityType.Builder.of(...)}, so the device class owns that half and the
     * registration pass adds its register with one line
     * ({@code AdBoard.TypeHolder.TYPES.register(modBus)}), exactly as it does for
     * {@code OpticsBlockEntities.BLOCK_ENTITIES}.
     *
     * <p>The type is resolved lazily inside the supplier -- {@code TYPE_SUPPLIER.get()} runs when a
     * block entity is actually created, long after registration -- which is the pattern
     * {@code OpticsBlockEntities#rgbLightType()} documents. The static block of {@link TypeHolder} fills
     * the field with the very holder the type is registered through, so the two cannot drift apart.</p>
     */
    private static Supplier<BlockEntityType<?>> TYPE_SUPPLIER = () -> null;

    /** Block entity type holder of this device; see {@link #TYPE_SUPPLIER}. */
    public static final class TypeHolder {

        public static final DeferredRegister<BlockEntityType<?>> TYPES =
                DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NyaSamaOptics.MODID);

        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> HOLDER =
                TYPES.register("ad_board", () -> BlockEntityType.Builder
                        .of((pos, state) -> new AdBoard.TileEntityAdBoard(TYPE_SUPPLIER.get(), pos, state),
                                AdBoard.registeredBlocks())
                        .build(null));

        static {
            TYPE_SUPPLIER = HOLDER;
        }

        private TypeHolder() {
        }

        /** The registered type once registration has run; mirrors {@code OpticsBlocks.rgbLightType()}. */
        public static BlockEntityType<?> get() {
            return HOLDER.get();
        }
    }

    /**
     * The blocks this class backs, handed to the block entity type as its {@code validBlocks}. 1.12.2
     * had exactly one ad board, but the list is filled by the registration pass the same way
     * {@code RGBLight.noteRegistered} fills its ten, so a second variant would need no change here.
     */
    private static final java.util.List<Block> REGISTERED = new java.util.ArrayList<>();

    public static Block[] registeredBlocks() {
        return REGISTERED.toArray(Block[]::new);
    }

    /** Called by the registration pass with the block it just registered. */
    public static void noteRegistered(Block block) {
        REGISTERED.add(block);
    }

    /** Block entity: reads its linked sender and pushes the enabled flag to the dot beam. */
    public static class TileEntityAdBoard extends TileEntityDecoText {

        public TileEntityAdBoard(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
            align = FontLoader.ALIGN_CENTER;
            setInfo(12, 1, 0.125, 1);

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
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) {
                return;
            }
            if (blockEntity instanceof TileEntityAdBoard board) {
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

    public AdBoard(BlockBehaviour.Properties properties) {
        super(properties, TypeHolder::get);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityAdBoard(type, pos, state);
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
     * The 1.12.2 board never set its orientation metadata -- it draws the same plate on all four sides
     * of the block -- so the inherited orientation from {@link DeviceBase#setPlacedBy} is overridden
     * back to {@code 0}. Without this the board would be rotated by {@code META} in the renderer, which
     * would be a visible behaviour change rather than a faithful port.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        setDeviceMeta(level, pos, 0);
    }

    /**
     * 1.12.2 {@code onBlockActivated}, carried over. When the clicked block holds this board's block
     * entity, the program on the held NGT tablet is run against it through a {@code DecoTextCore}
     * anonymous subclass -- {@code getWorld/getX/getY/getZ/getPlayer/getTile} are the hooks the ported
     * core asks for (the 1.12.2 {@code getWrapper()} no longer exists).
     *
     * <p>Both 1.12.2 exits are kept: running off the main-hand stack only, on the logical server
     * ({@code !level.isClientSide}), and returning early when that stack carries no NGT list. The
     * boolean outcome maps one-to-one -- {@code true} (a matching block entity) is now
     * {@code SUCCESS}, every {@code false} is {@code PASS_TO_DEFAULT_BLOCK_INTERACTION}.</p>
     *
     * <p>The stack is taken from {@code player.getMainHandItem()} exactly as the old
     * {@code getHeldItemMainhand()} did, so an off-hand activation still reads the main hand. That is
     * why the {@code itemStack} parameter of this hook is deliberately not the one used.</p>
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityAdBoard) {
            TileEntityAdBoard text = (TileEntityAdBoard) blockEntity;
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
                        public TileEntityAdBoard getTile() {
                            return text;
                        }
                    }.run();

                }
            }
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * Registers this block's {@code MapCodec}. 1.20.5+ requires every custom block to have one and
     * {@link #codec()} must return it. Taking the shared register as an argument keeps
     * {@code OpticsBlocks} in charge of the block-type table, mirroring how
     * {@code RGBLight.noteRegistered} keeps it in charge of the block table:
     * {@code AdBoard.registerCodec(OpticsBlocks.BLOCK_TYPES)}. The {@code _type} suffix keeps the key
     * clear of the block entity type that {@link TypeHolder} registers under {@code ad_board}.
     */
    public static DeferredHolder<MapCodec<? extends Block>, MapCodec<? extends Block>> registerCodec(
            DeferredRegister<MapCodec<? extends Block>> blockTypes) {
        return blockTypes.register("ad_board_type", () -> CODEC);
    }
}
