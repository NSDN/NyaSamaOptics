package club.nsdn.nyasamaoptics.renderer.tileblock;

/**
 * Created by drzzm32 on 2017.1.9.
 */

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class HoloJetModel extends ModelBase {

    ModelRenderer Base;
    ModelRenderer Body1;
    ModelRenderer Body2;
    ModelRenderer Body3;
    ModelRenderer Head;

    public HoloJetModel() {
        textureWidth = 32;
        textureHeight = 10;

        Base = new ModelRenderer(this, 0, 0);
        Base.addBox(0F, 0F, 0F, 4, 2, 4);
        Base.setRotationPoint(-2F, 22F, -2F);
        Base.setTextureSize(32, 10);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        Body1 = new ModelRenderer(this, 0, 0);
        Body1.addBox(0F, 0F, 0F, 5, 2, 5);
        Body1.setRotationPoint(-2.5F, 20F, -2.5F);
        Body1.setTextureSize(32, 10);
        Body1.mirror = true;
        setRotation(Body1, 0F, 0F, 0F);
        Body2 = new ModelRenderer(this, 0, 0);
        Body2.addBox(0F, 0F, 0F, 6, 1, 6);
        Body2.setRotationPoint(-3F, 19F, -3F);
        Body2.setTextureSize(32, 10);
        Body2.mirror = true;
        setRotation(Body2, 0F, 0F, 0F);
        Body3 = new ModelRenderer(this, 0, 0);
        Body3.addBox(0F, 0F, 0F, 7, 1, 7);
        Body3.setRotationPoint(-3.5F, 18F, -3.5F);
        Body3.setTextureSize(32, 10);
        Body3.mirror = true;
        setRotation(Body3, 0F, 0F, 0F);
        Head = new ModelRenderer(this, 0, 0);
        Head.addBox(0F, 0F, 0F, 8, 2, 8);
        Head.setRotationPoint(-4F, 16F, -4F);
        Head.setTextureSize(32, 10);
        Head.mirror = true;
        setRotation(Head, 0F, 0F, 0F);
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(entity, f, f1, f2, f3, f4, f5);
        Base.render(f5);
        Body1.render(f5);
        Body2.render(f5);
        Body3.render(f5);
        Head.render(f5);
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

