package club.nsdn.nyasamaoptics.util.font;

import club.nsdn.nyasamaoptics.NyaSamaOptics;
import cn.ac.nya.rawmdl.RawQuadCube;
import cn.ac.nya.rawmdl.RawQuadGroup;
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

/**
 * Created by drzzm32 on 2019.1.30.
 */
public class TextModel {

    private LinkedList<BakedQuad> quads = new LinkedList<>();
    private RawQuadGroup group = new RawQuadGroup();
    private static TextureAtlasSprite sprite =
            Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("blocks/concrete_white");

    private final float LX = -8, LY = 7;

    private void drawPixel(int x, int y, int thick) {
        group.add(
                new RawQuadCube(0.0625F, 0.0625F, 0.0625F * thick, sprite)
                        .translateCoord((x + LX - 8) / 16.0F, (y + LY - 16) / 16.0F, -0.5F)
        );
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
        group.setColor(color);
        group.bake(quads);
    }

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
    }

}
