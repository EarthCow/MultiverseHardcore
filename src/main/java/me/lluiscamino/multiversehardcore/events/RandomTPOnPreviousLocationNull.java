package me.lluiscamino.multiversehardcore.events;

import me.lluiscamino.multiversehardcore.utils.WorldUtils;
import nl.zandervdm.stayput.CustomEvents.RandomTPOnPreviousLocationNullEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RandomTPOnPreviousLocationNull implements Listener {

    @EventHandler
    public void onRandomTPOnPreviousLocationNull(RandomTPOnPreviousLocationNullEvent event) {
        WorldUtils.handlePlayerRandomTeleport(event);
    }

}
