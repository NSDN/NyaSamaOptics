package club.nsdn.nyasamaoptics.tileblock.holo;

import club.nsdn.nyasamaoptics.OpticsBlocks;
import club.nsdn.nyasamaoptics.api.LightBeam;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base block entity of the holographic text displays, ported from the 1.12.2 {@code TileEntityHoloText}.
 *
 * <p>It is the root of the whole text-display family: {@code TileEntityDecoText} in the {@code deco}
 * package extends it, and both {@code HoloJetRev} and {@code PillarHead} nest a subclass of it. What it
 * carries is the text state itself (content, colour, thickness, scale, alignment, font) plus the
 * receiver-driven enabled flag.</p>
 *
 * <p>Mapping notes:</p>
 * <ul>
 *   <li>The no-arg constructor becomes the mandatory {@code (BlockEntityType, BlockPos, BlockState)}
 *       triple, as in {@code TileEntityBase}; subclasses pass their registered type through.</li>
 *   <li>{@code readFromNBT}/{@code writeToNBT} keep the mod's own {@code fromNBT}/{@code toNBT} pair
 *       (see {@code TileEntityBase}); only {@code NBTTagCompound} became {@link CompoundTag}.</li>
 *   <li>{@code BlockLoader.light.lightCtl(world, pos, isEnabled)} becomes
 *       {@link LightBeam#lightCtl(Level, BlockPos, LightBeam, boolean)} with the {@code TYPE_DOT} beam
 *       resolved through {@link OpticsBlocks#dotLight()} -- the same rewrite {@code RGBLight} uses for
 *       its non-flood variants. The beam block is passed in explicitly because the old call resolved
 *       the block out of the loader singleton.</li>
 *   <li>{@code shouldRenderInPass(int)} is gone: verified with javap against
 *       neoforge-21.1.256-merged.jar that 1.21.1's {@code BlockEntity} has no render-pass hook (the
 *       old override returned {@code true} for every pass, so nothing is lost).</li>
 *   <li>{@code getRenderBoundingBox()} and {@code getMaxRenderDistanceSquared()} are not ported:
 *       {@code BlockEntity} has neither method any more (javap-verified). The old override returned
 *       {@code INFINITE_EXTENT_AABB}, which is now the renderer's business
 *       ({@code BlockEntityRenderer#shouldRender} / {@code getViewDistance}). The renderer port must
 *       reproduce it -- the text of this family is drawn far outside its own block.</li>
 * </ul>
 *
 * <p><b>Deferred (D4 + font util):</b> the 1.12.2 class held a client-only {@code TextModel model}
 * field filled by {@code createModel()} through {@code FontLoader.getModel(font, align, content, thick,
 * color)}, and the renderer drew it. Neither {@code TextModel} nor {@code FontLoader} exists in v3 yet
 * -- the font assets are ported separately ({@code 00-PORT-PLAN.md} 4.1) and this class therefore has
 * no model field. The change-detection {@link #hash} field is kept, because it is the state a future
 * model cache keys on, but the hash is no longer computed here: it needs {@code TextModel}'s notion of
 * "what changed", which arrives with the font utility. The renderer port will rebuild the glyph model
 * from the fields below and restore the guard.</p>
 */
public class TileEntityHoloText extends TileEntityReceiver {

    public String content;
    public int color;
    public int thick;
    public double scale;
    public int align;
    public int font;

    public boolean isEnabled;
    public boolean prevIsEnabled;

    /**
     * Change-detection stamp of the rendered model, {@code -1} meaning "no model built yet". Kept from
     * 1.12.2 (which compared a {@code HashSet} hash of content/thick/scale/font/color against it); the
     * computation itself waits on the font utility -- see the class note.
     */
    public int hash = -1;

    public TileEntityHoloText(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        content = "O";
        color = 0xFFFFFF;
        thick = 4;
        scale = 1.0;
        // 1.12.2: font = FontLoader.FONT_SONG, whose value is 1 -- the font constants are part of the
        // deferred font utility (see the class note), so the literal stands in for it for now.
        font = 1;
    }

    /*
     * NOT PORTED HERE: 1.12.2's client-only createModel() built a TextModel through
     * FontLoader.getModel(font, align, content, thick, color) and cached it under `hash`. It needs both
     * the font utility (FontLoader/TextModel, ported with the rest of the optics util package) and the
     * renderer that draws the model, so it is left out rather than stubbed into something that would
     * silently render nothing. The fields it consumed (content/color/thick/scale/align/font) and the
     * `hash` stamp all survive above.
     */

    @Override
    public CompoundTag toNBT(CompoundTag tagCompound) {
        tagCompound.putString("content", content);
        tagCompound.putInt("color", color);
        tagCompound.putInt("thick", thick);
        tagCompound.putDouble("scale", scale);
        tagCompound.putInt("align", align);
        tagCompound.putInt("font", font);
        tagCompound.putBoolean("isEnabled", isEnabled);
        return super.toNBT(tagCompound);
    }

    @Override
    public void fromNBT(CompoundTag tagCompound) {
        super.fromNBT(tagCompound);
        content = tagCompound.getString("content");
        color = tagCompound.getInt("color");
        thick = tagCompound.getInt("thick");
        scale = tagCompound.getDouble("scale");
        align = tagCompound.getInt("align");
        font = tagCompound.getInt("font");
        isEnabled = tagCompound.getBoolean("isEnabled");
    }

    public static void updateThis(TileEntityHoloText holoText) {
        holoText.refresh();
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

            LightBeam dotLight = OpticsBlocks.dotLight();
            if (dotLight != null) {
                LightBeam.lightCtl(level, pos, dotLight, holoText.isEnabled);
            }

            if (holoText.isEnabled != holoText.prevIsEnabled) {
                holoText.prevIsEnabled = holoText.isEnabled;
                holoText.refresh();
            }
        }
    }
}
