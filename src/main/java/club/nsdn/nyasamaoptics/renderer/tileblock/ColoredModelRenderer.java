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

    /**
     * Per-vertex brightness of a uniform draw -- <b>this is not a colour array; never put the tint in
     * it.</b>
     *
     * <p>{@code VertexConsumer#putBulkData(pose, quad, float[] brightness, red, green, blue, alpha, ...)}
     * multiplies <em>each vertex</em> by {@code brightness[vertex]} -- verified in the 1.21.1
     * {@code VertexConsumer} source: {@code f3 = brightness[l] * red * 255.0F} with {@code l} the vertex
     * index 0..3. In vanilla the array carries the per-corner ambient-occlusion brightness
     * ({@code ModelBlockRenderer} passes {@code aoFace.brightness}); the directional shade is the separate
     * {@code red/green/blue} multiplier from {@code level.getShade(...)}.</p>
     *
     * <p>1.12.2's {@code GL11.glColor3f(r, g, b)} is the vertex colour, i.e. exactly those
     * {@code red/green/blue} arguments -- so the tint goes there and this array stays pinned to 1. Putting
     * the tint here instead multiplies vertex 0 by r, vertex 1 by g, vertex 2 by b and vertex 3 by 1: a
     * four-corner gradient on every non-white tint (the RGB lamp colour, the 0.33 grey of a disabled
     * sender, the optional back plate), invisible only when the tint happens to be white -- which is why
     * this misuse stayed unnoticed.</p>
     */
    private static final float[] UNIFORM_BRIGHTNESS = { 1.0F, 1.0F, 1.0F, 1.0F };

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
        int[] light = { packedLight, packedLight, packedLight, packedLight };
        for (Direction direction : Direction.values()) {
            for (BakedQuad quad : model.getQuads(state, direction, random)) {
                // The tint travels in the red/green/blue arguments, never in the brightness array: that
                // array is indexed per vertex and its element is one scalar applied to all three channels,
                // so a tint placed there would scale vertex 0 by r, vertex 1 by g, vertex 2 by b and
                // vertex 3 by 1 -- see UNIFORM_BRIGHTNESS. putBulkData adds no directional shade of its
                // own (that is ModelBlockRenderer's diffuse pass), so the flat fixed-function look of
                // 1.12.2 is kept.
                consumer.putBulkData(pose, quad, UNIFORM_BRIGHTNESS, red, green, blue, 1.0F, light,
                        packedOverlay, false);
            }
        }
        for (BakedQuad quad : model.getQuads(state, null, random)) {
            consumer.putBulkData(pose, quad, UNIFORM_BRIGHTNESS, red, green, blue, 1.0F, light,
                    packedOverlay, false);
        }

        poseStack.popPose();
    }
}
