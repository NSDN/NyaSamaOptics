package club.nsdn.nyasamaoptics.TileEntities;


import club.nsdn.nyasamaoptics.Util.Font.FontLoader;
import club.nsdn.nyasamaoptics.Util.Font.TextModel;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by drzzm on 2017.1.8.
 */
public class HoloJet extends TileEntityBase {

    public static class TileText extends TileEntity {
        public TextModel model;

        public String content;
        public int color;
        public int thick;
        public double scaleX;
        public double scaleY;
        public double scaleZ;
        public int align;
        public int font;

        public TileText() {
            super();
            content = "DUMMY";
            color = 0xFFFFFF;
            thick = 4;
            scaleX = 1.0;
            scaleY = 1.0;
            scaleZ = 1.0;
            align = FontLoader.ALIGN_CENTER;
            font = FontLoader.FONT_DABIAOSONG;
        }

        public void createModel() {
            model = FontLoader.getModel(font, align, content, color, thick);
        }

        @Override
        public boolean shouldRenderInPass(int pass) {
            return true;
        }

        @Override
        public void writeToNBT(NBTTagCompound tagCompound) {
            super.writeToNBT(tagCompound);
            tagCompound.setString("content", content);
            tagCompound.setInteger("color", color);
            tagCompound.setInteger("thick", thick);
            tagCompound.setDouble("scaleX", scaleX);
            tagCompound.setDouble("scaleY", scaleY);
            tagCompound.setDouble("scaleZ", scaleZ);
            tagCompound.setInteger("align", align);
            tagCompound.setInteger("font", font);
        }

        @Override
        public void readFromNBT(NBTTagCompound tagCompound) {
            super.readFromNBT(tagCompound);
            content = tagCompound.getString("content");
            color = tagCompound.getInteger("color");
            thick = tagCompound.getInteger("thick");
            scaleX = tagCompound.getDouble("scaleX");
            scaleY = tagCompound.getDouble("scaleY");
            scaleZ = tagCompound.getDouble("scaleZ");
            align = tagCompound.getInteger("align");
            font = tagCompound.getInteger("font");
        }

        @Override
        public Packet getDescriptionPacket() {
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setString("content", content);
            tagCompound.setInteger("color", color);
            tagCompound.setInteger("thick", thick);
            tagCompound.setDouble("scaleX", scaleX);
            tagCompound.setDouble("scaleY", scaleY);
            tagCompound.setDouble("scaleZ", scaleZ);
            tagCompound.setInteger("align", align);
            tagCompound.setInteger("font", font);
            return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, tagCompound);
        }

        @Override
        public void onDataPacket(NetworkManager manager, S35PacketUpdateTileEntity packet) {
            NBTTagCompound tagCompound = packet.func_148857_g();
            content = tagCompound.getString("content");
            color = tagCompound.getInteger("color");
            thick = tagCompound.getInteger("thick");
            scaleX = tagCompound.getDouble("scaleX");
            scaleY = tagCompound.getDouble("scaleY");
            scaleZ = tagCompound.getDouble("scaleZ");
            align = tagCompound.getInteger("align");
            font = tagCompound.getInteger("font");
        }

    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileText();
    }

    public HoloJet(String name) {
        super(Material.glass, name);
        setIconLocation("holo_jet");
        setLightLevel(1);
        setCreativeTab(null);
    }

    @Override
    protected void setBoundsByMeta(int meta) {
        float x1 = 0.25F, y1 = 0.0F, z1 = 0.25F, x2 = 0.75F, y2 = 0.5F, z2 = 0.75F;
        switch (meta % 13) {
            case 1:
                setBlockBounds(x1, y1, z1, x2, y2, z2);
                break;
            case 2:
                setBlockBounds(1.0F - z2, y1, x1, 1.0F - z1, y2, x2);
                break;
            case 3:
                setBlockBounds(1.0F - x2, y1, 1.0F - z2, 1.0F - x1, y2, 1.0F - z1);
                break;
            case 4:
                setBlockBounds(z1, y1, 1.0F - x2, z2, y2, 1.0F - x1);
                break;
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
        int l = MathHelper.floor_double((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        if (l == 0) {
            world.setBlockMetadataWithNotify(x, y, z, 1, 2);
        }

        if (l == 1) {
            world.setBlockMetadataWithNotify(x, y, z, 2, 2);
        }

        if (l == 2) {
            world.setBlockMetadataWithNotify(x, y, z, 3, 2);
        }

        if (l == 3) {
            world.setBlockMetadataWithNotify(x, y, z, 4, 2);
        }

    }

}
