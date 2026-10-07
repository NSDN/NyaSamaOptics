package club.nsdn.nyasamaoptics.tileblock.screen;

import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared block entity of the two platform plates, ported from the 1.12.2
 * {@code TileEntityPlatformPlate}. The name is kept even though 1.21.1 calls these block entities.
 *
 * <p>{@link PlatformPlateFull.TileEntityPlatformPlateFull} and
 * {@link PlatformPlateHalf.TileEntityPlatformPlateHalf} extend it and differ only in the box they
 * describe through {@code setInfo}: full is {@code (12, 1, 1, 0.625)} -- {@code METAMAX} 12, one
 * block wide and {@code 0.625} tall -- half is {@code (12, 1, 0.5, 0.5)}. The panel state
 * ({@code content}, {@code color}, {@code scale}, {@code align}) and the enabled pair are preserved
 * verbatim from 1.12.2, as is the {@code updateThis} hook the script cores call.</p>
 *
 * <p>The render bounds are not ported: 1.12.2 overrode {@code shouldRenderInPass(int)} and
 * {@code getRenderBoundingBox()} (returning {@code INFINITE_EXTENT_AABB} because the panel text may
 * stick out of the block). 1.21.1 removed both methods from {@code BlockEntity} (javap-verified);
 * {@code BlockEntityRenderer#shouldRender} is where that logic goes when the platform plate renderer
 * is ported.</p>
 *
 * <p>1.12.2 had no block entity type registration at all -- {@code createNewTileEntity} returned an
 * instance and the game accepted it. In 1.21.1 this class must be constructed with its registered
 * {@link BlockEntityType}, hence the constructor triple.</p>
 */
public class TileEntityPlatformPlate extends TileEntityReceiver {

    public static final int ALIGN_CENTER = 0, ALIGN_LEFT = 1, ALIGN_RIGHT = 2;

    public String content;
    public int color;
    public double scale;
    public int align;

    public boolean isEnabled;
    public boolean prevIsEnabled;

    public TileEntityPlatformPlate(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        content = "DUMMY";
        color = 0x323232;
        scale = 1.0;
        align = ALIGN_CENTER;
    }

    /* NOT PORTED: shouldRenderInPass(int) / getRenderBoundingBox() -- 1.21.1 BlockEntity has
     * neither (javap-verified); both move to the renderer. */

    @Override
    public CompoundTag toNBT(CompoundTag tagCompound) {
        tagCompound.putString("content", content);
        tagCompound.putInt("color", color);
        tagCompound.putDouble("scale", scale);
        tagCompound.putInt("align", align);
        tagCompound.putBoolean("isEnabled", isEnabled);
        return super.toNBT(tagCompound);
    }

    @Override
    public void fromNBT(CompoundTag tagCompound) {
        super.fromNBT(tagCompound);
        content = tagCompound.getString("content");
        color = tagCompound.getInt("color");
        scale = tagCompound.getDouble("scale");
        align = tagCompound.getInt("align");
        isEnabled = tagCompound.getBoolean("isEnabled");
    }

    public static void updateThis(TileEntityPlatformPlate tilePlate) {
        tilePlate.refresh();
    }

}
