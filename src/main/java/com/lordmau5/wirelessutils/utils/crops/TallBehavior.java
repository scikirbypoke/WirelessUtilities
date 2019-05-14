package com.lordmau5.wirelessutils.utils.crops;

import com.google.common.collect.ImmutableSet;
import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockReed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public class TallBehavior implements IHarvestBehavior {

    public final Set<Block> targets;
    public final boolean harvestBottom;

    public int priority = 0;

    public TallBehavior() {
        targets = null;
        harvestBottom = false;
    }

    public TallBehavior(Block... targets) {
        this(ImmutableSet.copyOf(targets), false);
    }

    public TallBehavior(Set<Block> targets, boolean harvestBottom) {
        this.targets = targets;
        this.harvestBottom = harvestBottom;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean appliesTo(IBlockState state) {
        Block block = state.getBlock();
        if ( targets != null )
            return targets.contains(block);

        return (block instanceof BlockReed || block instanceof BlockCactus);
    }

    @Override
    public boolean canHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        if ( TileBaseDesublimator.isBlacklisted(state) )
            return false;

        return appliesTo(state) && (harvestBottom || appliesTo(world.getBlockState(pos.up())));
    }

    @Override
    public HarvestResult harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        return doHarvest(0, state, world, pos, silkTouch, fortune, desublimator) ?
                HarvestResult.SUCCESS : HarvestResult.FAILED;
    }

    private boolean doHarvest(int i, IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        if ( TileBaseDesublimator.isBlacklisted(state) )
            return false;

        IBlockState above = world.getBlockState(pos.up());
        boolean harvested = false;

        if ( i < 255 && appliesTo(above) )
            harvested = doHarvest(i + 1, above, world, pos.up(), silkTouch, fortune, desublimator);

        IBlockState below = world.getBlockState(pos.down());
        if ( !harvestBottom && !appliesTo(below) )
            return harvested;

        return harvestByBreaking(state, world, pos, silkTouch, fortune, desublimator) || harvested;
    }
}
