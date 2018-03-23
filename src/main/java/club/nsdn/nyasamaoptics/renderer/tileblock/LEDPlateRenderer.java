package club.nsdn.nyasamaoptics.renderer.tileblock;

import club.nsdn.nyasamaoptics.renderer.RendererHelper;
import club.nsdn.nyasamaoptics.tileblock.screen.LEDPlate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.WavefrontObject;
import org.lwjgl.opengl.GL11;

/**
 * Created by drzzm32 on 2018.3.22.
 */
public class LEDPlateRenderer extends TileEntitySpecialRenderer {

    private final WavefrontObject modelShell = new WavefrontObject(
            new ResourceLocation("nyasamaoptics", "models/blocks/" + "led_plate" + "_base.obj")
    );
    private final WavefrontObject modelLight = new WavefrontObject(
            new ResourceLocation("nyasamaoptics", "models/blocks/" + "led_plate" + "_light.obj")
    );

    private final ResourceLocation textureShell = new ResourceLocation("nyasamaoptics", "textures/blocks/light_shell.png");
    private final ResourceLocation textureLight = new ResourceLocation("nyasamaoptics", "textures/blocks/light_base.png");

    public LEDPlateRenderer() {
    }

    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y  + 0.5F, (float) z + 0.5F);

        RenderHelper.disableStandardItemLighting();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        } else {
            GL11.glShadeModel(GL11.GL_FLAT);
        }

        Tessellator.instance.setColorOpaque_F(1.0F, 1.0F, 1.0F);

        GL11.glPushMatrix();

        int meta = te.getWorldObj().getBlockMetadata(te.xCoord, te.yCoord, te.zCoord);

        float angle = ((meta - 1) % 4) * 90.0F;
        GL11.glRotatef(angle, 0.0F, -1.0F, 0.0F);

        if (te.getBlockType() instanceof LEDPlate) {
            GL11.glPushMatrix();

            switch ((meta - 1) / 4) {
                case 1:
                    GL11.glRotatef(90.0F, -1.0F, 0.0F, 0.0F);
                    break;
                case 2:
                    GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                    break;
            }

            Tessellator.instance.setBrightness(32);
            RendererHelper.renderWithResource(modelShell, textureShell);

            if (te instanceof LEDPlate.TilePlate) {
                LEDPlate.TilePlate tilePlate = (LEDPlate.TilePlate) te;
                int backColor = tilePlate.back;
                GL11.glColor3f(((backColor & 0xFF0000) >> 16) / 255.0F, ((backColor & 0x00FF00) >> 8) / 255.0F, (backColor & 0x0000FF) / 255.0F);
                Tessellator.instance.setBrightness(255);
                if (!tilePlate.isEnabled && tilePlate.getSender() != null) {
                    GL11.glColor3f(0.33F, 0.33F, 0.33F);
                    Tessellator.instance.setBrightness(32);
                }
                RendererHelper.renderWithResource(modelLight, textureLight);
                GL11.glColor3f(1.0F, 1.0F, 1.0F);

                int fontColor = tilePlate.color, align = tilePlate.align;
                double fontScale = tilePlate.scale; String content = tilePlate.content;
                GL11.glPushMatrix();
                GL11.glRotatef( -90.0F,1.0F, 0.0F, 0.0F);
                GL11.glPushMatrix();
                GL11.glRotatef( 180.0F,0.0F, 1.0F, 0.0F);
                GL11.glPushMatrix();
                GL11.glTranslatef(0.0F, 0.0F, 0.375F - 0.01F);
                renderString(content, fontScale, align, fontColor);
                GL11.glPopMatrix();
                GL11.glPopMatrix();
                GL11.glPopMatrix();
            }

            GL11.glPopMatrix();
        }

        GL11.glPopMatrix();

        RenderHelper.enableStandardItemLighting();

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
