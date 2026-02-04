package com.rumaruka.emt.item.events;

import com.rumaruka.emt.item.armor.boots.ItemElectricBootsTraveller;
import com.rumaruka.emt.item.armor.boots.ItemNanoBootsTraveller;
import com.rumaruka.emt.item.armor.boots.ItemQuantumBootsTraveller;
import ic2.api.item.ElectricItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;

public class BootsFallEvent {
    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer entity = (EntityPlayer) event.getEntity();
            ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
            Item item = stack.getItem();
            float distance = event.getDistance();

            // All 3 boots inherit from this class
            if (item instanceof ItemElectricBootsTraveller) {
                int minDistance = 4;
                int damageReductionCost = 5000;

                if (item instanceof ItemNanoBootsTraveller) {
                    minDistance = 6;
                    damageReductionCost = 7500;
                } else if (item instanceof ItemQuantumBootsTraveller) {
                    minDistance = 10;
                    damageReductionCost = 10000;
                }

                if (distance > minDistance) {
                    int damage = (int)Math.floor((distance - minDistance) * event.getDamageMultiplier() / 2.d);
                    double charge = ElectricItem.manager.getCharge(stack);
                    double target = damage * damageReductionCost;

                    if (charge >= target){
                        ElectricItem.manager.use(stack, target, entity);
                        event.setCanceled(true);
                    } else {
                        ElectricItem.manager.use(stack, charge, entity);
                        event.setDamageMultiplier((float)((target - charge) / damageReductionCost) / (distance - minDistance));
                    }
                } else {
                    event.setCanceled(true);
                }
            }
        }
    }
}
