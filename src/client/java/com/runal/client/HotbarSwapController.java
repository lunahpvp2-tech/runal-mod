package com.runal.client;

import net.minecraft.client.Minecraft;
//? if 1.21.4 || 1.21.11 {
/*import net.minecraft.world.inventory.ClickType;
*///?} else {
import net.minecraft.world.inventory.ContainerInput;
//?}
import net.minecraft.world.inventory.InventoryMenu;

public class HotbarSwapController {

    public static void swap(String row) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) return;
        if (mc.player.containerMenu != mc.player.inventoryMenu) return;

        int rowStart;
        if (HotbarSwapState.ROW_TOP.equals(row)) {
            rowStart = InventoryMenu.INV_SLOT_START;
        } else if (HotbarSwapState.ROW_MIDDLE.equals(row)) {
            rowStart = InventoryMenu.INV_SLOT_START + 9;
        } else {
            rowStart = InventoryMenu.INV_SLOT_END - 9;
        }

        int containerId = mc.player.containerMenu.containerId;
        for (int i = 0; i < 9; i++) {
            int rowSlot = rowStart + i;
            //? if 1.21.4 || 1.21.11 {
            /*mc.gameMode.handleInventoryMouseClick(containerId, rowSlot, i, ClickType.SWAP, mc.player);
            *///?} else {
            mc.gameMode.handleContainerInput(containerId, rowSlot, i, ContainerInput.SWAP, mc.player);
            //?}
        }
    }
}
