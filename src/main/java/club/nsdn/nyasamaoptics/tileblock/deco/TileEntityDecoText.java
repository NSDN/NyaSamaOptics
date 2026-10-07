package club.nsdn.nyasamaoptics.tileblock.deco;

import club.nsdn.nyasamaoptics.tileblock.holo.TileEntityHoloText;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity of the decorative text boards, ported from the 1.12.2 {@code TileEntityDecoText}.
 *
 * <p>Adds the "sub line" to {@link TileEntityHoloText} (a second string with its own colour, scale,
 * alignment and offset) plus the per-axis offsets and optional background colour of the main line.
 * {@code AdBoard}, {@code GuideBoard}, {@code StationBoard} and {@code TextWall} all nest a subclass of
 * this.</p>
 *
 * <p>Mapping notes:</p>
 * <ul>
 *   <li>Constructor shape and NBT pair follow {@link TileEntityHoloText}: {@code NBTTagCompound}
 *       becomes {@link CompoundTag}, and the no-arg constructor becomes the
 *       {@code (BlockEntityType, BlockPos, BlockState)} triple.</li>
 *   <li>{@link #updateSignal(Level, BlockPos)} is carried over, including the fact that it does
 *       <b>not</b> drive the light beam (the 1.12.2 override deliberately omitted the
 *       {@code BlockLoader.light.lightCtl} call that {@code TileEntityHoloText} makes) -- boards light
 *       the world through their own block light level instead. Kept verbatim, not "fixed".</li>
 *   <li>{@code updateThis(TileEntityDecoText)} is the static convenience hook the renderer/scripts used;
 *       it stays.</li>
 * </ul>
 *
 * <p>Also kept: the {@code SUB_ALIGN_*} constants, which are part of this class's API for scripts even
 * though nothing inside the class reads them. The {@code thick = 1} initialisation is preserved too --
 * the deco boards are flat plates, unlike the holographic text they inherit from.</p>
 */
public class TileEntityDecoText extends TileEntityHoloText {

    public static final int SUB_ALIGN_CENTER = 0, SUB_ALIGN_LEFT = 1, SUB_ALIGN_RIGHT = 2;

    public String subContent;
    public int subColor;
    public double subScale;
    public int subAlign;

    public double offsetX, offsetY, offsetZ;
    public double subOffsetX, subOffsetY, subOffsetZ;

    public int optionalBack;

    public TileEntityDecoText(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        thick = 1;

        subContent = " ";
        subColor = 0xFFFFFF;
        subScale = 1.0;
        subAlign = SUB_ALIGN_CENTER;

        offsetX = offsetY = offsetZ = 0;
        subOffsetX = subOffsetY = subOffsetZ = 0;

        optionalBack = 0x000000;
    }

    @Override
    public CompoundTag toNBT(CompoundTag tagCompound) {
        tagCompound.putString("subContent", subContent);
        tagCompound.putInt("subColor", subColor);
        tagCompound.putDouble("subScale", subScale);
        tagCompound.putInt("subAlign", subAlign);

        tagCompound.putDouble("offsetX", offsetX);
        tagCompound.putDouble("offsetY", offsetY);
        tagCompound.putDouble("offsetZ", offsetZ);
        tagCompound.putDouble("subOffsetX", subOffsetX);
        tagCompound.putDouble("subOffsetY", subOffsetY);
        tagCompound.putDouble("subOffsetZ", subOffsetZ);

        tagCompound.putInt("optionalBack", optionalBack);

        return super.toNBT(tagCompound);
    }

    @Override
    public void fromNBT(CompoundTag tagCompound) {
        super.fromNBT(tagCompound);

        subContent = tagCompound.getString("subContent");
        subColor = tagCompound.getInt("subColor");
        subScale = tagCompound.getDouble("subScale");
        subAlign = tagCompound.getInt("subAlign");

        offsetX = tagCompound.getDouble("offsetX");
        offsetY = tagCompound.getDouble("offsetY");
        offsetZ = tagCompound.getDouble("offsetZ");
        subOffsetX = tagCompound.getDouble("subOffsetX");
        subOffsetY = tagCompound.getDouble("subOffsetY");
        subOffsetZ = tagCompound.getDouble("subOffsetZ");

        optionalBack = tagCompound.getInt("optionalBack");
    }

    public static void updateThis(TileEntityDecoText decoText) {
        decoText.refresh();
    }

    @Override
    public void updateSignal(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return;
        }
        if (blockEntity instanceof TileEntityHoloText holoText) {
            if (holoText.getSender() != null) {
                holoText.isEnabled = holoText.senderIsPowered();
            } else {
                holoText.isEnabled = true;
            }

            if (holoText.isEnabled != holoText.prevIsEnabled) {
                holoText.prevIsEnabled = holoText.isEnabled;
                holoText.refresh();
            }
        }
    }
}
