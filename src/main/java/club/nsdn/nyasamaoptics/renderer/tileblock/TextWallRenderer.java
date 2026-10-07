package club.nsdn.nyasamaoptics.renderer.tileblock;

import club.nsdn.nyasamaoptics.tileblock.deco.TileEntityDecoText;
import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Renderer of the text wall, ported from the 1.12.2 {@code TextWallRenderer}.
 *
 * <p>Structurally identical to {@link GuideBoardRenderer} -- same {@code meta} normalisation, same
 * {@code +Y} yaw, same fixed {@code glRotatef(90, -1, 0, 0)}, same glyph-model transform chain -- and
 * differs only in the vertical offset, {@code 0.5F} instead of {@code -0.125F}, which is why the wall's
 * text sits half a block lower in the block's local space than the guide board's. Both constants are
 * carried over verbatim.</p>
 *
 * <p>The wall likewise has no {@code WavefrontObject} of its own: the lit variants are plain block
 * models. The glyph model comes from the block entity's own 1.12.2 seam -- {@code createModel()} then
 * {@code model} -- and the {@code white.png} bind disappears because {@code TextModel#render} selects its
 * own texture.</p>
 *
 * <p>Render bounds: global renderer in 1.12.2, {@code getMaxRenderDistanceSquared()} = 4096.0
 * (64 blocks, matching the default {@link #getViewDistance()} -- the override is kept explicit so the
 * number stays visible in the port).</p>
 */
public class TextWallRenderer extends AbsTileEntitySpecialRenderer {

    /** 1.12.2 {@code getMaxRenderDistanceSquared()} = 4096.0. */
    private static final int VIEW_DISTANCE = 64;

    public TextWallRenderer() {
    }

    public TextWallRenderer(BlockEntityRendererProvider.Context context) {
        this();
    }

    /** 1.12.2 {@code isGlobalRenderer}: the wall's text is not confined to its own block. */
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
                final float offset = 0.5F;

                // The shared chains (DecoTextRenderer); the -1 sign is this board's glRotatef(90, -1, 0, 0).
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
