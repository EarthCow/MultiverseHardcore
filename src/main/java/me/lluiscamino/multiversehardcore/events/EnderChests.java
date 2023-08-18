package me.lluiscamino.multiversehardcore.events;

import me.lluiscamino.multiversehardcore.MultiverseHardcore;
import me.lluiscamino.multiversehardcore.exceptions.WorldIsNotHardcoreException;
import me.lluiscamino.multiversehardcore.models.HardcoreWorld;
import me.lluiscamino.multiversehardcore.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EnderChests implements Listener {

    List<UUID> notifiedPlayers = new ArrayList<>();

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        try {
            if (event.getInventory().equals(event.getPlayer().getEnderChest())) {
                new HardcoreWorld(WorldUtils.getNormalWorld(event.getPlayer().getWorld()).getName());
                if (!notifiedPlayers.contains(event.getPlayer().getUniqueId())) {
                    event.getPlayer().sendMessage(ChatColor.RED + "The contents of your ender chest will be dropped upon death");
                    notifiedPlayers.add(event.getPlayer().getUniqueId());
                    Bukkit.getScheduler().runTaskLater(MultiverseHardcore.getInstance(), () -> notifiedPlayers.remove(event.getPlayer().getUniqueId()), 1800 * 20L);
                }
            }
        } catch (WorldIsNotHardcoreException ignored) {
        }
    }

}
