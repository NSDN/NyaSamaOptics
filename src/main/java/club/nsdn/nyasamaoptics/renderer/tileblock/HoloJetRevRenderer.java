package club.nsdn.nyasamaoptics.renderer.tileblock;

import club.nsdn.nyasamaoptics.renderer.RendererHelper;
import club.nsdn.nyasamaoptics.tileblock.light.HoloJetRev;
import club.nsdn.nyasamaoptics.util.font.FontLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.WavefrontObject;
import org.lwjgl.opengl.GL11;

/**
 * Created by drzzm on 2017.11.3.
 */
public class HoloJetRevRenderer extends TileEntitySpecialRenderer {

    private World world;
    private ResourceLocation baseTexture, jetTexture, text;
    private WavefrontObject baseModel, jetModel;

    public HoloJetRevRenderer() {
        world = Minecraft.getMinecraft().theWorld;
        baseTexture = new ResourceLocation("nyasamaoptics", "textures/blocks/light_shell.png");
        jetTexture = new ResourceLocation("nyasamaoptics", "textures/blocks/light_base.png");
        text = new ResourceLocation("nyasamaoptics", "textures/blocks/white.png");
        baseModel = new WavefrontObject(
                new ResourceLocation("nyasamaoptics", "models/blocks/" + "holo_jet_rev" + "_base.obj")
        );
        jetModel = new WavefrontObject(
                new ResourceLocation("nyasamaoptics", "models/blocks/" + "holo_jet_rev" + "_light.obj")
        );
    }

    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float scale) {
        if (!(tileEntity instanceof HoloJetRev.TileText)) return;
        HoloJetRev.TileText tileText = (HoloJetRev.TileText) tileEntity;
        GL11.glPushMatrix();
        {
            GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);

            tileText.createModel();

            RenderHelper.disableStandardItemLighting();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_CULL_FACE);

            if (Minecraft.isAmbientOcclusionEnabled())
            {
                GL11.glShadeModel(GL11.GL_SMOOTH);
            }
            else
            {
                GL11.glShadeModel(GL11.GL_FLAT);
            }

            Tessellator.instance.setColorOpaque_F(1.0F, 1.0F, 1.0F);

            GL11.glColor3f(1.0F, 1.0F, 1.0F);
            int meta = tileText.getBlockMetadata();
            float angle = ((meta - 1) % 4) * 90.0F;
            GL11.glPushMatrix();
            {
                GL11.glRotatef(angle, 0.0F, -1.0F, 0.0F);
                GL11.glPushMatrix();
                {
                    switch ((meta - 1) / 4) {
                        case 1:
                            GL11.glRotatef(90.0F, -1.0F, 0.0F, 0.0F);
                            break;
                        case 2:
                            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                            break;
                    }
                    RendererHelper.renderWithResource(baseModel, baseTexture);
                    RendererHelper.renderWithResource(jetModel, jetTexture);
                }
                GL11.glPopMatrix();
            }
            GL11.glPopMatrix();

            GL11.glPushMatrix();
            {
                GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(angle, 0.0F, 1.0F, 0.0F);

                Minecraft.getMinecraft().renderEngine.bindTexture(text);
                GL11.glPushMatrix();
                {
                    GL11.glTranslatef(0.0F, -1.5F, 0.0F);
                    GL11.glPushMatrix();
                    {
                        if (tileText.align == FontLoader.ALIGN_UP)
                            GL11.glTranslated(0.0, 0.5 - 2 * tileText.scale * (tileText.content.length() - 1), (double) (16 - tileText.thick) / 32.0);
                        else if (tileText.align == FontLoader.ALIGN_DOWN)
                            GL11.glTranslated(0.0, 2.5 - tileText.scale, (double) (16 - tileText.thick) / 32.0);
                        else
                            GL11.glTranslated(0.0, 1.5 - tileText.scale, (double) (16 - tileText.thick) / 32.0);
                        GL11.glPushMatrix();
                        {
                            GL11.glScaled(tileText.scale, tileText.scale, 1.0);
                            boolean control = true;
                            if (tileText.getSender() != null) control = tileText.isEnabled;
                            if (tileText.model != null && control)
                                tileText.model.render((Entity) null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
                        }
                        GL11.glPopMatrix();
                    }
                    GL11.glPopMatrix();
                }
                GL11.glPopMatrix();
            }
            GL11.glPopMatrix();

            RenderHelper.enableStandardItemLighting();
        }
        GL11.glPopMatrix();
    }

}
