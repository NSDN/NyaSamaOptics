package club.nsdn.nyasamaoptics.api;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

/**
 * Created by drzzm on 2018.1.13.
 */
public class LightBeam extends Block {

    public final Class<? extends Block> source;
    public final int lightType;

    public static final int TYPE_DOT = 0;
    public static final int TYPE_LINE = 1;

    public LightBeam(Class<? extends Block> source, int lightType) {
        super(Material.air);
        this.source = source;
        this.lightType = lightType;
        setLightLevel(1.0F);
        setLightOpacity(0);
        setHardness(-1.0F);
        setResistance(0xFFFFFF);
        int zero = 0;
        setBlockBounds(
            zero, zero, zero, zero, zero, zero
        );
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
    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        world.scheduleBlockUpdate(x, y, z, this, 1);
    }

    @Override
    public int tickRate(World world) {
        return 1;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (!world.isRemote) checkNearby(world, x, y, z);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        checkNearby(world, x, y, z);
    }

    public ForgeDirection getDir(World world, int x, int y, int z) {
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

    public void setDir(World world, int x, int y, int z, ForgeDirection dir) {
        int meta = -1;
        switch (dir) {
            case NORTH: meta = 0; break;
            case SOUTH: meta = 1; break;
            case WEST:  meta = 2; break;
            case EAST:  meta = 3; break;
            case UP:    meta = 4; break;
            case DOWN:  meta = 5; break;
        }
        if (meta >= 0) {
            world.setBlockMetadataWithNotify(x, y, z, meta, 3);
        }
    }

    public boolean isSource(World world, int x, int y, int z) {
        return world.getBlock(x, y, z).getClass() == source;
    }

    public boolean isMe(World world, int x, int y, int z) {
        return world.getBlock(x, y, z) == this;
    }

    public void checkNearby(World world, int x, int y, int z) {
        ForgeDirection dir = getDir(world, x, y, z);
        switch (lightType) {
            case TYPE_DOT:
                if (
                    !isSource(world, x - 1, y, z) && !isSource(world, x + 1, y, z) &&
                    !isSource(world, x, y - 1, z) && !isSource(world, x, y + 1, z) &&
                    !isSource(world, x, y, z - 1) && !isSource(world, x, y, z + 1)
                ) {
                    world.setBlock(x, y, z, Blocks.air);
                }
                break;
            case TYPE_LINE:
                boolean result = true;
                switch (dir) {
                    case NORTH: result = !isSource(world, x, y, z + 1) && !isMe(world, x, y, z + 1); break;
                    case SOUTH: result = !isSource(world, x, y, z - 1) && !isMe(world, x, y, z - 1); break;
                    case WEST:  result = !isSource(world, x + 1, y, z) && !isMe(world, x + 1, y, z); break;
                    case EAST:  result = !isSource(world, x - 1, y, z) && !isMe(world, x - 1, y, z); break;
                    case UP:    result = !isSource(world, x, y - 1, z) && !isMe(world, x, y - 1, z); break;
                    case DOWN:  result = !isSource(world, x, y + 1, z) && !isMe(world, x, y + 1, z); break;
                }
                if (result) world.setBlock(x, y, z, Blocks.air);
                break;
        }
    }

}
