package me.lluiscamino.multiversehardcore.events;

import me.lluiscamino.multiversehardcore.exceptions.WorldIsNotHardcoreException;
import me.lluiscamino.multiversehardcore.models.HardcoreWorld;
import me.lluiscamino.multiversehardcore.utils.WorldUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class EnderChests implements Listener {

    @EventHandler
    public void onItemCrafted(CraftItemEvent event) {
        try {
            if (event.getRecipe().getResult().getType().equals(Material.ENDER_CHEST)) {
                Player player = (Player) event.getInventory().getHolder();
                assert player != null;
                new HardcoreWorld(WorldUtils.getNormalWorld(player.getWorld()).getName());
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Ender chests are disabled on hardcore");
            }
        } catch (WorldIsNotHardcoreException ignored) {
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        try {
            if (event.getInventory().equals(event.getPlayer().getEnderChest())) {
                new HardcoreWorld(WorldUtils.getNormalWorld(event.getPlayer().getWorld()).getName());
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Ender chests are disabled on hardcore");
            }
        } catch (WorldIsNotHardcoreException ignored) {
        }
    }

}
