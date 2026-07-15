package com.bloxarena.listener;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.util.SelectionTool;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class WandListener implements Listener {

    private final SelectionTool tool;

    public WandListener(SelectionTool tool) {
        this.tool = tool;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Player p = e.getPlayer();
        if (!p.hasPermission("bloxarena.admin")) return;
        if (!tool.isWand(p.getInventory().getItemInMainHand())) return;

        Block block = e.getClickedBlock();
        if (block == null) return;

        Action action = e.getAction();
        if (action == Action.LEFT_CLICK_BLOCK) {
            e.setCancelled(true);
            tool.setPos1(p, block.getLocation());
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            e.setCancelled(true);
            tool.setPos2(p, block.getLocation());
        }
    }
}
