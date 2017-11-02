package club.nsdn.nyasamaoptics.Renderers.TileEntity;

import club.nsdn.nyasamaoptics.Renderers.RendererHelper;
import club.nsdn.nyasamaoptics.TileEntities.HoloJet;
import club.nsdn.nyasamaoptics.TileEntities.PillarHead;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
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
 * Created by drzzm on 2017.11.2.
 */
public class PillarHeadRenderer extends TileEntitySpecialRenderer {

    private World world;
    private ResourceLocation baseTexture, text;
    private WavefrontObject baseModel;

    public PillarHeadRenderer() {
        world = Minecraft.getMinecraft().theWorld;
        baseTexture = new ResourceLocation("nyasamaoptics", "textures/blocks/light_shell.png");
        text = new ResourceLocation("nyasamaoptics", "textures/blocks/white.png");
        baseModel = new WavefrontObject(
                new ResourceLocation("nyasamaoptics", "models/blocks/" + "pillar_head" + "_base.obj")
        );
    }

    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float scale) {
        PillarHead.TileText tileText = (PillarHead.TileText) tileEntity;
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

            int meta = tileText.getBlockMetadata();
            int angle = (meta & 0x3) * 90;
            GL11.glColor3f(1.0F, 1.0F, 1.0F);
            RendererHelper.renderWithResourceAndRotation(baseModel, angle, baseTexture);

            GL11.glPushMatrix();
            {
                GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(angle, 0.0F, 1.0F, 0.0F);

                Minecraft.getMinecraft().renderEngine.bindTexture(text);
                GL11.glPushMatrix();
                {
                    GL11.glTranslatef(0.0F, 0.5F, 0.0F);
                    GL11.glPushMatrix();
                    {
                        GL11.glScaled(tileText.scale, tileText.scale, 1.0);
                        if (tileText.model != null) {
                            GL11.glTranslated(0.0, 0.0, 0.0625 * (3.9 - tileText.thick));
                            tileText.model.render((Entity) null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
                            GL11.glPushMatrix();
                            {
                                GL11.glTranslated(0.0, 0.0, -0.0625 * (3.9 - tileText.thick));
                                GL11.glRotated(180.0, 0.0, 1.0, 0.0);
                                tileText.model.render((Entity) null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
                            }
                            GL11.glPopMatrix();
                        }
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
