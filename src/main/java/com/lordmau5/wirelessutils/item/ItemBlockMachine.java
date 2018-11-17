package com.lordmau5.wirelessutils.item;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.item.base.IExplainableItem;
import com.lordmau5.wirelessutils.utils.Level;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

public class ItemBlockMachine extends ItemBlockExplainable implements IExplainableItem {

    public ItemBlockMachine(BlockBaseMachine block) {
        super(block);
    }

    public Level getLevel(ItemStack stack) {
        return Level.fromInt(stack.getMetadata());
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return getLevel(stack).rarity;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String out = new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".tiered.name",
                new TextComponentTranslation(getTranslationKey(stack) + ".name"),
                getLevel(stack).getName()
        ).getUnformattedText();

        if ( stack.hasTagCompound() && stack.getTagCompound().hasKey("Configured") )
            return new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".configured",
                    out).getUnformattedText();

        return out;
    }
}
