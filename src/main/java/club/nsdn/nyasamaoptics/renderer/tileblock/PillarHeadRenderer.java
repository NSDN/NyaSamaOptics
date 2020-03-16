package club.nsdn.nyasamaoptics.renderer.tileblock;

import club.nsdn.nyasamaoptics.tileblock.holo.PillarHead;
import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.render.RendererHelper;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import cn.ac.nya.forgeobj.WavefrontObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

/**
 * Created by drzzm32 on 2019.1.30.
 */
public class PillarHeadRenderer extends AbsTileEntitySpecialRenderer {

    private ResourceLocation baseTexture, text;
    private WavefrontObject baseModel;

    public PillarHeadRenderer() {
        baseTexture = new ResourceLocation("nyasamaoptics", "textures/blocks/light_shell.png");
        text = new ResourceLocation("nyasamaoptics", "textures/blocks/white.png");
        baseModel = new WavefrontObject(
                new ResourceLocation("nyasamaoptics", "models/blocks/" + "pillar_head" + "_base.obj")
        );
    }

    @Override
    public void render(@Nonnull TileEntityBase te, double x, double y, double z, float partialTicks, int destroyStage, float partial) {
        int meta = te.META;

        if (!(te instanceof PillarHead.TileEntityPillarHead)) return;
        PillarHead.TileEntityPillarHead tileEntityPillarHead = (PillarHead.TileEntityPillarHead) te;
        GL11.glPushMatrix();
        {
            GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);

            tileEntityPillarHead.createModel();

            //RendererHelper.beginSpecialLighting();

            float angle = (meta % 4) * 90.0F;
            GL11.glColor3f(1.0F, 1.0F, 1.0F);
            RendererHelper.renderWithResourceAndRotation(baseModel, angle, baseTexture);

            RendererHelper.beginSpecialLighting();

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
                        GL11.glScaled(tileEntityPillarHead.scale, tileEntityPillarHead.scale, 1.0);
                        GL11.glPushMatrix();
                        {
                            doRenderText(tileEntityPillarHead);
                            GL11.glRotated(180.0, 0.0, 1.0, 0.0);
                            doRenderText(tileEntityPillarHead);
                        }
                        GL11.glPopMatrix();
                    }
                    GL11.glPopMatrix();
                }
                GL11.glPopMatrix();
            }
            GL11.glPopMatrix();

            GL11.glColor3f(1.0F, 1.0F, 1.0F);
            RendererHelper.endSpecialLighting();
        }
        GL11.glPopMatrix();
    }

    private void doRenderText(PillarHead.TileEntityPillarHead tileEntityPillarHead) {
        boolean control = true;
        if (tileEntityPillarHead.getSender() != null) control = tileEntityPillarHead.isEnabled;
        if (tileEntityPillarHead.model != null && control) {
            GL11.glPushMatrix();
            GL11.glTranslated(0.0, 0.0, 0.0625 * (3.9 - tileEntityPillarHead.thick));
            tileEntityPillarHead.model.render(tileEntityPillarHead);
            GL11.glPopMatrix();
        }
    }

}
