package club.nsdn.nyasamaoptics.renderer.tileblock;

import club.nsdn.nyasamaoptics.tileblock.holo.HoloJetRev;
import club.nsdn.nyasamaoptics.util.font.FontLoader;
import club.nsdn.nyasamaoptics.util.font.TextModel;
import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Renderer of the reversed holo jet, ported from the 1.12.2 {@code HoloJetRevRenderer}.
 *
 * <p>Two shells (<code>holo_jet_rev_base</code> with the light shell texture,
 * <code>holo_jet_rev_light</code> with the light base texture) are drawn with the same
 * centre/yaw/pitch transform as {@link LightRenderer}, then the glyph model is drawn rotated
 * {@code 180} about Z and by the yaw, one and a half blocks below the centre, with the
 * {@link FontLoader} alignment shifting it further.</p>
 *
 * <p>Kept verbatim: the {@code meta % 4} / {@code meta / 4} orientation pair, the {@code angle} reused by
 * both the shell and the glyph pass, the alignment-dependent translations
 * ({@code 0.5 - 2 * scale * (content.length() - 1)} for {@code ALIGN_UP},
 * {@code 2.5 - scale} for {@code ALIGN_DOWN}, {@code 1.5 - scale} otherwise -- note these are <i>not</i>
 * the text-alignment constants the boards use, they are the font's layout mode), the
 * {@code (16 - thick) / 32.0} depth, the {@code scale} applied around the model, and the
 * {@code model != null && control} guard.</p>
 *
 * <p><b>Changed in the port:</b> the glyph model comes from the block entity's own 1.12.2 seam --
 * {@code createModel()} then {@code model}, both restored on {@code TileEntityHoloText} by the tileblock
 * port -- so the original call position and the {@code model != null && control} guard survive; the
 * {@code white.png} bind has no equivalent because {@code TextModel} selects its own texture in
 * {@code TextModel#render}.</p>
 *
 * <p>Render bounds: global renderer in 1.12.2 with {@code getMaxRenderDistanceSquared()} = 65536.0, by
 * far the largest in the module (256 blocks).</p>
 */
public class HoloJetRevRenderer extends AbsTileEntitySpecialRenderer {

    /** 1.12.2 {@code getMaxRenderDistanceSquared()} = 65536.0, i.e. 256 blocks. */
    private static final int VIEW_DISTANCE = 256;

    public HoloJetRevRenderer() {
    }

    public HoloJetRevRenderer(BlockEntityRendererProvider.Context context) {
        this();
    }

    /** 1.12.2 {@code isGlobalRenderer}: the hologram is not confined to its own block. */
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

        if (!(blockEntity instanceof HoloJetRev.TileEntityHoloJetRev tile)) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);

        // 1.12.2 called createModel() here, before the shell pass.
        tile.createModel();
        TextModel model = tile.model;

        float angle = (meta % 4) * 90.0F;
        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(angle));
        poseStack.pushPose();
        switch (meta / 4) {
            case 1 -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            case 2 -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            default -> {
            }
        }
        ColoredModelRenderer.render(
                DeviceModels.resolve(DeviceModels.id("holo_jet_rev_base")),
                ResourceLocations.LIGHT_SHELL, poseStack, bufferSource, packedLight, packedOverlay);
        ColoredModelRenderer.render(
                DeviceModels.resolve(DeviceModels.id("holo_jet_rev_light")),
                ResourceLocations.LIGHT_BASE, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        poseStack.pushPose();
        poseStack.translate(0.0F, -1.5F, 0.0F);
        poseStack.pushPose();
        if (tile.align == FontLoader.ALIGN_UP) {
            poseStack.translate(0.0, 0.5 - 2 * tile.scale * (tile.content.length() - 1),
                    (16 - tile.thick) / 32.0);
        } else if (tile.align == FontLoader.ALIGN_DOWN) {
            poseStack.translate(0.0, 2.5 - tile.scale, (16 - tile.thick) / 32.0);
        } else {
            poseStack.translate(0.0, 1.5 - tile.scale, (16 - tile.thick) / 32.0);
        }
        poseStack.pushPose();
        poseStack.scale((float) tile.scale, (float) tile.scale, 1.0F);
        boolean control = tile.getSender() == null || tile.isEnabled;
        if (model != null && control) {
            model.render(poseStack, bufferSource, LightTexture.FULL_BRIGHT, packedOverlay);
        }
        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();

        poseStack.popPose();

        poseStack.popPose();
    }
}
