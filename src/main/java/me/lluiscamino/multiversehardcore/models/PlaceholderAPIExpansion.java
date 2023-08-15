package me.lluiscamino.multiversehardcore.models;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.lluiscamino.multiversehardcore.MultiverseHardcore;
import me.lluiscamino.multiversehardcore.exceptions.PlayerNotParticipatedException;
import me.lluiscamino.multiversehardcore.exceptions.WorldIsNotHardcoreException;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static me.lluiscamino.multiversehardcore.utils.WorldUtils.parseTime;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final MultiverseHardcore plugin;

    public PlaceholderAPIExpansion(MultiverseHardcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return "Griffin G.";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mvhc";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        try {
            String[] splitParams = params.split("_");
            if (splitParams.length < 2) return null;
            HardcoreWorld hardcoreWorld = new HardcoreWorld(splitParams[0]);
            World world = hardcoreWorld.getConfiguration().getWorld();

            switch (String.join("_", Arrays.copyOfRange(splitParams, 1, splitParams.length))) {
                case "player_banned" -> {
                    PlayerParticipation participation = new PlayerParticipation(player, world);
                    if (participation.isDeathBanned()) {
                        return ChatColor.RED + "You are banned until " + ChatColor.AQUA + participation.getUnBanDate();
                    } else {
                        return ChatColor.GREEN + "You are not banned";
                    }
                }
                case "start_date" -> {
                    return hardcoreWorld.getConfiguration().getStartDate().toString();
                }
                case "local_time" -> {
                    return parseTime(world.getTime());
                }
            }
        } catch (WorldIsNotHardcoreException ignore) {
            return "World is not hardcore";
        } catch (PlayerNotParticipatedException ignore) {
            return "You have never played hardcore";
        }

        return null; // Placeholder is unknown by the Expansion
    }
}