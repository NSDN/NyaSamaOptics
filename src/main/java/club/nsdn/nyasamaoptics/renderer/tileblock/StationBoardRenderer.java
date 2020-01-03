package club.nsdn.nyasamaoptics.renderer.tileblock;

import club.nsdn.nyasamaoptics.tileblock.deco.StationBoard;
import club.nsdn.nyasamaoptics.tileblock.deco.TileEntityDecoText;
import club.nsdn.nyasamaoptics.tileblock.screen.LEDPlate;
import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.render.RendererHelper;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import cn.ac.nya.forgeobj.WavefrontObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

/**
 * Created by drzzm32 on 2020.1.3.
 */
public class StationBoardRenderer extends AbsTileEntitySpecialRenderer {

    private final WavefrontObject modelBase = new WavefrontObject(
            new ResourceLocation("nyasamaoptics", "models/blocks/" + "station_board.obj")
    );

    private final ResourceLocation textureBase = new ResourceLocation("nyasamaoptics", "textures/blocks/light_base.png");

    private final ResourceLocation _text = new ResourceLocation("nyasamaoptics", "textures/blocks/white.png");

    public StationBoardRenderer() {
    }

    @Override
    public boolean isGlobalRenderer(TileEntity tileEntity) {
        return true;
    }

    @Override
    public void render(@Nonnull TileEntityBase te, double x, double y, double z, float partialTicks, int destroyStage, float partial) {
        int meta = te.META;

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y  + 0.5F, (float) z + 0.5F);

        //RendererHelper.beginSpecialLighting();

        GL11.glPushMatrix();
        float angle = (meta % 4) * 90.0F;
        GL11.glRotatef(angle, 0.0F, -1.0F, 0.0F);

        if (te instanceof StationBoard.TileEntityStationBoard) {
            StationBoard.TileEntityStationBoard board = (StationBoard.TileEntityStationBoard) te;

            RendererHelper.beginSpecialLighting();

            int backColor = board.optionalBack;
            GL11.glColor3f(((backColor & 0xFF0000) >> 16) / 255.0F, ((backColor & 0x00FF00) >> 8) / 255.0F, (backColor & 0x0000FF) / 255.0F);
            if (!board.isEnabled && board.getSender() != null) {
                GL11.glColor3f(0.33F, 0.33F, 0.33F);
            }
            RendererHelper.renderPartWithResource(modelBase, "base", textureBase);
            GL11.glColor3f(1.0F, 1.0F, 1.0F);

            // render holo text
            board.createModel();

            final float offset = 0.125F;

            GL11.glPushMatrix();
            GL11.glRotatef(90.0F, -1.0F, 0.0F, 0.0F);
            GL11.glPushMatrix();
            GL11.glTranslated(0, offset, 0);
            GL11.glTranslated(board.offsetX, board.offsetY, board.offsetZ);
            {
                GL11.glPushMatrix();
                {
                    GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
                    GL11.glPushMatrix();
                    {
                        Minecraft.getMinecraft().renderEngine.bindTexture(_text);
                        GL11.glTranslated(0.0, -board.scale, (double) (16 - board.thick) / 32.0);
                        GL11.glPushMatrix();
                        {
                            GL11.glScaled(board.scale, board.scale, 1.0);
                            if (board.model != null) {
                                int color = board.color;
                                GL11.glColor3f(
                                        ((color & 0xFF0000) >> 16) / 255.0F,
                                        ((color & 0x00FF00) >> 8) / 255.0F,
                                        (color & 0x0000FF) / 255.0F
                                );
                                GL11.glPushMatrix();
                                GL11.glTranslated(0, 0.5, 0);
                                board.model.render((Entity) null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
                                GL11.glPopMatrix();
                            }
                        }
                        GL11.glPopMatrix();
                    }
                    GL11.glPopMatrix();
                }
                GL11.glPopMatrix();
            }
            GL11.glPopMatrix();
            GL11.glPopMatrix();

            GL11.glColor3f(1.0F, 1.0F, 1.0F);

            // render sub text
            int fontColor = board.subColor, align = board.subAlign;
            double fontScale = board.subScale;
            String content = board.subContent;
            GL11.glPushMatrix();
            GL11.glRotatef(90.0F, -1.0F, 0.0F, 0.0F);
            GL11.glPushMatrix();
            GL11.glTranslated(board.subOffsetX, board.subOffsetY, board.subOffsetZ);
            GL11.glPushMatrix();
            GL11.glRotatef(-90.0F,1.0F, 0.0F, 0.0F);
            GL11.glPushMatrix();
            GL11.glRotatef(180.0F,0.0F, 1.0F, 0.0F);
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, 0.0F, -offset - 0.01F);
            renderString(content, fontScale, align, fontColor);
            GL11.glPopMatrix();
            GL11.glPopMatrix();
            GL11.glPopMatrix();
            GL11.glPopMatrix();
            GL11.glPopMatrix();

            // render labels
            if (board.state1) {
                GL11.glColor3f(1.0F, 1.0F, 1.0F);
                RendererHelper.renderPartWithResource(modelBase, "1", textureBase);
                fontColor = board.color1; align = LEDPlate.ALIGN_CENTER;
                fontScale = board.scale1;
                content = board.label1;
                GL11.glPushMatrix();
                GL11.glRotatef(90.0F, -1.0F, 0.0F, 0.0F);
                GL11.glPushMatrix();
                GL11.glTranslated(0.25, 0, 0.125);
                GL11.glPushMatrix();
                GL11.glRotatef(-90.0F,1.0F, 0.0F, 0.0F);
                GL11.glPushMatrix();
                GL11.glRotatef(180.0F,0.0F, 1.0F, 0.0F);
                GL11.glPushMatrix();
                GL11.glTranslatef(0.0F, 0.0F, -offset - 0.01F);
                renderString(content, fontScale, align, fontColor);
                GL11.glPopMatrix();
                GL11.glPopMatrix();
                GL11.glPopMatrix();
                GL11.glPopMatrix();
                GL11.glPopMatrix();
            }
            if (board.state2) {
                GL11.glColor3f(1.0F, 1.0F, 1.0F);
                RendererHelper.renderPartWithResource(modelBase, "2", textureBase);
                fontColor = board.color2; align = LEDPlate.ALIGN_CENTER;
                fontScale = board.scale2;
                content = board.label2;
                GL11.glPushMatrix();
                GL11.glRotatef(90.0F, -1.0F, 0.0F, 0.0F);
                GL11.glPushMatrix();
                GL11.glTranslated(-0.25, 0, 0.125);
                GL11.glPushMatrix();
                GL11.glRotatef(-90.0F,1.0F, 0.0F, 0.0F);
                GL11.glPushMatrix();
                GL11.glRotatef(180.0F,0.0F, 1.0F, 0.0F);
                GL11.glPushMatrix();
                GL11.glTranslatef(0.0F, 0.0F, -offset - 0.01F);
                renderString(content, fontScale, align, fontColor);
                GL11.glPopMatrix();
                GL11.glPopMatrix();
                GL11.glPopMatrix();
                GL11.glPopMatrix();
                GL11.glPopMatrix();
            }
        }

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
