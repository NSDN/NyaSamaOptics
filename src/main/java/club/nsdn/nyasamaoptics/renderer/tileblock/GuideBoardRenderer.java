package club.nsdn.nyasamaoptics.renderer.tileblock;

import club.nsdn.nyasamaoptics.tileblock.deco.TileEntityDecoText;
import club.nsdn.nyasamaoptics.tileblock.screen.LEDPlate;
import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.render.RendererHelper;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

/**
 * Created by drzzm32 on 2019.12.24.
 */
public class GuideBoardRenderer extends AbsTileEntitySpecialRenderer {

    private final ResourceLocation _text = new ResourceLocation("nyasamaoptics", "textures/blocks/white.png");

    public GuideBoardRenderer() {
    }

    @Override
    public boolean isGlobalRenderer(TileEntity tileEntity) {
        return true;
    }

    @Override
    public void render(@Nonnull TileEntityBase te, double x, double y, double z, float partialTicks, int destroyStage, float partial) {
        int meta = te.getBlockMetadata();
        if ((meta % 2) == 0) meta = 2 - meta;

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y  + 0.5F, (float) z + 0.5F);

        GL11.glPushMatrix();
        float angle = (meta & 0x3) * 90.0F;
        GL11.glRotatef(angle, 0.0F, 1.0F, 0.0F);

        GL11.glPushMatrix();
        GL11.glRotatef(90.0F, -1.0F, 0.0F, 0.0F);

        if (te instanceof TileEntityDecoText) {
            TileEntityDecoText text = (TileEntityDecoText) te;

            RendererHelper.beginSpecialLighting();
            GL11.glColor3f(1.0F, 1.0F, 1.0F);

            boolean control = true;
            if (text.getSender() != null) control = text.isEnabled;

            if (control) {
                // render holo text
                text.createModel();

                final float offset = -0.125F;

                GL11.glPushMatrix();
                GL11.glTranslated(0, -offset, 0);
                GL11.glTranslated(text.offsetX, text.offsetY, text.offsetZ);

                GL11.glPushMatrix();
                {
                    GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
                    GL11.glPushMatrix();
                    {
                        Minecraft.getMinecraft().renderEngine.bindTexture(_text);
                        GL11.glTranslated(0.0, -text.scale, (double) (16 - text.thick) / 32.0);
                        GL11.glPushMatrix();
                        {
                            GL11.glScaled(text.scale, text.scale, 1.0);
                            if (text.model != null) {
                                GL11.glPushMatrix();
                                GL11.glTranslated(0, 0.5, 0);
                                text.model.render(te);
                                GL11.glPopMatrix();
                            }
                        }
                        GL11.glPopMatrix();
                    }
                    GL11.glPopMatrix();
                }
                GL11.glPopMatrix();

                GL11.glPopMatrix();

                GL11.glColor3f(1.0F, 1.0F, 1.0F);

                // render sub text
                int fontColor = text.subColor, align = text.subAlign;
                double fontScale = text.subScale;
                String content = text.subContent;
                GL11.glPushMatrix();
                GL11.glTranslated(text.subOffsetX, text.subOffsetY, text.subOffsetZ);
                GL11.glPushMatrix();
                GL11.glRotatef(-90.0F,1.0F, 0.0F, 0.0F);
                GL11.glPushMatrix();
                GL11.glRotatef(180.0F,0.0F, 1.0F, 0.0F);
                GL11.glPushMatrix();
                GL11.glTranslatef(0.0F, 0.0F, offset - 0.01F);
                renderString(content, fontScale, align, fontColor);
                GL11.glPopMatrix();
                GL11.glPopMatrix();
                GL11.glPopMatrix();
                GL11.glPopMatrix();
            }
        }

        GL11.glPopMatrix();

        GL11.glPopMatrix();

        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        RendererHelper.endSpecialLighting();

        GL11.glPopMatrix();
    }

    private void renderString(String str, double scale, int align, int color) {
        FontRenderer renderer = Minecraft.getMinecraft().fontRenderer;
        GL11.glPushMatrix();
        GL11.glScaled(scale, scale, 1.0);
        GL11.glPushMatrix();
        GL11.glScalef(0.02F, 0.02F, 1.0F);
        GL11.glPushMatrix();
        String[] lines = str.split("\n");
        GL11.glTranslatef(0.0F, -(float) lines.length / 2.0F * renderer.FONT_HEIGHT, 0.0F);
        int x = 0, y = 0;
        for (String s : lines) {
            switch (align) {
                case LEDPlate.ALIGN_CENTER:
                    x = -renderer.getStringWidth(s) / 2; break;
                case LEDPlate.ALIGN_LEFT:
                    x = 0; break;
                case LEDPlate.ALIGN_RIGHT:
                    x = -renderer.getStringWidth(s); break;
            }
            renderer.drawString(s, x, y, color);
            y += renderer.FONT_HEIGHT;
        }
        GL11.glPopMatrix();
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }

}
