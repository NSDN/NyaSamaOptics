package club.nsdn.nyasamaoptics.renderer.tileblock;

import club.nsdn.nyasamaoptics.tileblock.deco.StationBoard;
import club.nsdn.nyasamaoptics.tileblock.screen.LEDPlate;
import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/**
 * Renderer of the station board, ported from the 1.12.2 {@code StationBoardRenderer}.
 *
 * <p>The richest of the boards: it draws the shell in <b>three parts</b> plus a two-line hologram, the
 * sub line, and two independently switchable side labels. The 1.12.2 code used
 * {@code RendererHelper.renderPartWithResource(modelBase, "base" | "1" | "2", textureBase)} against the
 * named {@code o} groups of {@code station_board.obj}, and tinted {@code "base"} with
 * {@code TileEntityStationBoard#optionalBack} (0.33 grey while the sender is off).</p>
 *
 * <p>Kept verbatim: the {@code meta % 4} yaw about -Y, the {@code offset = 0.125F} used with
 * <b>opposite signs</b> for the glyph block ({@code +offset}) and the sub line ({@code -offset - 0.01F})
 * -- unique to this board -- the two-stage translation of the sub line (once before the -90 X rotation,
 * once at the outer level), and the label blocks' {@code 卤0.25 + offsetN} horizontal shift with their
 * {@code 0.125} depth.</p>
 *
 * <p><b>Track B deferral, and what this class does instead.</b> {@code renderPartWithResource} is not
 * implemented (see the note in {@code nyasamatelecom}'s {@code RendererHelper} and
 * {@code docs/08-OBJ-PIPELINE.md} 搂5): 1.21.1's model system has no runtime per-group visibility, so the
 * pipeline splits named parts into separate models named {@code <stem>__<part>} (Track B). This renderer
 * therefore asks for {@code nyasamaoptics:block/station_board__base}, {@code __1} and {@code __2} -- the
 * pipeline's own naming -- and falls back to the whole {@code station_board} model when a part id is not
 * baked, via {@link #renderPartOrWhole}. Until the part JSONs are generated and copied into
 * {@code src/main/resources}, all three calls resolve to the whole model, so the board's shell is drawn
 * once plus the extra tinted passes (the {@code alreadyDrawn} set suppresses the exact duplicates). That
 * is a superset of the 1.12.2 picture, not a missing piece: the labels will be visible even with
 * {@code state1}/{@code state2} false until the parts land. Ordering matters more than usual here, so the
 * point to revisit is recorded in the class note rather than left implicit.</p>
 *
 * <p>Render bounds: global renderer in 1.12.2 with {@code getMaxRenderDistanceSquared()} = 4096.0 and a
 * 5-block render box.</p>
 */
public class StationBoardRenderer extends AbsTileEntitySpecialRenderer {

    /** 1.12.2 {@code getMaxRenderDistanceSquared()} = 4096.0, i.e. 64 blocks. */
    private static final int VIEW_DISTANCE = 64;

    /** The whole model, and the Track B part ids the pipeline produces for it. */
    private static final ResourceLocation MODEL_WHOLE = DeviceModels.id("station_board");
    private static final ResourceLocation PART_BASE = DeviceModels.id("station_board__base");
    private static final ResourceLocation PART_1 = DeviceModels.id("station_board__1");
    private static final ResourceLocation PART_2 = DeviceModels.id("station_board__2");

    public StationBoardRenderer() {
    }

    public StationBoardRenderer(BlockEntityRendererProvider.Context context) {
        this();
    }

    /** 1.12.2 {@code isGlobalRenderer}: the board's text is not confined to its own block. */
    @Override
    public boolean shouldRenderOffScreen(TileEntityBase blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }

    @Override
    public void renderDevice(TileEntityBase blockEntity, float partialTick, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        int meta = blockEntity.META;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);

        poseStack.pushPose();
        float angle = (meta % 4) * 90.0F;
        poseStack.mulPose(Axis.YN.rotationDegrees(angle));

