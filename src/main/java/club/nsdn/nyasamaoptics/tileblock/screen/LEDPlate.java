package club.nsdn.nyasamaoptics.tileblock.screen;

import club.nsdn.nyasamaoptics.OpticsBlocks;
import club.nsdn.nyasamaoptics.api.ILightSource;
import club.nsdn.nyasamatelecom.api.device.DeviceBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

/**
 * LED text plate, ported from the 1.12.2 {@code LEDPlate} (registry id {@code led_plate}, the
 * {@code "LEDPlate"} constructor argument was only the unlocalised name).
 *
 * <p>The device is a receiver-driven scrolling text panel. Its block entity keeps the 1.12.2 state
 * verbatim: {@code content}, {@code color}, {@code back}, {@code scale}, {@code align}, the
 * {@code inSpecial}/{@code specialBuffer} pair used by the special-command hooks, and the
 * {@code isEnabled}/{@code prevIsEnabled} pair driven by the linked sender. {@link #registerCommand}
 * and the {@code CMD_LIST} table are carried over unchanged: they let other code register a command
 * that produces the panel's buffer, and {@code updateSignal} runs it whenever the panel's first line
 * names a registered command.</p>
 *
 * <p>Mapping notes:</p>
 * <ul>
 *   <li>{@code DeviceBase} is the ported telecom base, so this device gets the modern
 *       {@code newBlockEntity}/{@code getTicker} pair and the 12-case {@code META} orientation
 *       handling; {@code createNewTileEntity} becomes {@link #createBlockEntity}.</li>
 *   <li>{@code breakBlock} becomes {@link #onRemove}, which tells the receiver link to detach
 *       ({@code onDestroy}) exactly as 1.12.2 did.</li>
 *   <li>1.12.2's {@code BlockLoader.light} (the {@code TYPE_DOT} beam) is now
 *       {@code OpticsBlocks.dotLight()}, resolved lazily with a null guard because block
 *       registration may not have completed when a block entity first ticks.</li>
 *   <li>{@code shouldRenderInPass} and {@code getRenderBoundingBox} on the block entity are not
 *       ported: 1.21.1 removed both hooks from {@code BlockEntity} (verified with javap) and moved
 *       render bounds / view distance onto the renderer, which is where the optics renderers supply
 *       them.</li>
 *   <li><b>Deferred:</b> 1.12.2's {@code onBlockActivated} ran an {@code util.LEDPlateCore} program
 *       read off an NGT tablet to set content/colour/back/scale/align. {@code LEDPlateCore} and the
 *       optics network wrapper are not ported yet, so the hook is left out rather than silently
 *       dropped; see the note at the bottom of this class. {@code TileEntityLEDPlate.updateThis} is
 *       already in place for that core to call.</li>
 * </ul>
 *
 * <p>Registration: 1.12.2 used {@code setLightLevel(0)} and a fixed registry name; the block is now
 * registered by {@code OpticsBlocks} with a properties object carrying light level 0, and the block
 * entity type is registered in {@code OpticsBlockEntities} naming
 * {@link #registeredBlocks()} -- the same shape as {@code RGBLight}.</p>
 */
public class LEDPlate extends DeviceBase implements ILightSource {

    public static final int ALIGN_CENTER = 0, ALIGN_LEFT = 1, ALIGN_RIGHT = 2;

    /**
     * Blocks this class backs; filled by {@code OpticsBlocks} with deferred holders so the block
     * entity type can name them (see {@code RGBLight} for why holders and not blocks).
     */
    private static final List<DeferredBlock<LEDPlate>> REGISTERED = new ArrayList<>();

    public static Block[] registeredBlocks() {
        return REGISTERED.stream().map(DeferredBlock::get).toArray(Block[]::new);
    }

    public static void noteRegistered(DeferredBlock<LEDPlate> block) {
        REGISTERED.add(block);
    }

    @FunctionalInterface
    public interface ISpecialCommand {
        String runCmd(Level level, BlockPos pos, String buffer, String[] args);
    }

