package com.rumaruka.emt.item.tool;

import com.rumaruka.emt.util.EMTConfigHandler;
import ic2.api.item.ElectricItem;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.BlockUtils;

import java.util.Objects;

public class ItemRockbreakerDrill extends ItemThaumiumDrill {

    private static final Block[] isEffective = {
        Blocks.NETHER_BRICK, Blocks.NETHERRACK, Blocks.GLOWSTONE, Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK,
        Blocks.DIAMOND_BLOCK, Blocks.LAPIS_BLOCK, Blocks.REDSTONE_BLOCK, Blocks.REDSTONE_ORE, Blocks.EMERALD_ORE,
        Blocks.STONEBRICK, Blocks.GLASS, Blocks.STONE, Blocks.GOLD_ORE, Blocks.IRON_ORE, Blocks.COAL_ORE,
        Blocks.COBBLESTONE, Blocks.DIAMOND_ORE, Blocks.LAPIS_ORE, Blocks.DIRT, Blocks.GRAVEL, Blocks.SAND,
        Blocks.SANDSTONE, Blocks.SOUL_SAND, Blocks.CLAY, Blocks.GRASS, Blocks.SNOW_LAYER, Blocks.SNOW,
        Blocks.FARMLAND, Blocks.HARDENED_CLAY, Blocks.STAINED_HARDENED_CLAY, Blocks.MOSSY_COBBLESTONE
    };

    // Note: In Minecraft, Items are singletons. Storing 'side' here can be
    // glitchy in multiplayer, but it's the standard way for 1.12.2 AoE tools.
    private EnumFacing side = EnumFacing.DOWN;
    public int searchCost = 1000;
    public int hitCost = 400;

    public ItemRockbreakerDrill() {
        this.efficiency = 25F;
        this.setMaxStackSize(1);
        if (!EMTConfigHandler.toolsInBore) {
            this.setMaxDamage(27);
        } else {
            this.setMaxDamage(2571);
        }
        maxCharge = 1000000;
        transferLimit = 1000;
        tier = 3;
    }

    private boolean isEffectiveAgainst(Block block) {
        for (Block b : isEffective) {
            if (b == block) return true;
        }
        return false;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        // Trace the block side before it's destroyed so we know which way the 3x3 face is
        RayTraceResult rayTraceResult = BlockUtils.getTargetBlock(player.world, player, false);
        if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
            side = rayTraceResult.sideHit;
        }
        return super.onBlockStartBreak(itemstack, pos, player);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        int actualCost = EMTConfigHandler.toolsInBore ? 1 : 350;

        // 1. Handle Sneaking (Disable AoE)
        if (entityLiving.isSneaking()) {
            ElectricItem.manager.use(stack, actualCost, entityLiving);
            return true;
        }

        // 2. Check if the initial block was valid for the tool
        if (ForgeHooks.isToolEffective(worldIn, pos, stack) || isEffectiveAgainst(state.getBlock())) {

            // Iterate in a 3x3 pattern perpendicular to the side hit
            for (int aa = -1; aa <= 1; aa++) {
                for (int bb = -1; bb <= 1; bb++) {

                    // Skip the center block because Vanilla/Forge already destroyed it
                    if (aa == 0 && bb == 0) continue;

                    BlockPos targetPos;

                    // Determine which axis to spread based on the face hit
                    if (side.getAxis() == EnumFacing.Axis.Y) {
                        targetPos = pos.add(aa, 0, bb);
                    } else if (side.getAxis() == EnumFacing.Axis.Z) {
                        targetPos = pos.add(aa, bb, 0);
                    } else { // X Axis
                        targetPos = pos.add(0, aa, bb);
                    }

                    IBlockState targetState = worldIn.getBlockState(targetPos);

                    // Only break the block if the drill is effective against it
                    if (ForgeHooks.isToolEffective(worldIn, targetPos, stack) || isEffectiveAgainst(targetState.getBlock())) {
                        if (ElectricItem.manager.canUse(stack, actualCost)) {
                            ElectricItem.manager.use(stack, actualCost, entityLiving);

                            // Use Thaumcraft's harvestBlock to handle drops, XP, and events properly
                            BlockUtils.harvestBlock(worldIn, (EntityPlayer) entityLiving, targetPos);
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemStack = player.getHeldItem(hand);

        // Sneak + Right Click logic (Sound/Energy use)
        if (player.isSneaking()) {
            ElectricItem.manager.use(itemStack, searchCost, player);
            worldIn.playSound(null, pos, SoundsTC.wandfail, SoundCategory.PLAYERS, 0.2f, 0.2f + worldIn.rand.nextFloat() * 0.2f);
            return EnumActionResult.SUCCESS;
        } else {
            for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                ItemStack torchItem = player.inventory.mainInventory.get(i);
                if (torchItem == ItemStack.EMPTY || !Objects.requireNonNull(torchItem.getItem().getRegistryName()).getResourcePath().contains("torch")) {
                    continue;
                }
                Item item = torchItem.getItem();
                if(!(item instanceof ItemBlock)){
                    continue;
                }
                int oldMeta = torchItem.getItemDamage();
                if (player.capabilities.isCreativeMode) {
                    torchItem.setItemDamage(oldMeta);
                }
            }
        }

        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        if (!player.world.isRemote && !(entity instanceof EntityPlayer)) {
            entity.setFire(2);
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        if (ElectricItem.manager.use(stack, hitCost, attacker)) {
            target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) attacker), 12F);
        }
        return false;
    }
}
