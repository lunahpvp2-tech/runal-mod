package com.runal.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
//? if 1.21.4 || 1.21.11 {
/*import net.minecraft.world.inventory.ClickType;
*///?} else {
import net.minecraft.world.inventory.ContainerInput;
//?}
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

public class AutoTrashController {

    private static final int SCAN_INTERVAL_TICKS = 20;
    private static int tickCounter = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(AutoTrashController::tick);
    }

    private static void tick(Minecraft mc) {
        if (!AutoTrashState.INSTANCE.isEnabled()) return;
        if (mc.player == null || mc.gameMode == null) return;
        if (mc.player.containerMenu != mc.player.inventoryMenu) return;

        tickCounter++;
        if (tickCounter < SCAN_INTERVAL_TICKS) return;
        tickCounter = 0;

        int containerId = mc.player.containerMenu.containerId;
        for (int slotId = InventoryMenu.INV_SLOT_START; slotId < InventoryMenu.USE_ROW_SLOT_END; slotId++) {
            Slot slot = mc.player.containerMenu.getSlot(slotId);
            if (!slot.hasItem()) continue;

            ItemStack stack = slot.getItem();
            String rarity = findShardRarity(stack);
            if (rarity == null) continue;
            if (!AutoTrashState.INSTANCE.shouldTrash(rarity)) continue;

            //? if 1.21.4 || 1.21.11 {
            /*mc.gameMode.handleInventoryMouseClick(containerId, slotId, 1, ClickType.THROW, mc.player);
            *///?} else {
            mc.gameMode.handleContainerInput(containerId, slotId, 1, ContainerInput.THROW, mc.player);
            //?}
        }
    }

    private static String findShardRarity(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) return null;

        for (Component line : lore.lines()) {
            String text = line.getString().trim();
            if (text.endsWith("SHARD")) {
                return text;
            }
        }
        return null;
    }
}
