package club.nsdn.nyasamaoptics.Util.Font;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;

/**
 * Created by drzzm on 2017.1.7.
 */
public class TextModel extends ModelBase {
    private LinkedList<ModelRenderer> shapes;
    private int color;

    private final float LX = -8, LY = 7, RX = 7, RY = 23;

    private void drawPixel(int x, int y, int thick) {
        ModelRenderer shape = new ModelRenderer(this, 0, 0);
        shape.addBox(0F, 0F, -8F, 1, 1, thick);
        shape.setRotationPoint(x + LX - 8.0F, y + LY - 16.0F, 0F);
        shape.setTextureSize(1, 1);
        shape.mirror = true;
        setRotation(shape, 0F, 0F, 0F);
        shapes.add(shape);
    }

    private void drawChar(byte[] font, int x, int thick, int first, int second) {
        int offset, base;
        if (first < 0xA1) {
            for (int i = 0; i < 32; i++) {
                for (int j = 0; j < 4; j++) {
                    offset = first * 128 + i * 4 + j;
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

    private void drawString(byte[] font, int x, int thick, byte[] str) {
        int count = 0;
        for (int i = 0; i < str.length; i++) {
            if ((str[i] & 0xFF) < 0xA1) {
                drawChar(FontLoader.ASCII_Consolas, x + count * 32, thick, str[i] & 0xFF, 0x00);
                count += 1;
            } else {
                drawChar(font, x + count * 32, thick, str[i] & 0xFF, str[i + 1] & 0xFF);
                count += 1;
                i += 1;
            }
        }
    }

    public TextModel(byte[] font, int align, String str, int color, int thick) {
        shapes = new LinkedList<ModelRenderer>();
        this.color = color;

        textureWidth = 1;
        textureHeight = 1;

        String gbStr;
        try {
            gbStr = new String(str.getBytes("utf-8"), "gb2312");
        } catch (Exception e) {
            System.out.println(e.toString());
            gbStr = str;
        }

        switch (align) {
            case FontLoader.ALIGN_CENTER:
                drawString(font, -(gbStr.length() - 1) * 16, thick, gbStr.getBytes());
                break;
            case FontLoader.ALIGN_LEFT:
                drawString(font, 0, thick, gbStr.getBytes());
                break;
            case FontLoader.ALIGN_RIGHT:
                drawString(font, -(gbStr.length() - 1) * 32, thick, gbStr.getBytes());
                break;
        }

    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(entity, f, f1, f2, f3, f4, f5);
        GL11.glColor3b((byte)((color & 0xFF0000) >> 16), (byte)((color & 0x00FF00) >> 8), (byte)(color & 0x0000FF));
        for (ModelRenderer i : shapes) {
            i.render(f5);
        }
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public void setRotationAngles(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    }
}
