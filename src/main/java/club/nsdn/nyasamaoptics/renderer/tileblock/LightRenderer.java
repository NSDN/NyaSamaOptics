package club.nsdn.nyasamaoptics.renderer.tileblock;

import club.nsdn.nyasamaoptics.tileblock.light.RGBLight;
import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/**
 * Renderer of the ten {@code RGBLight} lamp variants, ported from the 1.12.2 {@code LightRenderer}.
 *
 * <p>The 1.12.2 class is unusual in that it did not own its models: it read {@code RGBLight#resource}
 * (the registry-ish name each variant is built with) and lazily built
 * {@code models/blocks/<resource>_base.obj} and {@code models/blocks/<resource>_light.obj} onto the
 * <i>block</i> instance, exposed as {@code RGBLight.modelShell} / {@code modelLight}. Decision D4
 * removed those fields -- {@code RGBLight}'s own note records it -- so the model names are now resolved
 * to baked models at render time through {@link DeviceModels}, and the static
 * {@link #loadModel(RGBLight)} hook survives only as the place the ten names are documented (it has
 * nothing left to cache on the block).</p>
 *
 * <p>Kept verbatim from 1.12.2: the translation to the block centre, the {@code meta % 4} yaw and the
 * three-way {@code meta / 4} pitch case, the shell drawn untinted, the light shell tinted by
 * {@code TileEntityRGBLight#color} (or dimmed to 0.33 grey when the linked sender is off), and the
 * {@code getBlockType() instanceof RGBLight} guard -- which is now also what supplies the model name, so
 * a block entity of this type that is somehow not backed by an {@code RGBLight} draws nothing, exactly
 * as before.</p>
 *
 * <p><b>Not global:</b> 1.12.2 did not override {@code isGlobalRenderer} here (unlike the boards), so
 * {@link #shouldRenderOffScreen} stays false and the renderer relies on the default view distance. The
 * class effectively renders geometry that is a fraction of a block around its own position, which the
 * default frustum culling handles.</p>
 *
 * <p><b>Deferred:</b> none. The light colour is applied through the vertex colour
 * ({@link ColoredModelRenderer}), and the original's {@code begin/endSpecialLighting} pair is a
 * documented no-op in {@code RendererHelper}.</p>
 */
public class LightRenderer extends AbsTileEntitySpecialRenderer {

    private static final ResourceLocation TEXTURE_SHELL = ResourceLocations.LIGHT_SHELL;
    private static final ResourceLocation TEXTURE_LIGHT = ResourceLocations.LIGHT_BASE;

    public LightRenderer() {
    }

    public LightRenderer(BlockEntityRendererProvider.Context context) {
        this();
    }

    /**
     * 1.12.2 signature kept for call-site parity. It used to fill {@code light.modelShell} /
     * {@code light.modelLight} with freshly parsed {@code WavefrontObject}s; those fields no longer
     * exist (D4), and the renderer resolves the equivalent baked models from
     * {@code <resource>_base} / {@code <resource>_light} instead, so there is nothing left to load.
     */
    public static void loadModel(RGBLight light) {
        // Intentionally empty: models are resolved per frame through DeviceModels (see the class note).
    }

    @Override
    public void renderDevice(TileEntityBase blockEntity, float partialTick, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        int meta = blockEntity.META;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);

        float angle = (meta % 4) * 90.0F;
        poseStack.mulPose(Axis.YN.rotationDegrees(angle));

        Block block = blockEntity.getBlockState().getBlock();
        if (block instanceof RGBLight light && blockEntity instanceof RGBLight.TileEntityRGBLight tile) {
            poseStack.pushPose();

            // 1.12.2 switch (meta / 4): glRotatef(90, -1, 0, 0) is -90 about +X.
            switch (meta / 4) {
                case 1 -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                case 2 -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                default -> {
                }
            }

            String resource = light.resource;
            ColoredModelRenderer.render(
                    DeviceModels.resolve(DeviceModels.id(resource + "_base")), TEXTURE_SHELL,
                    poseStack, bufferSource, packedLight, packedOverlay);

            // The 1.12.2 beginSpecialLighting() was a no-op; the light shell is drawn in the same pass.
            int color = tile.color;
            boolean dim = !tile.isEnabled && tile.getSender() != null;
            if (dim) {
                // 1.12.2 substituted glColor3f(0.33F, 0.33F, 0.33F) for the colour.
                ColoredModelRenderer.render(
                        DeviceModels.resolve(DeviceModels.id(resource + "_light")),
                        TEXTURE_LIGHT, poseStack, bufferSource, packedLight, packedOverlay, 0.33F, 0.33F, 0.33F);
            } else {
                ColoredModelRenderer.render(
                        DeviceModels.resolve(DeviceModels.id(resource + "_light")),
                        TEXTURE_LIGHT, poseStack, bufferSource, packedLight, packedOverlay,
                        ((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F);
            }

            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
