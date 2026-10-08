package club.nsdn.nyasamaoptics.renderer.tileblock;

import club.nsdn.nyasamaoptics.tileblock.deco.TileEntityDecoText;
import club.nsdn.nyasamaoptics.util.font.TextModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * The two transform chains the deco boards share, extracted so their renderers stay readable.
 *
 * <p>{@code AdBoardRenderer}, {@code GuideBoardRenderer}, {@code TextWallRenderer} and
 * {@code StationBoardRenderer} all draw the same two things -- the holographic glyph model and the
 * "sub line" of ordinary font text -- and their 1.12.2 originals differed only in the vertical
 * {@code offset} constant, in the orientation of the text they use ({@code GuideBoard}/{@code TextWall}
 * begin with {@code glRotatef(90, -1, 0, 0)}, the other two with {@code glRotatef(90, 1, 0, 0)}), and
 * in which {@code ALIGN_*} constants the sub line compares against. Each chain is reproduced here with
 * the same call order, so the parity with
 * {@code GL11.glPushMatrix/glRotatef/glTranslated/glScaled} is one line per line:</p>
 *
 * <pre>
 * glPushMatrix; glTranslated(0, -offset, 0); glTranslated(offsetX/Y/Z, ...)
 *   glPushMatrix; glRotatef(90, +/-1, 0, 0); glRotatef(180, 0, 0, 1)
 *     glPushMatrix; bindTexture(_text); glTranslated(0, -scale, (16 - thick) / 32.0)
 *       glPushMatrix; glScaled(scale, scale, 1.0)
 *         glPushMatrix; glTranslated(0, 0.5, 0); model.render(te); glPopMatrix
 *       glPopMatrix
 *     glPopMatrix
 *   glPopMatrix
 * glPopMatrix
 *
 * glPushMatrix; [glTranslated(subOffsetX/Y/Z, ...)]
 *   glPushMatrix; glRotatef(-90, 1, 0, 0); glRotatef(180, 0, 1, 0)
 *     glPushMatrix; glTranslatef(0, 0, zOffset); renderString(...); glPopMatrix
 *   glPopMatrix
 * glPopMatrix
 * </pre>
 *
 * <p>Two GL conventions that are easy to get backwards: a negative rotation axis is expressed as the
 * positive {@link Axis} with a negated angle ({@code glRotatef(a, -1, 0, 0)} becomes
 * {@code Axis.XP.rotationDegrees(-a)}), and because GL multiplies each {@code glRotatef} onto the
 * <i>right</i> of the current matrix, the {@code mulPose} calls keep the original order rather than being
 * reversed.</p>
 *
 * <p><b>What is not reproducible:</b> {@code renderEngine.bindTexture(_text)} bound
 * {@code textures/blocks/white.png} for the glyph pass (1.12.2's plural folder name; the v3 assets
 * live under {@code textures/block/}, see {@link ResourceLocations}). In 1.21.1 the glyph model is a
 * {@link TextModel} and {@code TextModel#render} chooses {@code TextureAtlas.LOCATION_BLOCKS} itself, so
 * the white plate is no longer bound. Geometry, colour (baked into the model by {@code TextModel}) and
 * transforms are unchanged.</p>
 */
final class DecoTextRenderer {

    private DecoTextRenderer() {
    }

    /**
     * Draws the glyph model of {@code text} with the shared transform chain.
     *
     * @param yOffset     the POS vertical shift, i.e. the 1.12.2 {@code glTranslated(0, -offset, 0)}
     *                    value before the rotations: 0.375 (AdBoard), -0.125 (GuideBoard), 0.5
     *                    (TextWall). StationBoard's {@code glTranslated(0, offset, 0)} is applied by that
     *                    renderer <i>outside</i> this chain (between the two 90-degree X rotations), so it
     *                    passes 0 here.
     * @param rotateXSign 1 for {@code glRotatef(90, 1, 0, 0)}, -1 for {@code glRotatef(90, -1, 0, 0)}
     */
    static void renderHoloText(TileEntityDecoText text, float yOffset, float rotateXSign,
                               PoseStack poseStack, MultiBufferSource bufferSource, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.0, yOffset, 0.0);
        poseStack.translate(text.offsetX, text.offsetY, text.offsetZ);

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F * rotateXSign));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.pushPose();
        poseStack.translate(0.0, -text.scale, (16 - text.thick) / 32.0);
        poseStack.pushPose();
        poseStack.scale((float) text.scale, (float) text.scale, 1.0F);
        // 1.12.2: text.createModel(); if (text.model != null) { ... text.model.render(te); }
        text.createModel();
        TextModel model = text.model;
        if (model != null) {
            poseStack.pushPose();
            poseStack.translate(0.0, 0.5, 0.0);
            model.render(poseStack, bufferSource, LightTexture.FULL_BRIGHT, packedOverlay);
            poseStack.popPose();
        }
        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();

        poseStack.popPose();
    }

    /**
     * Draws one line of board text with the shared sub-line transform chain.
     *
     * @param zOffset the caller's own depth: {@code offset - 0.01F} for AdBoard/GuideBoard/TextWall,
     *                {@code -offset - 0.01F} for StationBoard, {@code 0.125} for StationBoard's labels
     */
    static void renderLine(String content, double scale, int align, int color,
                           float zOffset, int center, int left, int right,
                           PoseStack poseStack, MultiBufferSource bufferSource) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, zOffset);
        DeviceText.drawString(content, scale, align, color, center, left, right, poseStack, bufferSource);
        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();
    }
}
