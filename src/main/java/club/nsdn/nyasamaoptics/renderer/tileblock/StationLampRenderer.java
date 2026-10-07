package club.nsdn.nyasamaoptics.renderer.tileblock;

import club.nsdn.nyasamaoptics.tileblock.screen.StationLamp;
import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer of the station lamp (the hanging name-board lamp), ported from the 1.12.2
 * {@code StationLampRenderer}.
 *
 * <p>The lamp is a stack: one 5.5/16-tall segment per line in
 * {@code TileEntityStationLamp#lampInfo}, drawn with the <code>station_lamp_end</code> shell for the
 * first and <code>station_lamp_mid</code> for the rest, each wrapped in four
 * <code>station_lamp_back</code> panels (the 1.12.2 {@code renderWithResource4}, i.e. the model drawn
 * four times 90 degrees apart -- reproduced here as an explicit loop so the per-face colour can be
 * applied) and four text faces; then one <code>station_lamp_top</code> cap carrying the
 * <code>station_lamp_logo</code> on all four sides.</p>
 *
 * <p>Kept verbatim: the {@code count * 5.5 / 16} stacking (note the integer-free double arithmetic -- the
 * index is an {@code int} and {@code 5.5} is a {@code double}, so the offset is exact); the
 * {@code lightOff = getSender() != null && !isEnabled} rule, which dims both the back panels and the
 * logo to 0.33 grey; the {@code count -= 1} rewind before the cap; the four-times-repeated text with its
 * {@code 180}-about-Z rotation and {@code (0, 0.3125, -0.5)} placement; and the
 * {@code !logo.equals("null")} switch between a custom logo texture and the default one.</p>
 *
 * <p><b>Changed / not portable:</b> 1.12.2 did {@code new ResourceLocation(lamp.logo)}. In 1.21.1 that
 * constructor throws on a malformed path, and {@code lamp.logo} is arbitrary NBT-driven text, so
 * {@link ResourceLocation#tryParse(String)} is used and a malformed value falls back to the default logo
 * texture -- a save that used to crash the render thread now draws the default. {@code Util.isSpace} /
 * the vanilla glow pass around the text have no equivalent; the glyphs are drawn by {@link DeviceText}
 * directly.</p>
 *
 * <p>Render bounds: global renderer in 1.12.2 with {@code getMaxRenderDistanceSquared()} = 4096.0 and an
 * {@code INFINITE_EXTENT_AABB}, because the lamp's board may stick out of its block.</p>
 */
public class StationLampRenderer extends AbsTileEntitySpecialRenderer {

    /** 1.12.2 {@code getMaxRenderDistanceSquared()} = 4096.0, i.e. 64 blocks. */
    private static final int VIEW_DISTANCE = 64;

    private static final String MODEL_TOP = "station_lamp_top";
    private static final String MODEL_MID = "station_lamp_mid";
    private static final String MODEL_END = "station_lamp_end";
    private static final String MODEL_LOGO = "station_lamp_logo";
    private static final String MODEL_BACK = "station_lamp_back";

    public StationLampRenderer() {
    }

    public StationLampRenderer(BlockEntityRendererProvider.Context context) {
        this();
    }

    /** 1.12.2 {@code isGlobalRenderer}: the lamp's board is not confined to its own block. */
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

        if (blockEntity instanceof StationLamp.TileEntityStationLamp lamp) {
            poseStack.pushPose();

            boolean lightOff = lamp.getSender() != null && !lamp.isEnabled;

            int count = 0;
            for (StationLamp.TileEntityStationLamp.LampInfo info : lamp.lampInfo) {
                poseStack.pushPose();
                poseStack.translate(0.0, count * 5.5 / 16, 0.0);

                ColoredModelRenderer.render(
                        DeviceModels.resolve(DeviceModels.id(count == 0 ? MODEL_END : MODEL_MID)),
                        ResourceLocations.STATION_LAMP_BASE, poseStack, bufferSource, packedLight, packedOverlay);

                int backColor = info.back;
                float backRed;
                float backGreen;
                float backBlue;
                if (lightOff) {
                    backRed = backGreen = backBlue = 0.33F;
                } else {
                    backRed = ((backColor >> 16) & 0xFF) / 255.0F;
                    backGreen = ((backColor >> 8) & 0xFF) / 255.0F;
                    backBlue = (backColor & 0xFF) / 255.0F;
                }
                // 1.12.2 renderWithResource4(modelBack, textureBack): four copies, 90 degrees apart.
                for (int i = 0; i < 4; i++) {
                    ColoredModelRenderer.render(
                            DeviceModels.resolve(DeviceModels.id(MODEL_BACK)),
                            ResourceLocations.STATION_LAMP_BACK, poseStack, bufferSource, packedLight,
                            packedOverlay, Axis.YN.rotationDegrees(90.0F * i),
                            backRed, backGreen, backBlue);
                }

                for (int i = 0; i < 4; i++) {
                    renderFore(info, 90.0F * i, lightOff, poseStack, bufferSource);
                }

                poseStack.popPose();

                count += 1;
            }

            count -= 1;
            poseStack.pushPose();
            poseStack.translate(0.0, count * 5.5 / 16, 0.0);

            ColoredModelRenderer.render(
                    DeviceModels.resolve(DeviceModels.id(MODEL_TOP)),
                    ResourceLocations.STATION_LAMP_BASE, poseStack, bufferSource, packedLight, packedOverlay);

            float logoShade = lightOff ? 0.33F : 1.0F;
            // 1.12.2: new ResourceLocation(lamp.logo) -- replaced by tryParse (see the class note).
            ResourceLocation logoTexture = ResourceLocations.STATION_LAMP_LOGO;
            if (!lamp.logo.equals("null")) {
                ResourceLocation parsed = ResourceLocation.tryParse(lamp.logo);
                if (parsed != null) {
                    logoTexture = parsed;
                }
            }
            for (int i = 0; i < 4; i++) {
                ColoredModelRenderer.render(
                        DeviceModels.resolve(DeviceModels.id(MODEL_LOGO)), logoTexture,
                        poseStack, bufferSource, packedLight, packedOverlay, Axis.YN.rotationDegrees(90.0F * i),
                        logoShade, logoShade, logoShade);
            }
            poseStack.popPose();

            poseStack.popPose();
        }

        poseStack.popPose();
        poseStack.popPose();
    }

    /** 1.12.2 {@code renderFore}: one face's text, rotated 90 degrees about +Y and drawn when lit. */
    private void renderFore(StationLamp.TileEntityStationLamp.LampInfo info, float angle, boolean lightOff,
                            PoseStack poseStack, MultiBufferSource bufferSource) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        if (!lightOff) {
            int fontColor = info.color;
            double fontScale = info.scale;
            String content = info.content;
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.3125F, -0.5F);
            // 1.12.2 passed StationLamp.ALIGN_CENTER for align, and its switch had no default case: for
            // the LEFT/RIGHT values x stayed at its previous value. Passing the same constant for all
            // three parameters reproduces that exactly.
            DecoTextRenderer.renderLine(content, fontScale, StationLamp.ALIGN_CENTER, fontColor, 0.0F,
                    StationLamp.ALIGN_CENTER, StationLamp.ALIGN_CENTER, StationLamp.ALIGN_CENTER,
                    poseStack, bufferSource);
            poseStack.popPose();
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
