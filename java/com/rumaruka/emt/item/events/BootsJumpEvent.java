package com.rumaruka.emt.item.events;

import com.rumaruka.emt.item.armor.boots.ItemElectricBootsTraveller;
import com.rumaruka.emt.item.armor.boots.ItemNanoBootsTraveller;
import com.rumaruka.emt.item.armor.boots.ItemQuantumBootsTraveller;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;



public class BootsJumpEvent {

    @SubscribeEvent
    public void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);

            if (IC2.keyboard.isBoostKeyDown(player)) {
                Item item = stack.getItem();

                if (item instanceof ItemQuantumBootsTraveller) {
                    if(ElectricItem.manager.use(stack, 10000, player)) {
                        player.motionY += 0.75f;
                    }
                } else if (item instanceof ItemNanoBootsTraveller) {
                    if(ElectricItem.manager.use(stack, 1000, player)) {
                        player.motionY += 0.5f;
                    }
                } else if (item instanceof ItemElectricBootsTraveller) {
                    if (ElectricItem.manager.use(stack, 100, player)) {
                        player.motionY += 0.25f;
                    }
                }
            }
        }
    }
}
