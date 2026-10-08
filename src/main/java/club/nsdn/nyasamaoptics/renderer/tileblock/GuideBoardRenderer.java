package club.nsdn.nyasamaoptics.renderer.tileblock;

import club.nsdn.nyasamaoptics.tileblock.deco.TileEntityDecoText;
import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Renderer of the guide boards, ported from the 1.12.2 {@code GuideBoardRenderer}.
 *
 * <p>The guide board has no shell of its own: 1.12.2 loaded no {@code WavefrontObject} here at all (the
 * shaped board is the vanilla block model), and the renderer only draws the holographic line and the
 * sub line. That is why the translation/rotation block looks bare compared with its siblings.</p>
 *
 * <p>Kept verbatim: the {@code meta} normalisation ({@code if ((meta % 2) == 0) meta = 2 - meta;}), the
 * yaw of {@code (meta & 0x3) * 90} about <b>+Y</b> (the other boards use -Y), the fixed
 * {@code glRotatef(90, -1, 0, 0)}, the {@code offset = -0.125F} vertical shift, the
 * {@code (16 - thick) / 32.0} depth placement, the {@code scale} applied to the glyph model, the extra
 * {@code +0.5} lift of the model, and the sender-gated {@code isEnabled} control flag.</p>
 *
 * <p><b>Changed in the port:</b> the glyph model comes from the block entity's own 1.12.2 seam --
 * {@code createModel()} then {@code model}, both restored on {@code TileEntityHoloText} by the tileblock
 * port -- so the original call order and the {@code model != null} guard survive. The
 * {@code renderEngine.bindTexture(_text)} / {@code textures/blocks/white.png} bind (1.12.2's plural
 * folder name; the v3 assets live under {@code textures/block/}, see {@link ResourceLocations}) has
 * no equivalent: a {@code TextModel} carries its own texture ({@code TextModel#render} selects
 * {@code TextureAtlas.LOCATION_BLOCKS}), so the white plate is unused here.</p>
 *
 * <p>Render bounds: 1.12.2 was a global renderer ({@code isGlobalRenderer} true, i.e. the text may be
 * drawn far outside the block) and reported 1024.0 as its maximum render distance squared. That is
 * {@link #shouldRenderOffScreen} + {@link #getViewDistance} here.</p>
 */
public class GuideBoardRenderer extends AbsTileEntitySpecialRenderer {

    /** 1.12.2 {@code getMaxRenderDistanceSquared()} = 1024.0. */
    private static final int VIEW_DISTANCE = 32;

    public GuideBoardRenderer() {
    }

    public GuideBoardRenderer(BlockEntityRendererProvider.Context context) {
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
        if ((meta % 2) == 0) {
            meta = 2 - meta;
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);

        poseStack.pushPose();
        float angle = (meta & 0x3) * 90.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));

        if (blockEntity instanceof TileEntityDecoText text) {
            boolean control = text.getSender() == null || text.isEnabled;
            if (control) {
                final float offset = -0.125F;

                // The shared chains (DecoTextRenderer). Note the -1 sign: this board rotates the glyph
                // block by glRotatef(90, -1, 0, 0), unlike AdBoard/StationBoard.
                DecoTextRenderer.renderHoloText(text, offset, -1.0F, poseStack, bufferSource, packedOverlay);
                // 1.12.2 did NOT translate the sub line by subOffsetX/Y/Z here (AdBoard does); kept as is.
                DecoTextRenderer.renderLine(text.subContent, text.subScale, text.subAlign, text.subColor,
                        offset - 0.01F,
                        TileEntityDecoText.SUB_ALIGN_CENTER, TileEntityDecoText.SUB_ALIGN_LEFT,
                        TileEntityDecoText.SUB_ALIGN_RIGHT, poseStack, bufferSource);
            }
        }

        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();
    }
}
