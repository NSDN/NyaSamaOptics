package club.nsdn.nyasamaoptics.TileEntities;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.block.BlockContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.client.renderer.texture.IIconRegister;
import club.nsdn.nyasamaoptics.CreativeTab.CreativeTabLoader;

public class TileEntityBase extends BlockContainer {

    public static class TileEntity extends net.minecraft.tileentity.TileEntity {

        public void fromNBT(NBTTagCompound tagCompound) {
        }

        public NBTTagCompound toNBT(NBTTagCompound tagCompound) {
            return tagCompound;
        }

        @Override
        public void writeToNBT(NBTTagCompound tagCompound) {
            super.writeToNBT(tagCompound);
            toNBT(tagCompound);
        }

        @Override
        public void readFromNBT(NBTTagCompound tagCompound) {
            super.readFromNBT(tagCompound);
            fromNBT(tagCompound);
        }

        @Override
        public Packet getDescriptionPacket() {
            NBTTagCompound tagCompound = new NBTTagCompound();
            toNBT(tagCompound);
            return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, tagCompound);
        }

        @Override
        public void onDataPacket(NetworkManager manager, S35PacketUpdateTileEntity packet) {
            NBTTagCompound tagCompound = packet.func_148857_g();
            fromNBT(tagCompound);
        }

        public static void updateTileEntity(TileEntity tileEntity) {
            if (tileEntity == null) return;
            tileEntity.getWorldObj().markBlockForUpdate(
                    tileEntity.xCoord,
                    tileEntity.yCoord,
                    tileEntity.zCoord
            );
        }

    }

    protected String textureLocation = "";
    protected void setIconLocation(String textureLocation) { this.textureLocation = "nyasamaoptics" + ":" + textureLocation; }

    public TileEntityBase(String blockName) {
        super(Material.rock);
        setBlockName(blockName);
        setBlockTextureName("minecraft:quartz_block_side");
        setIconLocation("minecraft:quartz_block_side");
        setHardness(2.0F);
        setLightLevel(0);
        setStepSound(Block.soundTypeStone);
        setResistance(10.0F);
        setCreativeTab(CreativeTabLoader.tabNyaSamaOptics);
    }

    public TileEntityBase(Material material, String blockName) {
        super(material);
        setBlockName(blockName);
        setBlockTextureName("minecraft:quartz_block_side");
        setIconLocation("minecraft:quartz_block_side");
        setHardness(2.0F);
        setLightLevel(0);
        setStepSound(Block.soundTypeGlass);
        setResistance(10.0F);
        setCreativeTab(CreativeTabLoader.tabNyaSamaOptics);
    }

    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntity();
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    protected void setBoundsByXYZ(int meta, float x1, float y1, float z1, float x2, float y2, float z2) {
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

            case 7:
                setBlockBounds(x1, z1, y1, x2, z2, y2);
                break;
            case 8:
                setBlockBounds(1.0F - y2, z1, x1, 1.0F - y1, z2, x2);
                break;
            case 5:
                setBlockBounds(1.0F - x2, z1, 1.0F - y2, 1.0F - x1, z2, 1.0F - y1);
                break;
            case 6:
                setBlockBounds(y1, z1, 1.0F - x2, y2, z2, 1.0F - x1);
                break;

            case 9:
                setBlockBounds(x1, 1.0F - y2, z1, x2, 1.0F - y1, z2);
                break;
            case 10:
                setBlockBounds(1.0F - z2, 1.0F - y2, x1, 1.0F - z1, 1.0F - y1, x2);
                break;
            case 11:
                setBlockBounds(1.0F - x2, 1.0F - y2, 1.0F - z2, 1.0F - x1, 1.0F - y1, 1.0F - z1);
                break;
            case 12:
                setBlockBounds(z1, 1.0F - y2, 1.0F - x2, z2, 1.0F - y1, 1.0F - x1);
                break;
        }
    }

    protected void setBoundsByMeta(int meta) {
    }

    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess w, int x, int y, int z) {
        int meta = w.getBlockMetadata(x, y, z);
        setBoundsByMeta(meta);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
        int l = MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

        if (player.rotationPitch > 22.5F) {
            if (l == 0)
            {
                world.setBlockMetadataWithNotify(x, y, z, 1, 2);
            }

            if (l == 1)
            {
                world.setBlockMetadataWithNotify(x, y, z, 2, 2);
            }

            if (l == 2)
            {
                world.setBlockMetadataWithNotify(x, y, z, 3, 2);
            }

            if (l == 3)
            {
                world.setBlockMetadataWithNotify(x, y, z, 4, 2);
            }
        } else if (player.rotationPitch > -22.5F) {
            if (l == 0)
            {
                world.setBlockMetadataWithNotify(x, y, z, 5, 2);
            }

            if (l == 1)
            {
                world.setBlockMetadataWithNotify(x, y, z, 6, 2);
            }

            if (l == 2)
            {
                world.setBlockMetadataWithNotify(x, y, z, 7, 2);
            }

            if (l == 3)
            {
                world.setBlockMetadataWithNotify(x, y, z, 8, 2);
            }
        } else {
            if (l == 0)
            {
                world.setBlockMetadataWithNotify(x, y, z, 9, 2);
            }

            if (l == 1)
            {
                world.setBlockMetadataWithNotify(x, y, z, 10, 2);
            }

            if (l == 2)
            {
                world.setBlockMetadataWithNotify(x, y, z, 11, 2);
            }

            if (l == 3)
            {
                world.setBlockMetadataWithNotify(x, y, z, 12, 2);
            }
        }

    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    public void registerBlockIcons(IIconRegister icon)
    {
        this.blockIcon = icon.registerIcon(textureLocation);
    }

}
