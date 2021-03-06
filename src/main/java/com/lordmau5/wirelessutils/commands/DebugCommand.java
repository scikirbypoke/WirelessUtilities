package com.lordmau5.wirelessutils.commands;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.utils.mod.ModPermissions;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DebugCommand extends CommandBase {

    @Override
    @Nonnull
    public String getName() {
        return "wu_debug";
    }

    @Override
    @Nonnull
    public String getUsage(ICommandSender sender) {
        return "wu_debug";
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return ModPermissions.COMMAND_DEBUG.hasPermission(sender);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        if ( player == null )
            return;

        ItemStack bedStack = new ItemStack(Items.BED, 1, new Random().nextInt(16));
        bedStack.setStackDisplayName("Debug");

        sender.sendMessage(new TextComponentTranslation("commands." + WirelessUtils.MODID + ".debug.success", bedStack.getTextComponent()));

        if ( !player.addItemStackToInventory(bedStack) )
            player.entityDropItem(bedStack, 0);
    }

    @Override
    @Nonnull
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return Collections.emptyList();
    }
}
