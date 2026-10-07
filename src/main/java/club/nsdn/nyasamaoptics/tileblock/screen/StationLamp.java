package club.nsdn.nyasamaoptics.tileblock.screen;

import club.nsdn.nyasamaoptics.OpticsBlocks;
import club.nsdn.nyasamaoptics.api.ILightSource;
import club.nsdn.nyasamatelecom.api.device.DeviceBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Station lamp (the hanging name-board lamp), ported from the 1.12.2 {@code StationLamp} (registry id
 * {@code station_lamp}).
 *
 * <p>The block entity keeps its 1.12.2 shape verbatim: a {@link TileEntityStationLamp.LampInfo} list
 * (one entry per printed line, each with content / foreground colour / background colour / scale),
 * the {@code logo} resource path, and the {@code isEnabled}/{@code prevIsEnabled} pair driven by the
 * linked sender. NBT persistence keeps the 1.12.2 layout too -- one {@code LampInfo} compound per
 * index named {@code lampInfo0}, {@code lampInfo1}, ... plus {@code logo} and {@code isEnabled} --
 * so old saves keep loading.</p>
 *
 * <p>Mapping notes:</p>
 * <ul>
 *   <li>{@code DeviceBase} supplies the modern {@code newBlockEntity}/{@code getTicker} pair;
 *       {@code createNewTileEntity} becomes {@link #createBlockEntity}.</li>
 *   <li>1.12.2 put the lamp on its own 4-value metadata set from the player's yaw only, deliberately
 *       skipping {@code DeviceBase}'s pitch groups; {@link #setPlacedBy} reproduces exactly that.</li>
 *   <li>{@code BlockLoader.light} (the {@code TYPE_DOT} beam) is now {@code OpticsBlocks.dotLight()},
 *       resolved lazily with a null guard (see {@code RGBLight}).</li>
 *   <li>{@code breakBlock} becomes {@link #onRemove} and detaches the receiver link via
 *       {@code onDestroy}, as in 1.12.2.</li>
 *   <li>The block entity's {@code shouldRenderInPass} / {@code getRenderBoundingBox}
 *       ({@code INFINITE_EXTENT_AABB}, because the board may stick out) are not ported: 1.21.1 removed
 *       both from {@code BlockEntity} (javap-verified) and moved them to the renderer.</li>
 *   <li><b>Not portable:</b> 1.12.2's {@code isSideSolid} returning {@code facing == EnumFacing.DOWN}
 *       has no counterpart -- {@code BlockBehaviour} in 1.21.1 has no per-face solidity hook (verified
 *       with javap); per-face sturdiness is derived from the collision shape, which
 *       {@code TileEntityBase.AABB()} already provides (the lamp's bottom face is the full 1x1
 *       footprint). Faking it with an inflated shape is deliberately avoided.</li>
 *   <li><b>Deferred:</b> 1.12.2's {@code onBlockActivated} ran an {@code util.StationLampCore} program
 *       (NGT tablet) to fill the lamp lines and the logo; that core and the optics network wrapper are
 *       not ported yet, so the hook is left out and recorded at the bottom of this class.</li>
 * </ul>
 *
 * <p>Registration: 1.12.2 used {@code Material.GLASS} with light level 0 and the fixed id
 * {@code station_lamp}; the block and its block entity type are registered by {@code OpticsBlocks} /
 * {@code OpticsBlockEntities}, the type naming {@link #registeredBlocks()}.</p>
 */
public class StationLamp extends DeviceBase implements ILightSource {

    public static final int ALIGN_CENTER = 0, ALIGN_LEFT = 1, ALIGN_RIGHT = 2;

    /**
     * Blocks this class backs; filled by {@code OpticsBlocks} with deferred holders so the block
     * entity type can name them (same shape as {@code RGBLight}).
     */
    private static final List<DeferredBlock<StationLamp>> REGISTERED = new ArrayList<>();

    public static Block[] registeredBlocks() {
        return REGISTERED.stream().map(DeferredBlock::get).toArray(Block[]::new);
    }

    public static void noteRegistered(DeferredBlock<StationLamp> block) {
        REGISTERED.add(block);
    }

    public static class TileEntityStationLamp extends TileEntityReceiver {

        public static class LampInfo {
            public String content;
            public int color;
            public int back;
            public double scale;

            public static LampInfo get(String content, int color, int back, double scale) {
                LampInfo info = new LampInfo();
                info.content = content;
                info.color = color;
                info.back = back;
                info.scale = scale;
                return info;
            }

            public static LampInfo def() {
                return get("DUMMY", 0xEE1111, 0x000000, 1.0);
            }
        }

        public LinkedList<LampInfo> lampInfo;
        public String logo;

        public boolean isEnabled;
        public boolean prevIsEnabled;

        public TileEntityStationLamp(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);

            lampInfo = new LinkedList<>();
            lampInfo.add(LampInfo.def());
            logo = "null";

            setInfo(4, 1, 1.375, 1);
        }

        /* NOT PORTED: shouldRenderInPass(int) / getRenderBoundingBox() -- 1.21.1 BlockEntity has
         * neither (javap-verified); the renderer supplies the bounds instead. */

        @Override
        public CompoundTag toNBT(CompoundTag tagCompound) {
            int i = 0;
            for (LampInfo info : lampInfo) {
                CompoundTag tag = new CompoundTag();
                tag.putString("content", info.content);
                tag.putInt("color", info.color);
                tag.putInt("back", info.back);
                tag.putDouble("scale", info.scale);
                tagCompound.put("lampInfo" + i, tag);
                i += 1;
            }
            tagCompound.putString("logo", logo);
            tagCompound.putBoolean("isEnabled", isEnabled);

            return super.toNBT(tagCompound);
        }

        @Override
        public void fromNBT(CompoundTag tagCompound) {
            super.fromNBT(tagCompound);

            if (tagCompound.contains("lampInfo0")) {
                lampInfo.clear();
                for (int i = 0; tagCompound.contains("lampInfo" + i); i++) {
                    CompoundTag tag = tagCompound.getCompound("lampInfo" + i);
                    if (!tag.contains("content")) {
                        continue;
                    }
                    String content = tag.getString("content");
                    int color = tag.getInt("color");
                    int back = tag.getInt("back");
                    double scale = tag.getDouble("scale");
                    lampInfo.add(LampInfo.get(content, color, back, scale));
                }
            }
            logo = tagCompound.getString("logo");
            isEnabled = tagCompound.getBoolean("isEnabled");
        }

        public static void updateThis(TileEntityStationLamp tileEntityStationLamp) {
            tileEntityStationLamp.refresh();
        }

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof TileEntityStationLamp lamp)) {
                return;
            }

            if (lamp.getSender() != null) {
                lamp.isEnabled = lamp.senderIsPowered();
            } else {
                lamp.isEnabled = true;
            }

            if (OpticsBlocks.dotLight() != null) {
                OpticsBlocks.dotLight().lightCtl(level, pos, lamp.isEnabled);
            }

            if (lamp.isEnabled != lamp.prevIsEnabled) {
                lamp.prevIsEnabled = lamp.isEnabled;
                lamp.refresh();
            }
        }

    }

    /**
     * @param properties      block properties; 1.12.2 used {@code Material.GLASS} with light level 0
     * @param blockEntityType the registered type, supplied lazily (see {@code DeviceBase})
     */
    public StationLamp(BlockBehaviour.Properties properties,
                       Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityStationLamp(type, pos, state);
    }

    /**
     * 1.12.2 {@code StationLamp.onBlockPlacedBy} took only the yaw-derived value 0..3 and did not call
     * {@code DeviceBase}'s pitch-aware version (which adds +4 for level placement and +8 for ceiling
     * placement), so the lamp's own four metadata values are reproduced here.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        int val = Mth.floor((double) (placer.getYRot() * 4.0F / 360.0F) + 0.5D) & 3;
        setDeviceMeta(level, pos, val);
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
     * NOT PORTED HERE: 1.12.2's onBlockActivated ran a StationLampCore program (NGT tablet) to fill the
     * lampInfo list and the logo. StationLampCore lives in the optics util package and needs the optics
     * network wrapper, neither of which is ported yet, so the hook is deferred rather than dropped. In
     * 1.21.1 the hook to add is useItemOn(ItemStack, BlockState, Level, BlockPos, Player,
     * InteractionHand, BlockHitResult) returning ItemInteractionResult; 1.12.2 returned true for a
     * matching block entity and false otherwise, i.e. SUCCESS /
     * PASS_TO_DEFAULT_BLOCK_INTERACTION.
     *
     * Also for the renderer port: the tile entity's `logo` field stays a String (verbatim from 1.12.2),
     * and the 1.12.2 renderer did `new ResourceLocation(lamp.logo)`. In 1.21.1 that becomes
     * ResourceLocation.tryParse / parse with a validity guard, because a malformed path throws.
     */
}