    public static void registerCommand(String cmd, ISpecialCommand command) {
        TileEntityLEDPlate.CMD_LIST.put(cmd, command);
    }

    public static class TileEntityLEDPlate extends TileEntityReceiver {

        public String content;
        public int color;
        public int back;
        public double scale;
        public int align;

        public boolean prevInSpecial;
        public boolean inSpecial;
        public String specialBuffer = "-= NSO-LED =-";
        static LinkedHashMap<String, ISpecialCommand> CMD_LIST = new LinkedHashMap<>();

        public boolean isEnabled;
        public boolean prevIsEnabled;

        public TileEntityLEDPlate(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
            content = "DUMMY";
            color = 0xEE1111;
            back = 0x000000;
            scale = 1.0;
            align = ALIGN_CENTER;
            setInfo(12, 1, 0.125, 1);
        }

        /* NOT PORTED: shouldRenderInPass(int) / getRenderBoundingBox() -- 1.21.1 BlockEntity has
         * neither (javap-verified); the renderer supplies the bounds instead. */

        @Override
        public CompoundTag toNBT(CompoundTag tagCompound) {
            tagCompound.putString("content", content);
            tagCompound.putInt("color", color);
            tagCompound.putInt("back", back);
            tagCompound.putDouble("scale", scale);
            tagCompound.putInt("align", align);
            tagCompound.putBoolean("isEnabled", isEnabled);

            tagCompound.putBoolean("inSpecial", inSpecial);
            tagCompound.putString("specialBuffer", specialBuffer);

            return super.toNBT(tagCompound);
        }

        @Override
        public void fromNBT(CompoundTag tagCompound) {
            super.fromNBT(tagCompound);

            content = tagCompound.getString("content");
            color = tagCompound.getInt("color");
            back = tagCompound.getInt("back");
            scale = tagCompound.getDouble("scale");
            align = tagCompound.getInt("align");
            isEnabled = tagCompound.getBoolean("isEnabled");

            inSpecial = tagCompound.getBoolean("inSpecial");
            specialBuffer = tagCompound.getString("specialBuffer");
        }

        public static void updateThis(TileEntityLEDPlate tileEntityLEDPlate) {
            tileEntityLEDPlate.refresh();
        }

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof TileEntityLEDPlate light)) {
                return;
            }

            if (light.getSender() != null) {
                light.isEnabled = light.senderIsPowered();
            } else {
                light.isEnabled = true;
            }

            String cmd = light.content.split("\n")[0];
            light.prevInSpecial = light.inSpecial;
            light.inSpecial = CMD_LIST.containsKey(cmd);
            if (light.isEnabled && light.inSpecial) {
                light.specialBuffer = CMD_LIST.get(cmd).runCmd(
                        level, pos, light.specialBuffer, light.content.split("\n")
                );
                light.refresh();
            } else {
                light.specialBuffer = "-= NSO-LED =-";
                if (light.inSpecial != light.prevInSpecial) {
                    light.refresh();
                }
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
    public LEDPlate(BlockBehaviour.Properties properties,
                    Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityLEDPlate(type, pos, state);
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
     * NOT PORTED HERE: 1.12.2's onBlockActivated ran an LEDPlateCore program (read off an NGT tablet
     * through Util.getTagListFromNGT + NSASM.getCode) to set the panel's content, colour, back colour,
     * scale and alignment. LEDPlateCore lives in the optics util package and needs the optics network
     * wrapper, neither of which is ported yet, so the hook is deferred rather than dropped. In 1.21.1
     * the hook to add is useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand,
     * BlockHitResult) returning ItemInteractionResult (not InteractionResult); 1.12.2 returned true
     * when the block entity was a TileEntityLEDPlate, so the modern version should return
     * ItemInteractionResult.SUCCESS in that case and PASS_TO_DEFAULT_BLOCK_INTERACTION otherwise.
     */
}
