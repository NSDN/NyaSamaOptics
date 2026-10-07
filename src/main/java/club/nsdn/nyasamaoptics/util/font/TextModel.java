package club.nsdn.nyasamaoptics.util.font;

import club.nsdn.nyasamaoptics.NyaSamaOptics;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Text geometry for the holographic/board devices, ported from the 1.12.2
 * {@code club.nsdn.nyasamaoptics.util.font.TextModel}.
 *
 * <p>A model is a 1bpp bitmap font plus a string; every lit bit becomes a small cube
 * (1/16 x 1/16 x thick/16, textured with a white block sprite) placed on the 1/16 grid and centred
 * on the block. The glyph decoding, the pixel placement and the alignment arithmetic are carried over
 * word for word -- including the 1.12.2 quirks (ASCII table indexed {@code first * 64}, GB2312 indexed
 * {@code ((first - 0xA1) * 0x5E + (second - 0xA1)) * 128}, {@code ALIGN_DOWN} drawing the same
 * vertical string as {@code ALIGN_UP}, and {@code ALIGN_NULL}/{@code ALIGN_NOP} drawing nothing).
 * ASCII glyphs also use the <em>horizontal</em> origin 0 while CJK vertical glyphs use 0 as well,
 * exactly as before.</p>
 *
 * <p><b>Rewritten: the geometry pipeline.</b> 1.12.2 built the cubes with
 * {@code cn.ac.nya.rawmdl.RawQuadCube}/{@code RawQuadGroup}, baked them into
 * {@code BakedQuad}s and culled coincident quads with {@code cn.ac.nya.mutable.MutableQuad}. That whole
 * self-made OBJ/quad stack is discarded by decision D4, so the port replaces it with an equivalent
 * bake that writes a plain float buffer:</p>
 * <ul>
 *   <li>{@code drawPixel} still receives the same {@code (x, y, thick)} and still describes the same
 *       cube -- {@code RawQuadBase} centres it in X and Z and grows it upwards from Y=0, which is why
 *       the corner table below looks asymmetric in Y;</li>
 *   <li>the six faces are emitted in the original order with the original corner order and the original
 *       UV corners (the old code passed {@code u}/{@code v} of 0..16, which is the whole sprite, so the
 *       port uses {@code getU0()}/{@code getU1()}/{@code getV0()}/{@code getV1()} directly);</li>
 *   <li>the old {@code cullFace} removed pairs of quads whose four vertices coincided, which for this
 *       geometry means "the shared face between two neighbouring pixels" (pixels are saved into a set,
 *       so identical pixels collapse exactly as the old cull did). The port drops a face when the
 *       neighbouring pixel is filled, which is the same set of faces at O(n) instead of O(n^2).</li>
 * </ul>
 * <p>The old public helpers {@code dist}, {@code vertexEquals}, {@code cullFace} and the
 * {@code BakedQuad} list are therefore gone; nothing outside this class used them (checked across the
 * 1.12.2 tree -- only {@code FontLoader#getModel} and {@code model.render(...)} are called from
 * outside).</p>
 *
 * <p><b>Rewritten: the render call,</b> because 1.21.1 has no immediate-mode {@code Tessellator}.
 * 1.12.2's {@code render(TileEntity)} (which read {@code world.getCombinedLight(pos, 1)}) and
 * {@code render(World, BlockPos, BufferBuilder)} collapse into one entry point that takes what a
 * {@code BlockEntityRenderer} is handed: {@link #render(PoseStack, MultiBufferSource, int, int)}.
 * The renderers that call it ({@code HoloJetRevRenderer}, {@code PillarHeadRenderer},
 * {@code StationBoardRenderer}, ...) are not ported yet, so this signature is the seam they will use.
 * Two details of the old render are preserved on purpose: the lock is taken with {@code tryLock()} so a
 * frame is skipped while the background bake runs, and the per-face diffuse factor that 1.12.2 got from
 * {@code LightUtil.diffuseLight(face)} is applied to the vertex colour, because NeoForge 1.21.1 no
 * longer exposes that helper (direction shading is baked into block meshes).</p>
 *
 * <p><b>Deferred:</b></p>
 * <ul>
 *   <li><b>Atlas sprite caching.</b> 1.12.2 held the sprite in a static field resolved at class load;
 *       1.21.1 reloads atlases, so the look-up now happens per model construction and tolerates a
 *       missing client/atlas (returns degenerate UVs instead of throwing). UVs baked here can still go
 *       stale across a resource reload -- the old code had the same flaw. The proper fix (re-bake on
 *       {@code RegisterClientReloadListenersEvent}) belongs with the renderers, which own the models'
 *       lifetime.</li>
 *   <li><b>Off-thread baking.</b> 1.12.2 scheduled the bake on the shared
 *       {@code NyaSamaLib.EXECUTOR}; that pool is gone from v3 (see {@code docs/09} §10), so this class
 *       owns a single daemon scheduler. Nothing but this model's own float buffer is touched off-thread,
 *       and publication happens under the same lock as before.</li>
 *   <li><b>Sprite name.</b> {@code "blocks/concrete_white"} is the pre-1.13 texture path; in 1.21.1 the
 *       same vanilla texture is {@code minecraft:block/white_concrete}.</li>
 *   <li><b>{@code TextureAtlas.LOCATION_BLOCKS} is {@code @Deprecated} in 21.1.256</b> (javap:
 *       {@code Deprecated: true} on the field). It is still used here because 1.21.1 has no
 *       non-deprecated public name for the block sheet -- {@code Sheets} exposes sheet
 *       {@code RenderType}s, not the atlas location -- and {@code docs/09} §8 prescribes exactly this
 *       call. The build therefore emits a deprecation note for this class; a cheatsheet update should
 *       record the flag.</li>
 * </ul>
 */
public class TextModel {

    /**
     * Baked geometry: nine floats per vertex -- {@code x, y, z, u, v, nx, ny, nz, shade}. Four
     * consecutive vertices form one face. {@code null} until the background bake publishes it.
     */
    private float[] vertices;

    /**
     * Filled bitmap pixels, packed as {@code (x << 32) | y}. 1.12.2 pushed a {@code RawQuadCube} into a
     * {@code RawQuadGroup} here; a set also reproduces that stage's quad-level de-duplication.
     */
    private final LinkedHashSet<Long> pixels = new LinkedHashSet<>();

    /** Depth of every pixel cube in 1/16 units; the 1.12.2 code passed it to {@code drawPixel}. */
    private int thick;

    /** Colour the model was built with; 1.12.2 handed it to the bake closure ({@code group.setColor}). */
    private int color = 0xFFFFFF;

    private final ReentrantLock lock = new ReentrantLock();

    private final float LX = -8, LY = 7;

    private void drawPixel(int x, int y, int thick) {
        // 1.12.2: group.add(new RawQuadCube(0.0625F, 0.0625F, 0.0625F * thick, sprite)
        //                          .translateCoord((x + LX - 8) / 16.0F, (y + LY - 16) / 16.0F, -0.5F));
        pixels.add(pack(x, y));
        this.thick = thick;
    }

    private static long pack(int x, int y) {
        return (((long) x) << 32) | (((long) y) & 0xFFFFFFFFL);
    }

    private static int unpackX(long key) {
        return (int) (key >> 32);
    }

    private static int unpackY(long key) {
        return (int) key;
    }

    private boolean filled(int x, int y) {
        return pixels.contains(pack(x, y));
    }

    private void drawChar(byte[] font, int x, int thick, int first, int second) {
        int offset, base;
        if (first < 0xA1) {
            for (int i = 0; i < 32; i++) {
                for (int j = 0; j < 2; j++) {
                    offset = first * 64 + i * 2 + j;
                    for (int k = 0; k < 8; k++) {
                        if ((font[offset] & (0x80 >> k)) > 0) {
                            drawPixel(x + j * 8 + k, i, thick);
                        }
                    }
                }
            }
        } else {
            base = (first - 0xA1) * 0x5E + (second - 0xA1);
            for (int i = 0; i < 32; i++) {
                for (int j = 0; j < 4; j++) {
                    offset = base * 128 + i * 4 + j;
                    for (int k = 0; k < 8; k++) {
                        if ((font[offset] & (0x80 >> k)) > 0) {
                            drawPixel(x + j * 8 + k, i, thick);
                        }
                    }
                }
            }
        }
    }

    private void drawVerticalChar(byte[] font, int y, int thick, int first, int second) {
        int offset, base;
        if (first < 0xA1) {
            for (int i = 0; i < 32; i++) {
                for (int j = 0; j < 2; j++) {
                    offset = first * 64 + i * 2 + j;
                    for (int k = 0; k < 8; k++) {
                        if ((font[offset] & (0x80 >> k)) > 0) {
                            drawPixel(8 + j * 8 + k, y + i, thick);
                        }
                    }
                }
            }
        } else {
            base = (first - 0xA1) * 0x5E + (second - 0xA1);
            for (int i = 0; i < 32; i++) {
                for (int j = 0; j < 4; j++) {
                    offset = base * 128 + i * 4 + j;
                    for (int k = 0; k < 8; k++) {
                        if ((font[offset] & (0x80 >> k)) > 0) {
                            drawPixel(j * 8 + k, y + i, thick);
                        }
                    }
                }
            }
        }
    }

    private void drawString(byte[] font, int x, int thick, byte[] str) {
        int count = 0;
        for (int i = 0; i < str.length; i++) {
            if ((str[i] & 0xFF) < 0xA1) {
                drawChar(FontLoader.ASCII, x + count * 16, thick, str[i] & 0xFF, 0x00);
                count += 1;
            } else {
                drawChar(font, x + count * 16, thick, str[i] & 0xFF, str[i + 1] & 0xFF);
                count += 2;
                i += 1;
            }
        }
    }

    private void drawVerticalString(byte[] font, int thick, byte[] str) {
        int count = 0;
        for (int i = 0; i < str.length; i++) {
            if ((str[i] & 0xFF) < 0xA1) {
                drawVerticalChar(FontLoader.ASCII, count * 32, thick, str[i] & 0xFF, 0x00);
                count += 1;
            } else {
                drawVerticalChar(font, count * 32, thick, str[i] & 0xFF, str[i + 1] & 0xFF);
                count += 1;
                i += 1;
            }
        }
    }

    public TextModel(byte[] font, int align, String str, int thick, int color) {
        byte[] buf;
        try {
            buf = str.getBytes("GB2312");
        } catch (Exception e) {
            NyaSamaOptics.LOGGER.error(e.getMessage());
            buf = str.getBytes();
        }

        switch (align) {
            case FontLoader.ALIGN_CENTER:
                drawString(font, -(buf.length - 2) * 8, thick, buf);
                break;
            case FontLoader.ALIGN_LEFT:
                drawString(font, 0, thick, buf);
                break;
            case FontLoader.ALIGN_RIGHT:
                drawString(font, -(buf.length - 1) * 16, thick, buf);
                break;
            case FontLoader.ALIGN_UP:
                drawVerticalString(font, thick, buf);
                break;
            case FontLoader.ALIGN_DOWN:
                drawVerticalString(font, thick, buf);
                break;
        }

        this.color = color & 0xFFFFFF;

        TextureAtlasSprite sprite = atlasSprite();
        float u0 = sprite == null ? 0.0F : sprite.getU0();
        float u1 = sprite == null ? 0.0F : sprite.getU1();
        float v0 = sprite == null ? 0.0F : sprite.getV0();
        float v1 = sprite == null ? 0.0F : sprite.getV1();

        // 1.12.2: quads.clear();
        vertices = null;

        executor.schedule(() -> {
            lock.lock();
            try {
                // 1.12.2: group.setColor(color); group.bake(quads); cullFace(quads);
                vertices = bake(u0, v0, u1, v1);
            } finally {
                // The 1.12.2 lambda had no try/finally; an exception would have left the model locked
                // forever. Behaviour is otherwise identical.
                lock.unlock();
            }
        }, 10, TimeUnit.MILLISECONDS);
    }

    /**
     * The background bake pool.
     *
     * <p>1.12.2 borrowed the shared {@code NyaSamaLib.EXECUTOR}. That pool does not exist in v3 -- see
     * {@code docs/09-NEOFORGE-API-CHEATSHEET.md} §10, which replaced it with {@code NSWorkQueue} for
     * main-thread work and left background evaluation where it was. Baking only produces this model's
     * own vertex buffer and never touches the world, so it stays a background job; a single daemon
     * thread also keeps two bakes from racing on the same atlas look-up.</p>
     */
    public static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, runnable -> {
        Thread thread = new Thread(runnable, "NyaSamaOptics-TextModel");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * 1.12.2 drew every pixel cube with {@code blocks/concrete_white}; the 1.13+ name of that vanilla
     * texture is {@code block/white_concrete}.
     */
    private static TextureAtlasSprite atlasSprite() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return null;
        }
        return minecraft.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(ResourceLocation.withDefaultNamespace("block/white_concrete"));
    }

    /** Number of faces the bake will emit, so the float buffer can be sized exactly. */
    private int countFaces() {
        int faces = 0;
        for (long key : pixels) {
            int x = unpackX(key);
            int y = unpackY(key);
            faces += 6;
            if (filled(x, y - 1)) faces--;
            if (filled(x, y + 1)) faces--;
            if (filled(x - 1, y)) faces--;
            if (filled(x + 1, y)) faces--;
        }
        return faces;
    }

    /**
     * Turns the recorded pixels into the vertex buffer. The face order, corner order, UV corners and
     * normals are those of {@code cn.ac.nya.rawmdl.RawQuadCube#bake}, and the shade factors are the six
     * direction factors 1.12.2 multiplied in with {@code LightUtil.diffuseLight}.
     */
    private float[] bake(float u0, float v0, float u1, float v1) {
        float[] data = new float[countFaces() * 4 * 9];
        int slot = 0;

        for (long key : pixels) {
            int px = unpackX(key);
            int py = unpackY(key);

            float tx = (px + LX - 8) / 16.0F;
            float ty = (py + LY - 16) / 16.0F;
            float tz = -0.5F;

            // RawQuadBase: the cube is centred in X and Z and grows upwards from Y = 0.
            float x = 0.0625F / 2.0F;
            float z = (0.0625F * thick) / 2.0F;
            float y = 0.0625F;

            // Corners 0..7 in RawQuadBase order: 0..3 on the top face, 4..7 on the bottom.
            float[] corners = new float[] {
                    tx + x, ty + y, tz + z,
                    tx + x, ty + y, tz - z,
                    tx - x, ty + y, tz - z,
                    tx - x, ty + y, tz + z,
                    tx + x, ty, tz + z,
                    tx + x, ty, tz - z,
                    tx - x, ty, tz - z,
                    tx - x, ty, tz + z,
            };

            // Down - Yneg (1.12.2: vertexes 7,6,5,4 with (uMin,vMin)...(uMax,vMin), shade 0.5)
            if (!filled(px, py - 1)) {
                slot = put(data, slot, corners, 7, u0, v0, 0.0F, -1.0F, 0.0F, 0.5F);
                slot = put(data, slot, corners, 6, u0, v1, 0.0F, -1.0F, 0.0F, 0.5F);
                slot = put(data, slot, corners, 5, u1, v1, 0.0F, -1.0F, 0.0F, 0.5F);
                slot = put(data, slot, corners, 4, u1, v0, 0.0F, -1.0F, 0.0F, 0.5F);
            }

            // Up - Ypos (vertexes 0,1,2,3, shade 1.0)
            if (!filled(px, py + 1)) {
                slot = put(data, slot, corners, 0, u1, v1, 0.0F, 1.0F, 0.0F, 1.0F);
                slot = put(data, slot, corners, 1, u1, v0, 0.0F, 1.0F, 0.0F, 1.0F);
                slot = put(data, slot, corners, 2, u0, v0, 0.0F, 1.0F, 0.0F, 1.0F);
                slot = put(data, slot, corners, 3, u0, v1, 0.0F, 1.0F, 0.0F, 1.0F);
            }

            // North - Zneg (vertexes 2,1,5,6, shade 0.8)
            slot = put(data, slot, corners, 2, u1, v0, 0.0F, 0.0F, -1.0F, 0.8F);
            slot = put(data, slot, corners, 1, u0, v0, 0.0F, 0.0F, -1.0F, 0.8F);
            slot = put(data, slot, corners, 5, u0, v1, 0.0F, 0.0F, -1.0F, 0.8F);
            slot = put(data, slot, corners, 6, u1, v1, 0.0F, 0.0F, -1.0F, 0.8F);

            // South - Zpos (vertexes 3,7,4,0, shade 0.8)
            slot = put(data, slot, corners, 3, u0, v0, 0.0F, 0.0F, 1.0F, 0.8F);
            slot = put(data, slot, corners, 7, u0, v1, 0.0F, 0.0F, 1.0F, 0.8F);
            slot = put(data, slot, corners, 4, u1, v1, 0.0F, 0.0F, 1.0F, 0.8F);
            slot = put(data, slot, corners, 0, u1, v0, 0.0F, 0.0F, 1.0F, 0.8F);

            // West - Xneg (vertexes 3,2,6,7, shade 0.6)
            if (!filled(px - 1, py)) {
                slot = put(data, slot, corners, 3, u1, v0, -1.0F, 0.0F, 0.0F, 0.6F);
                slot = put(data, slot, corners, 2, u0, v0, -1.0F, 0.0F, 0.0F, 0.6F);
                slot = put(data, slot, corners, 6, u0, v1, -1.0F, 0.0F, 0.0F, 0.6F);
                slot = put(data, slot, corners, 7, u1, v1, -1.0F, 0.0F, 0.0F, 0.6F);
            }

            // East - Xpos (vertexes 1,0,4,5, shade 0.6)
            if (!filled(px + 1, py)) {
                slot = put(data, slot, corners, 1, u1, v0, 1.0F, 0.0F, 0.0F, 0.6F);
                slot = put(data, slot, corners, 0, u0, v0, 1.0F, 0.0F, 0.0F, 0.6F);
                slot = put(data, slot, corners, 4, u0, v1, 1.0F, 0.0F, 0.0F, 0.6F);
                slot = put(data, slot, corners, 5, u1, v1, 1.0F, 0.0F, 0.0F, 0.6F);
            }
        }

        return data;
    }

    /** Writes one vertex; {@code corner} indexes {@code corners} with a stride of three. */
    private static int put(float[] data, int slot, float[] corners, int corner,
                           float u, float v, float nx, float ny, float nz, float shade) {
        int i = slot * 9;
        int c = corner * 3;
        data[i] = corners[c];
        data[i + 1] = corners[c + 1];
        data[i + 2] = corners[c + 2];
        data[i + 3] = u;
        data[i + 4] = v;
        data[i + 5] = nx;
        data[i + 6] = ny;
        data[i + 7] = nz;
        data[i + 8] = shade;
        return slot + 1;
    }

    /**
     * Draws the baked text in the current pose.
     *
     * <p>Replaces 1.12.2's {@code render(TileEntity)} / {@code render(World, BlockPos, BufferBuilder)}:
     * the light of the block came from {@code world.getCombinedLight(pos, 1)} then and arrives as
     * {@code packedLight} now, and the {@code Tessellator}/{@code BufferBuilder} pair becomes a
     * {@link MultiBufferSource}. 1.12.2 rendered with culling switched off
     * ({@code RendererHelper.beginSpecialLighting} called {@code glDisable(GL_CULL_FACE)}), which is
     * what {@code entityCutoutNoCull} expresses here; blending was on but the sprite is opaque, so the
     * look is unchanged.</p>
     */
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!lock.tryLock()) {
            // 1.12.2 rendered nothing for this model while the background bake held the lock.
            return;
        }
        try {
            float[] data = vertices;
            if (data == null) {
                return;
            }

            int red = (color >> 16) & 0xFF;
            int green = (color >> 8) & 0xFF;
            int blue = color & 0xFF;

            VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
            PoseStack.Pose pose = poseStack.last();
            for (int i = 0; i < data.length; i += 9) {
                float shade = data[i + 8];
                buffer.addVertex(pose, data[i], data[i + 1], data[i + 2])
                        .setColor((int) (red * shade), (int) (green * shade), (int) (blue * shade), 255)
                        .setUv(data[i + 3], data[i + 4])
                        .setLight(packedLight)
                        .setOverlay(packedOverlay)
                        .setNormal(pose, data[i + 5], data[i + 6], data[i + 7]);
            }
        } finally {
            lock.unlock();
        }
    }

}
