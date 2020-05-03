package club.nsdn.nyasamaoptics.util.font;

import club.nsdn.nyasamaoptics.NyaSamaOptics;
import cn.ac.nya.NyaSamaLib;
import cn.ac.nya.mutable.MutableQuad;
import cn.ac.nya.mutable.MutableVertex;
import cn.ac.nya.rawmdl.RawQuadCube;
import cn.ac.nya.rawmdl.RawQuadGroup;
import com.google.common.primitives.Doubles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by drzzm32 on 2019.1.30.
 */
public class TextModel {

    private LinkedList<BakedQuad> quads = new LinkedList<>();
    private RawQuadGroup group = new RawQuadGroup();
    private static TextureAtlasSprite sprite =
            Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("blocks/concrete_white");

    private final ReentrantLock lock = new ReentrantLock();

    private final float LX = -8, LY = 7;

    private void drawPixel(int x, int y, int thick) {
        group.add(
                new RawQuadCube(0.0625F, 0.0625F, 0.0625F * thick, sprite)
                        .translateCoord((x + LX - 8) / 16.0F, (y + LY - 16) / 16.0F, -0.5F)
        );
    }

    public static double dist(MutableVertex a, MutableVertex b) {
        double x = a.position_x - b.position_x;
        double y = a.position_y - b.position_y;
        double z = a.position_z - b.position_z;
        return x * x + y * y + z * z;
    }

    public static boolean dist(MutableVertex v, MutableQuad q) {
        double a = dist(v, q.vertex_0), b = dist(v, q.vertex_1);
        double c = dist(v, q.vertex_2), d = dist(v, q.vertex_3);
        return Doubles.min(a, b, c, d) <= 0.0025;
    }

    public static boolean vertexEquals(BakedQuad a, BakedQuad b) {
        if (a == b) return true;
        MutableQuad x = new MutableQuad();
        MutableQuad y = new MutableQuad();
        x.fromBakedItem(a); y.fromBakedItem(b);

        return dist(x.vertex_0, y) && dist(x.vertex_1, y) &&
                dist(x.vertex_2, y) && dist(x.vertex_3, y);
    }

    public static void cullFace(LinkedList<BakedQuad> quads) {
        LinkedList<BakedQuad> remove = new LinkedList<>();

        for (BakedQuad left : quads)
            quads.listIterator(quads.indexOf(left)).forEachRemaining((right) -> {
                if (!left.equals(right)) {
                    if (vertexEquals(left, right)) {
                        if (!remove.contains(left))
                            remove.add(left);
                        if (left.getFace().equals(right.getFace().getOpposite())) {
                            if (!remove.contains(right))
                                remove.add(right);
                        }
                    }
                }
            });

        quads.removeAll(remove);
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
            NyaSamaOptics.logger.error(e.getMessage());
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

        quads.clear();

        executor.schedule(() -> {
            lock.lock();
            group.setColor(color);
            group.bake(quads);
            cullFace(quads);
            lock.unlock();
        }, 10, TimeUnit.MILLISECONDS);
    }

    public static ScheduledThreadPoolExecutor executor = NyaSamaLib.EXECUTOR;

    public void render(TileEntity te) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        buffer.setTranslation(0, 0, 0);
        render(te.getWorld(), te.getPos(), buffer);
        tessellator.draw();
    }

    public void render(World world, BlockPos pos, BufferBuilder buffer) {
        buffer.setTranslation(0, 0, 0);

        int i = world.getCombinedLight(pos, 1);
        if (lock.tryLock()) {
            for (BakedQuad quad: quads) {
                buffer.addVertexData(quad.getVertexData());
                buffer.putBrightness4(i, i, i, i);

                float diffuse = 1;
                if (quad.shouldApplyDiffuseLighting())
                    diffuse = net.minecraftforge.client.model.pipeline.LightUtil.diffuseLight(quad.getFace());

                buffer.putColorMultiplier(diffuse, diffuse, diffuse, 4);
                buffer.putColorMultiplier(diffuse, diffuse, diffuse, 3);
                buffer.putColorMultiplier(diffuse, diffuse, diffuse, 2);
                buffer.putColorMultiplier(diffuse, diffuse, diffuse, 1);

                buffer.putPosition(0, 0, 0);
            }
            lock.unlock();
        }
    }

}
