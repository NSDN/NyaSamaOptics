package club.nsdn.nyasamaoptics.renderer.tileblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

/**
 * Coloured baked-model drawing for the optics renderers -- the one place this port departs from
 * {@code RendererHelper#renderWithResource}.
 *
 * <p><b>Why a second drawing path exists.</b> 1.12.2 kept a colour on the GL state machine
 * ({@code GL11.glColor3f(...)}) and then drew a model; several optics devices depend on exactly that --
 * the RGB lamp tints its light shell with {@code TileEntityRGBLight#color}, the station board tints its
 * board plate with {@code optionalBack}, and every one of them substitutes 0.33 grey when the linked
 * sender is off. In 1.21.1 the equivalent of {@code glColor3f} is the vertex colour, and
 * {@code ModelBlockRenderer#renderModel} (which {@code RendererHelper} uses) offers no way to set one:
 * it derives the four corner colours from the block colour provider of the {@link BlockState} it is
 * handed, and the optics devices have no such provider registered. Drawing the quads here, with the
 * colour passed explicitly, is therefore the faithful mapping rather than a stylistic one.</p>
 *
 * <p><b>What is preserved:</b> the same {@code entityCutoutNoCull(texture)} render type
 * {@code RendererHelper} picks (1.12.2 rendered these with culling off), the same uniform
 * {@code 1/16} model scale, and the same vertex build order. Shading is switched off explicitly to
 * match the fixed-function look 1.12.2 produced -- these are flat lamp shells and printed plates, and
 * directional shading would darken the faces that used to render flat in 1.12.2.</p>
 *
 * <p><b>Deferred:</b> none of the calling renderers needs per-group visibility, so this class is only
 * about colour; the part-model story is in {@code StationBoardRenderer} and
 * {@code docs/08-OBJ-PIPELINE.md} §5.</p>
 */
public final class ColoredModelRenderer {

    private ColoredModelRenderer() {
    }

    /** White -- the 1.12.2 default colour state, i.e. "no tint". */
    public static void render(BakedModel model, ResourceLocation texture, PoseStack poseStack,
                              MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        render(model, texture, poseStack, bufferSource, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F);
    }

    /** Draws {@code model} scaled by 1/16, multiplied by the given colour, as {@code glColor3f} did. */
    public static void render(BakedModel model, ResourceLocation texture, PoseStack poseStack,
                              MultiBufferSource bufferSource, int packedLight, int packedOverlay,
                              float red, float green, float blue) {
        render(model, texture, poseStack, bufferSource, packedLight, packedOverlay, null,
                red, green, blue);
    }

    /**
     * Same, with the old {@code glRotatef} applied first: 1.12.2's
     * {@code renderWithResourceAndRotation} / {@code renderWithResource4} drew the model under a yaw
     * about -Y ({@code Axis.YN}), which is what the 1.12.2 {@code RendererHelper} does too.
     */
    public static void render(BakedModel model, ResourceLocation texture, PoseStack poseStack,
                              MultiBufferSource bufferSource, int packedLight, int packedOverlay,
                              Quaternionf rotation, float red, float green, float blue) {
        if (model == null) {
            return;
        }

        poseStack.pushPose();
        if (rotation != null) {
            poseStack.mulPose(rotation);
        }
        // The old code scaled by 1/16 because the OBJ coordinates were in block units.
        poseStack.scale(0.0625F, 0.0625F, 0.0625F);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        PoseStack.Pose pose = poseStack.last();
        RandomSource random = RandomSource.create();

        BlockState state = null;
        float[] color = { red, green, blue, 1.0F };
        int[] light = { packedLight, packedLight, packedLight, packedLight };
        for (Direction direction : Direction.values()) {
            for (BakedQuad quad : model.getQuads(state, direction, random)) {
                // 1.12.2 left shading to the lighting pass; 1.21.1 applies it in putBulkData unless the
                // third float is 1, so it is pinned to 1 to keep the flat fixed-function look.
                consumer.putBulkData(pose, quad, color, 1.0F, 1.0F, 1.0F, 1.0F, light, packedOverlay, false);
            }
        }
        for (BakedQuad quad : model.getQuads(state, null, random)) {
            consumer.putBulkData(pose, quad, color, 1.0F, 1.0F, 1.0F, 1.0F, light, packedOverlay, false);
        }

        poseStack.popPose();
    }
}
