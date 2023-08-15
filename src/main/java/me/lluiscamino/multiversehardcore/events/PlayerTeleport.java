package me.lluiscamino.multiversehardcore.events;

import me.lluiscamino.multiversehardcore.utils.WorldUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleport implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        WorldUtils.handlePlayerNormalTeleport(event);
    }
}