        if (blockEntity instanceof StationBoard.TileEntityStationBoard board) {
            Set<ResourceLocation> alreadyDrawn = new HashSet<>();

            int backColor = board.optionalBack;
            boolean dim = !board.isEnabled && board.getSender() != null;
            if (dim) {
                renderPartOrWhole(PART_BASE, alreadyDrawn, poseStack, bufferSource, packedLight, packedOverlay,
                        0.33F, 0.33F, 0.33F);
            } else {
                renderPartOrWhole(PART_BASE, alreadyDrawn, poseStack, bufferSource, packedLight, packedOverlay,
                        ((backColor >> 16) & 0xFF) / 255.0F, ((backColor >> 8) & 0xFF) / 255.0F,
                        (backColor & 0xFF) / 255.0F);
            }

            final float offset = 0.125F;

            // Holo text. The outer -90 X rotation composes with the chain's own -90 X to leave the
            // original +90 in effect; the +offset Y translation sits between the two, exactly as in
            // 1.12.2, so the chain itself is passed yOffset = 0.
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.pushPose();
            poseStack.translate(0.0, offset, 0.0);
            DecoTextRenderer.renderHoloText(board, 0.0F, 1.0F, poseStack, bufferSource, packedOverlay);
            poseStack.popPose();
            poseStack.popPose();

            // Sub text. 1.12.2's outer glRotatef(90, 1, 0, 0) cancels the line chain's own -90 X rotation,
            // so only the Y/Z turns and the -offset - 0.01F depth remain; the sub offset is translated
            // first, as AdBoard does.
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.pushPose();
            poseStack.translate(board.subOffsetX, board.subOffsetY, board.subOffsetZ);
            DecoTextRenderer.renderLine(board.subContent, board.subScale, board.subAlign, board.subColor,
                    -offset - 0.01F,
                    LEDPlate.ALIGN_CENTER, LEDPlate.ALIGN_LEFT, LEDPlate.ALIGN_RIGHT,
                    poseStack, bufferSource);
            poseStack.popPose();
            poseStack.popPose();

            // Side labels: only drawn when the corresponding state flag is on, and only then is their
            // plate part drawn at all.
            if (board.state1) {
                renderPartOrWhole(PART_1, alreadyDrawn, poseStack, bufferSource, packedLight, packedOverlay,
                        1.0F, 1.0F, 1.0F);
                poseStack.pushPose();
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.pushPose();
                poseStack.translate(0.25 + board.offset1, 0, 0.125);
                DecoTextRenderer.renderLine(board.label1, board.scale1, LEDPlate.ALIGN_CENTER,
                        board.color1, 0.0F,
                        LEDPlate.ALIGN_CENTER, LEDPlate.ALIGN_LEFT, LEDPlate.ALIGN_RIGHT,
                        poseStack, bufferSource);
                poseStack.popPose();
                poseStack.popPose();
            }
            if (board.state2) {
                renderPartOrWhole(PART_2, alreadyDrawn, poseStack, bufferSource, packedLight, packedOverlay,
                        1.0F, 1.0F, 1.0F);
                poseStack.pushPose();
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.pushPose();
                poseStack.translate(-0.25 + board.offset2, 0, 0.125);
                DecoTextRenderer.renderLine(board.label2, board.scale2, LEDPlate.ALIGN_CENTER,
                        board.color2, 0.0F,
                        LEDPlate.ALIGN_CENTER, LEDPlate.ALIGN_LEFT, LEDPlate.ALIGN_RIGHT,
                        poseStack, bufferSource);
                poseStack.popPose();
                poseStack.popPose();
            }
        }

        poseStack.popPose();
        poseStack.popPose();
    }

    /**
     * Draws one named part of the station board, or the whole model when the part is not baked.
     *
     * <p>{@code alreadyDrawn} suppresses an exact repeat: while the Track B parts are missing, all three
     * calls fall back to the same whole model, and drawing it three times would blend the shell three
     * times over itself. It is scoped to one render call, so a future where the parts exist simply never
     * hits it.</p>
     */
    private void renderPartOrWhole(ResourceLocation part, Set<ResourceLocation> alreadyDrawn,
                                   PoseStack poseStack, MultiBufferSource bufferSource,
                                   int packedLight, int packedOverlay,
                                   float red, float green, float blue) {
        ResourceLocation key = part;
        if (DeviceModels.resolve(part) == DeviceModels.resolve(MODEL_WHOLE)) {
            // The part id is not baked, so the resolver handed back the shared missing model; draw the
            // whole station board instead (see the class note).
            key = MODEL_WHOLE;
            if (!alreadyDrawn.add(key)) {
                return;
            }
        }
        ColoredModelRenderer.render(DeviceModels.resolve(key), ResourceLocations.LIGHT_BASE, poseStack,
                bufferSource, packedLight, packedOverlay, red, green, blue);
    }
}
