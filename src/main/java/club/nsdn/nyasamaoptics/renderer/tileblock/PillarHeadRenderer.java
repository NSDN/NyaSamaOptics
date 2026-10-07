package club.nsdn.nyasamaoptics.renderer.tileblock;

import club.nsdn.nyasamaoptics.tileblock.holo.PillarHead;
import club.nsdn.nyasamaoptics.util.font.TextModel;
import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.render.RendererHelper;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Renderer of the pillar head, ported from the 1.12.2 {@code PillarHeadRenderer}.
 *
 * <p>One shell (<code>pillar_head_base</code>) and a downward-running vertical text on two opposite
 * faces. The shell is the one place in this package that uses
 * {@link RendererHelper#renderWithResourceAndRotation} rather than a hand-written yaw: 1.12.2 called it
 * with the plain {@code (meta % 4) * 90} angle and no pitch case (a pillar head is always upright), so
 * the helper's rotate-about-{-Y} is exactly the original transform.</p>
 *
 * <p>Kept verbatim from {@code doRenderText}, which is called twice inside a
 * {@code glRotated(180, 0, 1, 0)} so the text appears on both sides: the {@code 0.0625 * (3.9 - thick)}
 * depth offset, the {@code scale} around the model, the sender-gated {@code control} flag and the
 * {@code model != null} guard, and the {@code +0.5} lift applied before the scale (not after, unlike the
 * boards).</p>
 *
 * <p><b>Changed in the port:</b> the glyph model comes from the block entity's own 1.12.2 seam --
 * {@code createModel()} then {@code model}, both restored on {@code TileEntityHoloText} -- and the
 * {@code white.png} bind has no equivalent because the glyph model selects its own texture.
 * {@link PillarHead.TileEntityPillarHead} sets {@code align = FontLoader.ALIGN_DOWN}, which
 * {@code TextModel} turns into a vertical glyph run -- the reason the model needs no extra rotation here
 * beyond the two calls.</p>
 *
 * <p>Render bounds: 1.12.2 gave this block entity {@code getMaxRenderDistanceSquared()} = 4096.0 and a
 * 5-block render box; the first becomes {@link #getViewDistance()}. 1.12.2 did not mark it as a global
 * renderer, so {@link #shouldRenderOffScreen} stays false, matching the original.</p>
 */
public class PillarHeadRenderer extends AbsTileEntitySpecialRenderer {

    /** 1.12.2 {@code getMaxRenderDistanceSquared()} = 4096.0, i.e. 64 blocks. */
    private static final int VIEW_DISTANCE = 64;

    public PillarHeadRenderer() {
    }

    public PillarHeadRenderer(BlockEntityRendererProvider.Context context) {
        this();
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }

    @Override
    public void renderDevice(TileEntityBase blockEntity, float partialTick, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        int meta = blockEntity.META;

        if (!(blockEntity instanceof PillarHead.TileEntityPillarHead pillar)) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);

        // 1.12.2 called createModel() here, before the shell pass.
        pillar.createModel();
        TextModel model = pillar.model;

        float angle = (meta % 4) * 90.0F;
        RendererHelper.renderWithResourceAndRotation(
                DeviceModels.resolve(DeviceModels.id("pillar_head_base")), angle,
                ResourceLocations.LIGHT_SHELL, poseStack, bufferSource, packedLight, packedOverlay);

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);
        poseStack.pushPose();
        poseStack.scale((float) pillar.scale, (float) pillar.scale, 1.0F);
        poseStack.pushPose();
        // doRenderText(...) and then its 180-degree mirror.
        renderText(pillar, model, poseStack, bufferSource, packedOverlay);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        renderText(pillar, model, poseStack, bufferSource, packedOverlay);
        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();

        poseStack.popPose();

        poseStack.popPose();
    }

    /** 1.12.2 {@code doRenderText}: sender-gated, depth placed by thickness. */
    private void renderText(PillarHead.TileEntityPillarHead pillar, TextModel model,
                            PoseStack poseStack, MultiBufferSource bufferSource, int packedOverlay) {
        boolean control = pillar.getSender() == null || pillar.isEnabled;
        if (model != null && control) {
            poseStack.pushPose();
            // 1.12.2 glTranslated(0.0, 0.0, 0.0625 * (3.9 - thick)) -- double arithmetic, cast to float.
            poseStack.translate(0.0, 0.0, (float) (0.0625 * (3.9 - pillar.thick)));            model.render(poseStack, bufferSource, LightTexture.FULL_BRIGHT, packedOverlay);
            poseStack.popPose();
        }
    }
}
