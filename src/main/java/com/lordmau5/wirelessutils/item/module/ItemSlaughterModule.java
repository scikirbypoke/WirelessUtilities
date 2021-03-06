package com.lordmau5.wirelessutils.item.module;

import cofh.core.network.PacketBase;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.modules.ElementSlaughterModule;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.CachedItemList;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemSlaughterModule extends ItemFilteringModule {

    public static final CachedItemList weaponList = new CachedItemList();

    static {
        weaponList.addInput(ModConfig.vaporizers.modules.slaughter.weaponList);
    }


    public ItemSlaughterModule() {
        super();
        setName("slaughter_module");
    }

    @Override
    public boolean allowPlayerMode() {
        return ModConfig.vaporizers.modules.slaughter.targetPlayers;
    }

    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return true;
    }

    @Nullable
    @Override
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        return Level.fromInt(ModConfig.vaporizers.modules.slaughter.requiredLevel);
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.slaughter.energyMultiplier;
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.slaughter.energyAddition;
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.slaughter.energyDrain;
    }

    @Override
    public boolean isConfigured(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return false;

        return tag.hasKey("CollectDrops") || tag.hasKey("CollectExp") || super.isConfigured(stack);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        String name = "item." + WirelessUtils.MODID + ".slaughter_module";

        tooltip.add(new TextComponentTranslation(
                name + ".drops",
                StringHelper.localize("btn." + WirelessUtils.MODID + ".drop_mode." + getDropMode(stack))
        ).getFormattedText());

        tooltip.add(new TextComponentTranslation(
                name + ".exp",
                StringHelper.localize("btn." + WirelessUtils.MODID + ".drop_mode." + getExperienceMode(stack))
        ).getFormattedText());

        if ( ModConfig.vaporizers.modules.slaughter.enableAsPlayer && getAsPlayer(stack) )
            tooltip.add(StringHelper.localize(name + ".as_player"));
    }

    public boolean getAsPlayer(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            return tag != null && tag.getBoolean("AsPlayer");
        }

        return false;
    }

    @Nonnull
    public ItemStack setAsPlayer(@Nonnull ItemStack stack, boolean enabled) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( enabled )
            tag.setBoolean("AsPlayer", true);
        else
            tag.removeTag("AsPlayer");

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    public int getDropMode(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("CollectDrops", Constants.NBT.TAG_BYTE) )
                return tag.getByte("CollectDrops");
        }

        return ModConfig.vaporizers.modules.slaughter.collectDrops;
    }

    @Nonnull
    public ItemStack setDropMode(@Nonnull ItemStack stack, int mode) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( mode < ModConfig.vaporizers.modules.slaughter.collectDropsMinimum )
            mode = 3;
        else if ( mode > 3 )
            mode = ModConfig.vaporizers.modules.slaughter.collectDropsMinimum;

        tag.setByte("CollectDrops", (byte) mode);
        stack.setTagCompound(tag);
        return stack;
    }

    public int getExperienceMode(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("CollectExp", Constants.NBT.TAG_BYTE) )
                return tag.getByte("CollectExp");
        }

        return ModConfig.vaporizers.modules.slaughter.collectExperience;
    }

    @Nonnull
    public ItemStack setExperienceMode(@Nonnull ItemStack stack, int mode) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( mode < ModConfig.vaporizers.modules.slaughter.collectExperienceMinimum )
            mode = 3;
        else if ( mode > 3 )
            mode = ModConfig.vaporizers.modules.slaughter.collectExperienceMinimum;


        tag.setByte("CollectExp", (byte) mode);
        stack.setTagCompound(tag);
        return stack;
    }

    @Nullable
    public TileBaseVaporizer.IVaporizerBehavior getBehavior(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return new SlaughterBehavior(vaporizer, stack);
    }

    public static class VaporizerDamage extends EntityDamageSource {
        private final TileBaseVaporizer vaporizer;

        public VaporizerDamage(EntityPlayer player, TileBaseVaporizer vaporizer) {
            super("player", player);
            this.vaporizer = vaporizer;
        }

        public TileBaseVaporizer getVaporizer() {
            return vaporizer;
        }

        @Override
        public boolean isDifficultyScaled() {
            return false;
        }

        @Override
        public boolean isUnblockable() {
            return ModConfig.vaporizers.modules.slaughter.unblockable;
        }

        @Override
        public boolean isDamageAbsolute() {
            return ModConfig.vaporizers.modules.slaughter.absolute;
        }

        @Override
        public ITextComponent getDeathMessage(EntityLivingBase entity) {
            ItemStack weapon = damageSourceEntity instanceof EntityLivingBase ? ((EntityLivingBase) damageSourceEntity).getHeldItemMainhand() : ItemStack.EMPTY;
            if ( !weapon.isEmpty() && weapon.hasDisplayName() )
                return new TextComponentTranslation(
                        "item." + WirelessUtils.MODID + ".slaughter_module.kill.item",
                        entity.getDisplayName(),
                        weapon.getTextComponent()
                );

            return new TextComponentTranslation(
                    "item." + WirelessUtils.MODID + ".slaughter_module.kill",
                    entity.getDisplayName()
            );
        }
    }

    public static class SlaughterBehavior extends FilteredBehavior {

        private static final ItemStack DIAMOND_GHOST = new ItemStack(Items.DIAMOND_SWORD);

        private int cost = 0;

        private int dropMode = 0;
        private int experienceMode = 0;
        private boolean asPlayer = true;
        private boolean hasWeapon = false;

        public SlaughterBehavior(@Nonnull TileBaseVaporizer vaporizer, @Nonnull ItemStack module) {
            super(vaporizer);

            allowBosses = ModConfig.vaporizers.modules.slaughter.attackBosses;
            allowPlayers = ModConfig.vaporizers.modules.slaughter.targetPlayers;
            allowCreative = false;

            requireAttackable = true;
            requireAlive = true;

            updateModule(module);
            updateWeapon();
        }

        public void updateCost() {
            cost = ModConfig.vaporizers.modules.slaughter.energy;

            if ( asPlayer )
                cost += ModConfig.vaporizers.modules.slaughter.energyPlayer;

            if ( hasWeapon )
                cost += ModConfig.vaporizers.modules.slaughter.energyWeapon;
        }

        public void updateWeapon() {
            if ( !ModConfig.vaporizers.modules.slaughter.enableWeapon ) {
                hasWeapon = false;
            } else {
                ItemStack weapon = vaporizer.getInput().getStackInSlot(0);
                hasWeapon = !weapon.isEmpty() && isValidWeapon(weapon);
            }

            updateCost();
        }

        public boolean isValidWeapon(@Nonnull ItemStack weapon) {
            return ModConfig.vaporizers.modules.slaughter.weaponIsWhitelist == weaponList.matches(weapon);
        }

        public double getEnergyMultiplier() {
            ItemStack stack = vaporizer.getModule();
            if ( stack.isEmpty() || !(stack.getItem() instanceof ItemModule) )
                return 1;

            ItemModule item = (ItemModule) stack.getItem();
            return item.getEnergyMultiplier(stack, vaporizer);
        }

        public int getEnergyAddition() {
            ItemStack stack = vaporizer.getModule();
            if ( stack.isEmpty() || !(stack.getItem() instanceof ItemModule) )
                return 0;

            ItemModule item = (ItemModule) stack.getItem();
            return item.getEnergyAddition(stack, vaporizer);
        }

        public int getEnergyDrain() {
            ItemStack stack = vaporizer.getModule();
            if ( stack.isEmpty() || !(stack.getItem() instanceof ItemModule) )
                return 0;

            ItemModule item = (ItemModule) stack.getItem();
            return item.getEneryDrain(stack, vaporizer);
        }

        @Override
        public void updateModule(@Nonnull ItemStack stack) {
            super.updateModule(stack);

            dropMode = ModItems.itemSlaughterModule.getDropMode(stack);
            experienceMode = ModItems.itemSlaughterModule.getExperienceMode(stack);
            asPlayer = ModConfig.vaporizers.modules.slaughter.enableWeapon && ModConfig.vaporizers.modules.slaughter.enableAsPlayer && ModItems.itemSlaughterModule.getAsPlayer(stack);

            updateCost();
        }

        public void updateModifier(@Nonnull ItemStack stack) {

        }

        public ElementModuleBase getGUI(@Nonnull GuiBaseVaporizer gui) {
            return new ElementSlaughterModule(gui, this);
        }

        public boolean wantsFluid() {
            return false;
        }

        public boolean canInvert() {
            return false;
        }

        @Override
        public void updateModePacket(@Nonnull PacketBase packet) {
            super.updateModePacket(packet);
            packet.addByte(dropMode);
            packet.addByte(experienceMode);
            packet.addBool(asPlayer);
        }

        @Nonnull
        @Override
        public ItemStack handleModeDelegate(@Nonnull ItemStack stack, @Nonnull PacketBase packet) {
            ModItems.itemSlaughterModule.setDropMode(stack, packet.getByte());
            ModItems.itemSlaughterModule.setExperienceMode(stack, packet.getByte());
            ModItems.itemSlaughterModule.setAsPlayer(stack, packet.getBool());
            return stack;
        }

        public boolean asPlayer() {
            return asPlayer;
        }

        public int getExperienceMode() {
            return Math.max(experienceMode, ModConfig.vaporizers.modules.slaughter.collectExperienceMinimum);
        }

        @Override
        public int getDropMode() {
            return Math.max(dropMode, ModConfig.vaporizers.modules.slaughter.collectDropsMinimum);
        }

        public boolean isInputUnlocked(int slot) {
            return ModConfig.vaporizers.modules.slaughter.enableWeapon && slot == 0;
        }

        public boolean isValidInput(@Nonnull ItemStack stack, int slot) {
            if ( !ModConfig.vaporizers.modules.slaughter.enableWeapon || slot != 0 )
                return false;

            return isValidWeapon(stack);
        }

        public void updateInput(int slot) {
            updateWeapon();
        }

        @Override
        public void getItemToolTip(@Nonnull List<String> tooltip, @Nullable Slot slot, @Nonnull ItemStack stack) {
            if ( stack.isEmpty() )
                return;

            final boolean valid = isValidWeapon(stack);

            tooltip.add(new TextComponentTranslation(
                    "item." + WirelessUtils.MODID + ".slaughter_module.weapon." + (valid ? "valid" : "invalid")
            ).setStyle(valid ? TextHelpers.GREEN : TextHelpers.RED).getFormattedText());
        }

        @Nonnull
        @Override
        public ItemStack getInputGhost(int slot) {
            if ( slot == 0 && ModConfig.vaporizers.modules.slaughter.enableWeapon ) {
                ItemStack ghost = ItemStack.EMPTY;
                if ( ModConfig.vaporizers.modules.slaughter.weaponIsWhitelist )
                    ghost = pickGhost(weaponList.getGhosts());

                if ( ghost.isEmpty() )
                    return DIAMOND_GHOST;
                return ghost;
            }

            return ItemStack.EMPTY;
        }

        public boolean isModifierUnlocked() {
            return false;
        }

        @Nonnull
        public ItemStack getModifierGhost() {
            return ItemStack.EMPTY;
        }

        public boolean isValidModifier(@Nonnull ItemStack stack) {
            return false;
        }

        public int getBlockEnergyCost(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            return 0;
        }

        public int getEntityEnergyCost(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget target) {
            return cost;
        }

        public int getMaxEntityEnergyCost(@Nonnull TileBaseVaporizer.VaporizerTarget target) {
            return cost;
        }

        public int getActionCost() {
            int cost = ModConfig.vaporizers.modules.slaughter.budget;

            if ( asPlayer )
                cost += ModConfig.vaporizers.modules.slaughter.budgetPlayer;

            if ( hasWeapon )
                cost += ModConfig.vaporizers.modules.slaughter.budgetWeapon;

            return cost;
        }

        @Override
        public boolean canRun(boolean ignorePower) {
            if ( !super.canRun(ignorePower) )
                return false;

            if ( ModConfig.vaporizers.modules.slaughter.requireWeapon && !hasWeapon )
                return false;

            return ignorePower || vaporizer.getEnergyStored() >= cost;
        }

        @Nullable
        @Override
        public String getUnconfiguredExplanation() {
            if ( ModConfig.vaporizers.modules.slaughter.requireWeapon && !hasWeapon )
                return "info." + WirelessUtils.MODID + ".vaporizer.missing_weapon";

            return super.getUnconfiguredExplanation();
        }

        @Nonnull
        public IWorkProvider.WorkResult processEntity(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget target) {
            EntityLivingBase living = (EntityLivingBase) entity;

            TileBaseVaporizer.WUVaporizerPlayer player = vaporizer.getFakePlayer(living.world);
            if ( player == null )
                return IWorkProvider.WorkResult.FAILURE_STOP;

            ItemStack weapon = hasWeapon ? vaporizer.getInput().getStackInSlot(0) : ItemStack.EMPTY;
            // Set up the player to actually do stuff.
            player.capabilities.isCreativeMode = vaporizer.isCreative();
            player.inventory.clear();
            player.setHeldItem(EnumHand.MAIN_HAND, weapon);
            player.updateAttributes(true);
            player.updateCooldown();

            IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);

            float damage = living.getAbsorptionAmount();
            float max = (float) ModConfig.vaporizers.modules.slaughter.maxDamage;
            int mode = ModConfig.vaporizers.modules.slaughter.damageMode;
            if ( mode == 0 )
                damage += living.getHealth();
            else if ( mode == 1 )
                damage += living.getMaxHealth();
            else if ( max == 0 )
                damage = 1000000000;
            else
                damage = max;

            if ( max != 0 && damage > max )
                damage = max;

            // We want to make sure we don't do *less* damage than we should.
            if ( !weapon.isEmpty() ) {
                double real = attribute.getAttributeValue();
                if ( real > damage && real < Float.MAX_VALUE )
                    damage = (float) real;
            }

            if ( asPlayer ) {
                // To give this the best chance of working set the base attack damage
                // of the fake player to the damage value calculated for the big hit.
                // This won't ignore resistances, but it'll still hurt. A lot.
                double old = attribute.getBaseValue();
                if ( damage > old )
                    attribute.setBaseValue(damage);

                // Pretend like we're a real player and whack it.
                boolean success = ForgeHooks.onPlayerAttackTarget(player, living);

                if ( damage > old )
                    attribute.setBaseValue(old);

                // If that worked, just stop now. Otherwise, we murder the old fashioned way.
                if ( !success )
                    return IWorkProvider.WorkResult.SUCCESS_CONTINUE;

            }

            // Just use a big raw damage event to get our slaughter on.
            boolean success = living.attackEntityFrom(new VaporizerDamage(player, vaporizer), damage);

            // Make sure we didn't murder things too hard. This is
            // legitimately a thing that can leave you with entities
            // wandering around, unable to die.
            if ( Float.isNaN(living.getAbsorptionAmount()) )
                living.setAbsorptionAmount(0);

            if ( Float.isNaN(living.getHealth()) )
                living.setHealth(1);

            int weaponDamage = ModConfig.vaporizers.modules.slaughter.damageWeapon;
            if ( success && weaponDamage > 0 && !vaporizer.isCreative() ) {
                weapon.damageItem(weaponDamage, player);
                if ( weapon.isEmpty() ) {
                    hasWeapon = false;
                    vaporizer.getInput().setStackInSlot(0, weapon);
                }
                vaporizer.markChunkDirty();
            }

            return IWorkProvider.WorkResult.SUCCESS_CONTINUE;
        }

        @Nonnull
        public IWorkProvider.WorkResult processBlock(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            return IWorkProvider.WorkResult.FAILURE_REMOVE;
        }
    }
}
