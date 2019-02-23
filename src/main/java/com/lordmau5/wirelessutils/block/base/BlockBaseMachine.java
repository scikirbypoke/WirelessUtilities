package com.lordmau5.wirelessutils.block.base;

import cofh.api.tileentity.IInventoryRetainer;
import cofh.core.util.helpers.FluidHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.base.IJEIInformationItem;
import com.lordmau5.wirelessutils.tile.base.ISidedTransfer;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseMachine;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.constants.Properties;
import com.lordmau5.wirelessutils.utils.crafting.INBTPreservingIngredient;
import mezz.jei.api.IModRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public abstract class BlockBaseMachine extends BlockBaseTile implements IJEIInformationItem, IInventoryRetainer, INBTPreservingIngredient {

    private String name;

    protected BlockBaseMachine() {
        super();

        setHardness(5F);
        setResistance(10F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
    }

    @Override
    public void setName(String name) {
        this.name = name;
        super.setName(name);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if ( tab != this.getCreativeTab() )
            return;

        Level[] levels = Level.values();

        for (int i = 0; i < levels.length; i++)
            items.add(new ItemStack(this, 1, i));
    }

    @Override
    public void registerJEI(IModRegistry registry) {
        if ( name == null )
            return;

        Level[] levels = Level.values();
        for (int i = 0; i < levels.length; i++)
            IJEIInformationItem.addJEIInformation(registry, new ItemStack(this, 1, i), "tab." + WirelessUtils.MODID + "." + name);
    }

    @Override
    public boolean isValidForCraft(IRecipe recipe, InventoryCrafting craft, ItemStack stack, ItemStack output) {
        return Block.getBlockFromItem(output.getItem()) == this;
    }

    /* Block State */

    public boolean hasSidedTransfer() {
        return false;
    }


    @Override
    protected BlockStateContainer createBlockState() {
        if ( hasSidedTransfer() )
            return new BlockStateContainer(this, Properties.ACTIVE, Properties.LEVEL, Properties.SIDES[0], Properties.SIDES[1], Properties.SIDES[2], Properties.SIDES[3], Properties.SIDES[4], Properties.SIDES[5]);

        return new BlockStateContainer(this, Properties.ACTIVE, Properties.LEVEL);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if ( tile instanceof TileEntityBaseMachine ) {
            TileEntityBaseMachine machine = (TileEntityBaseMachine) tile;
            state = state.withProperty(Properties.ACTIVE, machine.isActive);
        }

        if ( hasSidedTransfer() && tile instanceof ISidedTransfer ) {
            ISidedTransfer transfer = (ISidedTransfer) tile;
            for (ISidedTransfer.TransferSide side : ISidedTransfer.TransferSide.VALUES)
                state = state.withProperty(Properties.SIDES[side.index], transfer.getSideTransferMode(side) == ISidedTransfer.Mode.ACTIVE);
        }

        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(Properties.LEVEL);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        if ( meta < 0 )
            meta = 0;

        if ( meta >= Level.values().length )
            meta = Level.values().length - 1;

        return this.getDefaultState().withProperty(Properties.LEVEL, meta);
    }

    /* Block <-> Item Conversion */

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if ( te instanceof TileEntityBaseMachine ) {
            TileEntityBaseMachine machine = (TileEntityBaseMachine) te;
            machine.setLevel(stack.getMetadata());

            if ( placer instanceof EntityPlayer ) {
                EntityPlayer player = (EntityPlayer) placer;
                machine.setOwner(player.getGameProfile());
            }

            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null ) {
                if ( tag.hasKey("BlockEntityTag") )
                    tag = tag.getCompoundTag("BlockEntityTag");

                machine.readAugmentsFromNBT(tag);
            }

            machine.updateAugmentStatus();

            if ( tag != null )
                machine.readExtraFromNBT(tag);
        }

        super.onBlockPlacedBy(world, pos, state, placer, stack);
    }

    @Override
    public NBTTagCompound getItemStackTag(IBlockAccess world, BlockPos pos) {
        NBTTagCompound tag = super.getItemStackTag(world, pos);

        TileEntity tile = world.getTileEntity(pos);
        if ( !(tile instanceof TileEntityBaseMachine) )
            return tag;

        TileEntityBaseMachine machine = (TileEntityBaseMachine) tile;

        tag.setBoolean("Configured", true);

        machine.writeAugmentsToNBT(tag);
        machine.writeExtraToNBT(tag);

        return tag;
    }

    @Override
    public boolean retainInventory() {
        return true;
    }

    @Override
    public boolean onBlockActivatedDelegate(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntityBaseMachine machine = (TileEntityBaseMachine) world.getTileEntity(pos);
        ItemStack stack = player.getHeldItem(hand);
        if ( stack.getItem() == Items.BED && stack.getDisplayName().equalsIgnoreCase("debug") ) {
            if ( machine != null ) {
                machine.debugPrint();
                return true;
            }
        }

        if ( machine != null && !player.isSneaking() ) {
            IFluidHandler handler = machine.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
            if ( FluidHelper.isFillableEmptyContainer(stack) ) {
                if ( FluidHelper.fillItemFromHandler(stack, handler, player, hand) )
                    return true;
            } else {
                if ( FluidHelper.drainItemToHandler(stack, handler, player, hand) )
                    return true;
            }
        }

        return super.onBlockActivatedDelegate(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }

    /* Rendering */

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }
}
