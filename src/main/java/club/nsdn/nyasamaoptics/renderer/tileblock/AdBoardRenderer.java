package club.nsdn.nyasamaoptics.renderer.tileblock;

import club.nsdn.nyasamaoptics.tileblock.deco.TileEntityDecoText;
import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Renderer of the advertisement board, ported from the 1.12.2 {@code AdBoardRenderer}.
 *
 * <p>Two shells from the OBJ set (<code>ad_board_base</code> with the light shell texture,
 * <code>ad_board_light</code> with the light base texture) plus the holographic line and the sub line.
 * The light shell is tinted by {@code TileEntityDecoText#optionalBack}, or dimmed to 0.33 grey while the
 * linked sender is off -- that is what {@link ColoredModelRenderer} exists for.</p>
 *
 * <p>Kept verbatim: the {@code meta % 4} yaw about <b>-Y</b>, the {@code meta / 4} pitch case applied
 * after it, the {@code offset = 0.375F} downward shift of the glyph block, the
 * {@code (16 - thick) / 32.0} depth, the {@code scale} around the model, the extra {@code +0.5} lift, and
 * the sub line's own offset/rotate chain ending in {@code offset - 0.01F}.</p>
 *
 * <p><b>Changed in the port:</b> the glyph model comes from the block entity's own 1.12.2 seam --
 * {@code createModel()} then {@code model} (both restored on {@code TileEntityHoloText} by the tileblock
 * port) -- so the renderer keeps the original call order and the {@code model != null} guard. What is
 * <i>not</i> reproducible is the {@code renderEngine.bindTexture(_text)} white plate: the ported
 * {@code TextModel#render} selects {@code TextureAtlas.LOCATION_BLOCKS} itself.</p>
 *
 * <p>Render bounds: global renderer in 1.12.2 with {@code getMaxRenderDistanceSquared()} = 1024.0 and a
 * 3-block render box; the first becomes {@link #shouldRenderOffScreen} and
 * {@link #getViewDistance()}.</p>
 */
public class AdBoardRenderer extends AbsTileEntitySpecialRenderer {

    /** 1.12.2 {@code getMaxRenderDistanceSquared()} = 1024.0, i.e. 32 blocks. */
    private static final int VIEW_DISTANCE = 32;

    public AdBoardRenderer() {
    }

    public AdBoardRenderer(BlockEntityRendererProvider.Context context) {
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

        poseStack.pushPose();
        switch (meta / 4) {
            case 1 -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            case 2 -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            default -> {
            }
        }

        ColoredModelRenderer.render(
                DeviceModels.resolve(DeviceModels.id("ad_board_base")), ResourceLocations.LIGHT_SHELL,
                poseStack, bufferSource, packedLight, packedOverlay);

        if (blockEntity instanceof TileEntityDecoText text) {
            int backColor = text.optionalBack;
            boolean dim = !text.isEnabled && text.getSender() != null;
            if (dim) {
                ColoredModelRenderer.render(
                        DeviceModels.resolve(DeviceModels.id("ad_board_light")), ResourceLocations.LIGHT_BASE,
                        poseStack, bufferSource, packedLight, packedOverlay, 0.33F, 0.33F, 0.33F);
            } else {
                ColoredModelRenderer.render(
                        DeviceModels.resolve(DeviceModels.id("ad_board_light")), ResourceLocations.LIGHT_BASE,
                        poseStack, bufferSource, packedLight, packedOverlay,
                        ((backColor >> 16) & 0xFF) / 255.0F, ((backColor >> 8) & 0xFF) / 255.0F,
                        (backColor & 0xFF) / 255.0F);
            }

            boolean control = text.getSender() == null || text.isEnabled;

            if (control) {
                final float offset = 0.375F;

                // The glyph model, then the sub line -- both chains live in DecoTextRenderer, and the
                // "+1" signs are AdBoard's own (GuideBoard/TextWall use -1).
                DecoTextRenderer.renderHoloText(text, -offset, 1.0F, poseStack, bufferSource, packedOverlay);

                // Sub text: translated by the sub offset first, then the shared line chain.
                poseStack.pushPose();
                poseStack.translate(text.subOffsetX, text.subOffsetY, text.subOffsetZ);
                DecoTextRenderer.renderLine(text.subContent, text.subScale, text.subAlign, text.subColor,
                        offset - 0.01F,
                        TileEntityDecoText.SUB_ALIGN_CENTER, TileEntityDecoText.SUB_ALIGN_LEFT,
                        TileEntityDecoText.SUB_ALIGN_RIGHT, poseStack, bufferSource);
                poseStack.popPose();
            }
        }

        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();
    }
}
