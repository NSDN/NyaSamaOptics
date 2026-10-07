package club.nsdn.nyasamaoptics.renderer.tileblock;

import club.nsdn.nyasamaoptics.tileblock.screen.TileEntityPlatformPlate;
import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Renderer of the two platform plates (the lit platform edge), ported from the 1.12.2
 * {@code PlatformPlateRenderer}.
 *
 * <p>The class takes a half/full flag, exactly as in 1.12.2, because the two variants differ only in
 * their shell -- <code>platform_light_full_base</code> vs <code>platform_light_half_base</code> -- and in
 * the vertical shift of the text. The registration step constructs it twice:
 * {@code () -> new PlatformPlateRenderer(false)} for the full plate and {@code (true)} for the half
 * one.</p>
 *
 * <p>Kept verbatim: the {@code meta % 4} yaw about <b>-Y</b>, the {@code meta / 4} pitch case, the
 * shell's scale and the angled text placement. The text placement arithmetic is the fiddliest piece of
 * the module and is reproduced literally:</p>
 * <pre>
 * a = 68 degrees; full = 1.8/16; half = 0.5/16
 * glTranslatef(0, d, -tan(a) * d)     with d = full (or half)
 * shift = 0.75/16 (full) or 1.5/16 (half)
 * glTranslatef(0, tan(a) * shift, shift)
 * glRotatef(a, 1, 0, 0)
 * </pre>
 * <p>{@code Math.tan} is called on the <b>degrees</b> value, as 1.12.2 did -- {@code Math.tan(68.0)}
 * rather than {@code Math.tan(Math.toRadians(68.0))}. That is almost certainly not what the author
 * intended (the tangent of 68 radians), but it is what the shipped mod rendered, and the plate's text
 * angle is visible on screen, so "fixing" it would be a visible behaviour change. It is preserved and
 * flagged here rather than silently corrected.</p>
 *
 * <p><b>Not ported / changed:</b> the vanilla {@code RenderHelper} glow pass around the text is gone;
 * the modern font batch is drawn directly, and the glyph colour comes from
 * {@code TileEntityPlatformPlate#color} through {@link DeviceText}. The shell is drawn with
 * {@link ColoredModelRenderer} at white, as 1.12.2 set {@code glColor3f(1, 1, 1)} before it.</p>
 *
 * <p>Render bounds: 1.12.2's block entity returned {@code INFINITE_EXTENT_AABB} and did not mark the
 * renderer global; the panel text can stick out of its block, so {@link #shouldRenderOffScreen} is set
 * here.</p>
 */
public class PlatformPlateRenderer extends AbsTileEntitySpecialRenderer {

    private static final String MODEL_FULL = "platform_light_full_base";
    private static final String MODEL_HALF = "platform_light_half_base";

    private final boolean isHalf;

    public PlatformPlateRenderer(boolean isHalf) {
        this.isHalf = isHalf;
    }

    public PlatformPlateRenderer(boolean isHalf, BlockEntityRendererProvider.Context context) {
        this(isHalf);
    }

    /** 1.12.2's block entity reported {@code INFINITE_EXTENT_AABB}: the text is not block-confined. */
    @Override
    public boolean shouldRenderOffScreen(TileEntityBase blockEntity) {
        return true;
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
                DeviceModels.resolve(DeviceModels.id(isHalf ? MODEL_HALF : MODEL_FULL)),
                ResourceLocations.LIGHT_SHELL, poseStack, bufferSource, packedLight, packedOverlay);

        if (blockEntity instanceof TileEntityPlatformPlate plate) {
            boolean control = plate.getSender() == null || plate.isEnabled;
            if (control) {
                final float a = 68.0F;
                final float full = 1.8F / 16.0F;
                final float half = 0.5F / 16.0F;
                poseStack.pushPose();
                float shift;
                if (!isHalf) {
                    poseStack.translate(0.0F, full, -(float) Math.tan(a) * full);
                    shift = 0.75F / 16.0F;
                } else {
                    poseStack.translate(0.0F, half, -(float) Math.tan(a) * half);
                    shift = 1.5F / 16.0F;
                }
                poseStack.translate(0.0F, (float) Math.tan(a) * shift, shift);
                poseStack.pushPose();
                poseStack.mulPose(Axis.XP.rotationDegrees(a));
                doRenderString(plate, poseStack, bufferSource);
                poseStack.popPose();
                poseStack.popPose();
            }
        }

        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();
    }

    /** 1.12.2 {@code doRenderString}: rotate -90 about X, then 180 about Y, then the shared line chain. */
    private void doRenderString(TileEntityPlatformPlate plate, PoseStack poseStack,
                                MultiBufferSource bufferSource) {
        DecoTextRenderer.renderLine(plate.content, plate.scale, plate.align, plate.color, 0.0F,
                TileEntityPlatformPlate.ALIGN_CENTER, TileEntityPlatformPlate.ALIGN_LEFT,
                TileEntityPlatformPlate.ALIGN_RIGHT, poseStack, bufferSource);
    }
}
