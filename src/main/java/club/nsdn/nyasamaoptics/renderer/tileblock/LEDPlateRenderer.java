package club.nsdn.nyasamaoptics.renderer.tileblock;

import club.nsdn.nyasamaoptics.tileblock.screen.LEDPlate;
import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Renderer of the LED text plate, ported from the 1.12.2 {@code LEDPlateRenderer}.
 *
 * <p>Same shape as {@link AdBoardRenderer}: two shells (<code>led_plate_base</code>,
 * <code>led_plate_light</code>) under the {@code meta % 4} yaw about -Y and the {@code meta / 4} pitch
 * case, the light shell tinted by {@code TileEntityLEDPlate#back} (0.33 grey while the sender is off),
 * then the panel text.</p>
 *
 * <p>Kept verbatim: the {@code te.getBlockType() instanceof LEDPlate} guard that wraps the whole shell
 * block (now {@code getBlockState().getBlock()}), the {@code control} flag =
 * {@code getSender() == null || isEnabled}, the {@code inSpecial ? specialBuffer : content} selection,
 * and the text transform chain ending in {@code 0.375F - 0.01F}.</p>
 *
 * <p>Text alignment uses {@code LEDPlate.ALIGN_CENTER/LEFT/RIGHT}, passed into
 * {@link DeviceText#drawString} rather than imported into it, because the four devices that share that
 * method each define their own copy of the constants.</p>
 *
 * <p>Render bounds: 1.12.2 overrode neither {@code isGlobalRenderer} nor
 * {@code getMaxRenderDistanceSquared} here, so the default view distance is correct and
 * {@link #shouldRenderOffScreen} stays false. The block entity's
 * {@code getRenderBoundingBox()} was the inherited {@code FULL_BLOCK_AABB} equivalent.</p>
 */
public class LEDPlateRenderer extends AbsTileEntitySpecialRenderer {

    public LEDPlateRenderer() {
    }

    public LEDPlateRenderer(BlockEntityRendererProvider.Context context) {
        this();
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

        if (blockEntity.getBlockState().getBlock() instanceof LEDPlate) {
            poseStack.pushPose();

            switch (meta / 4) {
                case 1 -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                case 2 -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                default -> {
                }
            }

            ColoredModelRenderer.render(
                    DeviceModels.resolve(DeviceModels.id("led_plate_base")), ResourceLocations.LIGHT_SHELL,
                    poseStack, bufferSource, packedLight, packedOverlay);

            if (blockEntity instanceof LEDPlate.TileEntityLEDPlate plate) {
                int backColor = plate.back;
                boolean dim = !plate.isEnabled && plate.getSender() != null;
                if (dim) {
                    ColoredModelRenderer.render(
                            DeviceModels.resolve(DeviceModels.id("led_plate_light")),
                            ResourceLocations.LIGHT_BASE, poseStack, bufferSource, packedLight, packedOverlay,
                            0.33F, 0.33F, 0.33F);
                } else {
                    ColoredModelRenderer.render(
                            DeviceModels.resolve(DeviceModels.id("led_plate_light")),
                            ResourceLocations.LIGHT_BASE, poseStack, bufferSource, packedLight, packedOverlay,
                            ((backColor >> 16) & 0xFF) / 255.0F, ((backColor >> 8) & 0xFF) / 255.0F,
                            (backColor & 0xFF) / 255.0F);
                }

                boolean control = plate.getSender() == null || plate.isEnabled;
                if (control) {
                    int fontColor = plate.color;
                    int align = plate.align;
                    double fontScale = plate.scale;
                    String content = plate.inSpecial ? plate.specialBuffer : plate.content;
                    poseStack.pushPose();
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                    poseStack.pushPose();
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                    poseStack.pushPose();
                    poseStack.translate(0.0F, 0.0F, 0.375F - 0.01F);
                    DeviceText.drawString(content, fontScale, align, fontColor,
                            LEDPlate.ALIGN_CENTER, LEDPlate.ALIGN_LEFT, LEDPlate.ALIGN_RIGHT,
                            poseStack, bufferSource);
                    poseStack.popPose();
                    poseStack.popPose();
                    poseStack.popPose();
                }
            }

            poseStack.popPose();
        }

        poseStack.popPose();
        poseStack.popPose();
    }
}
