package club.nsdn.nyasamaoptics.tileblock.holo;

import club.nsdn.nyasamaoptics.OpticsBlocks;
import club.nsdn.nyasamaoptics.api.LightBeam;
import club.nsdn.nyasamaoptics.util.font.FontLoader;
import club.nsdn.nyasamaoptics.util.font.TextModel;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;

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
 * <p><b>Font model (connected):</b> the 1.12.2 class held a client-only {@code TextModel model} field
 * filled by {@code createModel()} through {@code FontLoader.getModel(font, align, content, thick,
 * color)}, and the renderer drew it. Both halves are back with the ported {@code util.font} package:
 * {@link #model} and {@link #createModel()} are the 1.12.2 members, names and visibility unchanged, so
 * the renderer port can keep calling {@code createModel()} and then reading {@code model} exactly as
 * its 1.12.2 counterpart did.</p>
 *
 * <p>1.12.2 annotated the field and the method as client-only ({@code SideOnly(Side.CLIENT)}). That
 * annotation is not available in v3 (Forge's side-only marker is on the forbidden list), so the members
 * are plain ones and the client-only contract lives in this note instead: {@link #createModel()} must
 * only be called from the renderer, which runs on the client. The {@code HashSet} change-stamp behind
 * {@link #hash} is carried over verbatim, including its mix of the raw {@code content} and
 * {@code String.valueOf(...)} of the numerics.</p>
 *
 * <p><b>Still deferred (renderer):</b> the drawing of {@link #model}
 * ({@code TextModel#render(PoseStack, MultiBufferSource, int, int)} in the ported class) and the
 * infinite render bounding box belong to the block entity renderer, which is ported separately.</p>
 */
public class TileEntityHoloText extends TileEntityReceiver {

    /**
     * Glyph model of this display, or {@code null} until {@link #createModel()} has built one.
     *
     * <p>1.12.2 declared this client-only ({@code SideOnly(Side.CLIENT)}); see the class note for why
     * the annotation is gone and what that means for callers.</p>
     */
    public TextModel model;

    public String content;
    public int color;
    public int thick;
    public double scale;
    public int align;
    public int font;

    public boolean isEnabled;
    public boolean prevIsEnabled;

    /**
     * Change-detection stamp of {@link #model}, {@code -1} meaning "no model built yet". 1.12.2 compared
     * a {@code HashSet} hash of content/thick/scale/font/color against it, and {@link #createModel()}
     * does the same.
     */
    public int hash = -1;

    public TileEntityHoloText(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        content = "O";
        color = 0xFFFFFF;
        thick = 4;
        scale = 1.0;
        font = FontLoader.FONT_SONG;
    }

    /**
     * Builds {@link #model} when the values it depends on have changed -- the 1.12.2 body verbatim,
     * including the {@code HashSet} and its {@code String.valueOf} conversions.
     *
     * <p>1.12.2 marked this client-only ({@code SideOnly(Side.CLIENT)}); the annotation is gone (see the
     * class note), so this must be called from the renderer on the client, which is what every 1.12.2
     * renderer of this family did ({@code AdBoardRenderer}, {@code GuideBoardRenderer},
     * {@code HoloJetRevRenderer}, {@code PillarHeadRenderer}, {@code StationBoardRenderer},
     * {@code TextWallRenderer}).</p>
     */
    public void createModel() {
        HashSet<Object> hashSet = new HashSet<Object>();
        hashSet.add(content);
        hashSet.add(String.valueOf(thick));
        hashSet.add(String.valueOf(scale));
        hashSet.add(String.valueOf(font));
        hashSet.add(String.valueOf(color));

        if (hash != hashSet.hashCode()) {
            model = FontLoader.getModel(font, align, content, thick, color);
            hash = hashSet.hashCode();
        }
    }

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
