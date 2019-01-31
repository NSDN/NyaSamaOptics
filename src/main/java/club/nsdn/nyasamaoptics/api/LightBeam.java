package club.nsdn.nyasamaoptics.api;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

/**
 * Created by drzzm on 2018.1.13.
 */
public class LightBeam extends Block {

    public Class<? extends Block> source;
    public int lightType;

    public static final int TYPE_DOT = 0;
    public static final int TYPE_LINE = 1;

    public LightBeam(Class<? extends Block> source, int lightType) {
        super(Material.vine);
        this.source = source;
        this.lightType = lightType;
        setLightLevel(1.0F);
        setLightOpacity(0);
        setHardness(-1.0F);
        setResistance(0xFFFFFF);
        setBlockTextureName("nyasamaoptics:light_beam");
        int zero = 0;
        setBlockBounds(zero, zero, zero, zero, zero, zero);
    }

    public LightBeam(Class<? extends Block> source, int lightType, float lightLevel) {
        super(Material.vine);
        this.source = source;
        this.lightType = lightType;
        setLightLevel(lightLevel);
        setLightOpacity(0);
        setHardness(-1.0F);
        setResistance(0xFFFFFF);
        setBlockTextureName("nyasamaoptics:light_beam");
        int zero = 0;
        setBlockBounds(zero, zero, zero, zero, zero, zero);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isAir(IBlockAccess world, int x, int y, int z) {
        return true;
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockAccess world, int x, int y, int z) {
        return true;
    }

    @Override
    public boolean isLeaves(IBlockAccess world, int x, int y, int z) {
        return true;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        checkNearby(world, x, y, z);
    }

    public void lightCtl(World world, int x, int y, int z, boolean state) {
        lightCtl(world, x, y, z, this, state);
    }

    public void lightCtl(World world, int x, int y, int z, ForgeDirection dir, int length, boolean state) {
        lightCtl(world, x, y, z, this, dir, length, state);
    }

    /******************************************************************************************************************/

    public static ForgeDirection getDir(World world, int x, int y, int z) {
        switch (world.getBlockMetadata(x, y, z)) {
            case 0: return ForgeDirection.NORTH;
            case 1: return ForgeDirection.SOUTH;
            case 2: return ForgeDirection.WEST;
            case 3: return ForgeDirection.EAST;
            case 4: return ForgeDirection.UP;
            case 5: return ForgeDirection.DOWN;
        }
        return ForgeDirection.UNKNOWN;
    }

    public static int getMetaFromDir(ForgeDirection dir) {
        int meta = -1;
        switch (dir) {
            case NORTH: meta = 0; break;
            case SOUTH: meta = 1; break;
            case WEST:  meta = 2; break;
            case EAST:  meta = 3; break;
            case UP:    meta = 4; break;
            case DOWN:  meta = 5; break;
        }
        return meta;
    }

    public static boolean isSource(World world, int x, int y, int z, ForgeDirection offset) {
        if (world.getBlock(x, y, z) instanceof LightBeam) {
            LightBeam lightBeam = (LightBeam) world.getBlock(x, y, z);
            x +=offset.offsetX; y += offset.offsetY; z += offset.offsetZ;
            return lightBeam.source.isInstance(world.getBlock(x, y, z));
        }
        return false;
    }

    public static boolean isMe(World world, int x, int y, int z) {
        return world.getBlock(x, y, z) instanceof LightBeam;
    }

    public static boolean isMe(World world, int x, int y, int z, ForgeDirection offset) {
        x +=offset.offsetX; y += offset.offsetY; z += offset.offsetZ;
        return world.getBlock(x, y, z) instanceof LightBeam;
    }

    public static boolean placeLight(World world, int x, int y, int z, LightBeam lightBeam, int meta) {
        if (meta == -1) {
            if (isMe(world, x, y, z)) {
                world.setBlockToAir(x, y, z);
            } else return false;
        } else {
            if (world.isAirBlock(x, y, z) && !isMe(world, x, y, z)) {
                world.setBlock(x, y, z, lightBeam, meta, 3);
            } else return false;
        }

        return true;
    }

    public static void lightCtl(World world, int x, int y, int z, LightBeam lightBeam, boolean state) {
        int meta = state ? 0 : -1;

        placeLight(world, x + 1, y, z, lightBeam, meta);
        placeLight(world, x - 1, y, z, lightBeam, meta);
        placeLight(world, x, y + 1, z, lightBeam, meta);
        placeLight(world, x, y - 1, z, lightBeam, meta);
        placeLight(world, x, y, z + 1, lightBeam, meta);
        placeLight(world, x, y, z - 1, lightBeam, meta);
    }

    public static void lightCtl(World world, int x, int y, int z, LightBeam lightBeam, ForgeDirection dir, int length, boolean state) {
        int meta = state ? getMetaFromDir(dir) : -1;

        int tx, ty, tz;
        for (int i = 1; i < length; i++) {
            tx = x + dir.offsetX * i;
            ty = y + dir.offsetY * i;
            tz = z + dir.offsetZ * i;
            if (!placeLight(world, tx, ty, tz, lightBeam, meta)) break;
        }
    }

    public static void checkNearby(World world, int x, int y, int z) {
        boolean keepAlive = false;

        if (world.getBlock(x, y, z) instanceof LightBeam) {
            LightBeam lightBeam = (LightBeam) world.getBlock(x, y, z);

            switch (lightBeam.lightType) {
                case TYPE_DOT:
                    keepAlive |= isSource(world, x, y, z, ForgeDirection.UP);
                    keepAlive |= isSource(world, x, y, z, ForgeDirection.DOWN);
                    keepAlive |= isSource(world, x, y, z, ForgeDirection.NORTH);
                    keepAlive |= isSource(world, x, y, z, ForgeDirection.SOUTH);
                    keepAlive |= isSource(world, x, y, z, ForgeDirection.WEST);
                    keepAlive |= isSource(world, x, y, z, ForgeDirection.EAST);
                    break;
                case TYPE_LINE:
                    ForgeDirection dir = getDir(world, x, y, z).getOpposite();
                    keepAlive = isSource(world, x, y, z, dir) || isMe(world, x, y, z, dir);
                    break;
            }
        }

        if (!keepAlive) world.setBlockToAir(x, y, z);
    }

}
