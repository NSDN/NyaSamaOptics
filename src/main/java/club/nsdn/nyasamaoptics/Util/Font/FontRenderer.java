package club.nsdn.nyasamaoptics.Util.Font;

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
public class FontRenderer extends TileEntitySpecialRenderer {

    private World world;

    public FontRenderer() {
        world = Minecraft.getMinecraft().theWorld;
    }

    private void adjustRotatePivotViaMeta(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        GL11.glPushMatrix();
        GL11.glRotatef(meta * (-90), 0.0F, 0.0F, 1.0F);
        GL11.glPopMatrix();
    }

    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float scale) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);

        Tessellator.instance.setColorOpaque_F(1.0F, 1.0F, 1.0F);

        if (((HoloJet.TileText)tileEntity).model == null) {
            ((HoloJet.TileText)tileEntity).createModel();
        }

        GL11.glPushMatrix();
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

        int rotation = 0;
        switch (tileEntity.getBlockMetadata() % 13) {
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

        GL11.glPushMatrix();
        GL11.glScaled(((HoloJet.TileText)tileEntity).scaleX, ((HoloJet.TileText)tileEntity).scaleY, ((HoloJet.TileText)tileEntity).scaleZ);
        GL11.glTranslated(((HoloJet.TileText)tileEntity).scaleX, -((HoloJet.TileText)tileEntity).scaleY, ((HoloJet.TileText)tileEntity).scaleZ);
        if (((HoloJet.TileText)tileEntity).model != null) ((HoloJet.TileText)tileEntity).model.render((Entity) null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        GL11.glPopMatrix();

        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }

}
