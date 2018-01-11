package club.nsdn.nyasamaoptics.renderer.tileblock;

import club.nsdn.nyasamaoptics.tileblock.light.HoloJet;
import club.nsdn.nyasamaoptics.util.font.FontLoader;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

import org.lwjgl.opengl.GL11;

/**
 * Created by drzzm on 2017.1.7.
 */
public class HoloJetRenderer extends TileEntitySpecialRenderer {

    private World world;
    private ResourceLocation holoJet, text;
    private ModelBase holoJetModel;

    public HoloJetRenderer() {
        world = Minecraft.getMinecraft().theWorld;
        holoJet = new ResourceLocation("nyasamaoptics", "textures/blocks/BrushedAluminum.png");
        text = new ResourceLocation("nyasamaoptics", "textures/blocks/white.png");
        holoJetModel = new HoloJetModel();
    }

    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float scale) {
        if (!(tileEntity instanceof HoloJet.TileText)) return;
        HoloJet.TileText tileText = (HoloJet.TileText) tileEntity;
        GL11.glPushMatrix();
        {
            GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);

            Tessellator.instance.setColorOpaque_F(1.0F, 1.0F, 1.0F);

            if (tileText.model == null) {
                tileText.createModel();
            }

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

            GL11.glPushMatrix();
            {
                GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

                int rotation;
                switch (tileText.getBlockMetadata() % 13) {
                    case 1:
                        rotation = 0;
                        GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
                        break;
                    case 2:
                        rotation = 90;
                        GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
                        break;
                    case 3:
                        rotation = 180;
                        GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
                        break;
                    case 4:
                        rotation = 270;
                        GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
                        break;
                }

                Minecraft.getMinecraft().renderEngine.bindTexture(holoJet);
                GL11.glColor3f(1.0F, 1.0F, 1.0F);
                holoJetModel.render((Entity) null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);

                Minecraft.getMinecraft().renderEngine.bindTexture(text);

                GL11.glPushMatrix();
                {
                    GL11.glTranslatef(0.0F, -1.5F, 0.0F);
                    GL11.glPushMatrix();
                    {
                        if (tileText.align != FontLoader.ALIGN_UP)
                            GL11.glTranslated(0.0, 1.5 - tileText.scaleY, (double) (16 - tileText.thick) / 32.0);
                        else
                            GL11.glTranslated(0.0, 0.5 - 2 * tileText.scaleY * (tileText.content.length() - 1), (double) (16 - tileText.thick) / 32.0);
                        GL11.glPushMatrix();
                        {
                            GL11.glScaled(tileText.scaleX, tileText.scaleY, tileText.scaleZ);
                            if (tileText.model != null)
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
