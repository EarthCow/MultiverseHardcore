package me.lluiscamino.multiversehardcore.utils;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import me.lluiscamino.multiversehardcore.MultiverseHardcore;
import me.lluiscamino.multiversehardcore.exceptions.PlayerNotParticipatedException;
import me.lluiscamino.multiversehardcore.exceptions.PlayerParticipationAlreadyExistsException;
import me.lluiscamino.multiversehardcore.exceptions.WorldIsNotHardcoreException;
import me.lluiscamino.multiversehardcore.models.HardcoreWorld;
import me.lluiscamino.multiversehardcore.models.PlayerParticipation;
import nl.zandervdm.stayput.CustomEvents.RandomTPOnPreviousLocationNullEvent;
import nl.zandervdm.stayput.Main;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

public final class WorldUtils {

    private WorldUtils() {
    }

    public static boolean worldExists(@NotNull String worldName) {
        return getServer().getWorld(worldName) != null;
    }

    public static boolean worldIsHardcore(@NotNull World world) {
        try {
            new HardcoreWorld(world.getName());
            return true;
        } catch (WorldIsNotHardcoreException e) {
            return false;
        }
    }

    public static boolean playerIsInWorld(Player player, World world) {
        String worldName = world.getName();
        String playerWorldName = player.getWorld().getName();
        return worldName.equals(playerWorldName);
    }

    public static World getNormalWorld(@NotNull World world) {
        try {
            if (world.getEnvironment() == World.Environment.NORMAL) return world;
            MultiverseNetherPortals netherPortals = (MultiverseNetherPortals) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-NetherPortals");
            if (netherPortals != null) {
                String[] worldLinks = netherPortals.getWorldLinks().toString().replace("{","").replace("}","").replace(" ", "").split(",");
                String worldLink = Arrays.stream(worldLinks).filter(link -> link.contains(world.getName())).findFirst().orElse(null);
                if (worldLink == null) {
                    worldLinks = netherPortals.getEndWorldLinks().toString().replace("{","").replace("}","").replace(" ", "").split(",");
                    worldLink = Arrays.stream(worldLinks).filter(link -> link.contains(world.getName())).findFirst().orElse(null);
                }
                if (worldLink != null) {
                    World normalWorld = getServer().getWorld(worldLink.replace(world.getName(), "").replace("=", ""));
                    if (normalWorld == null) return world;
                    HardcoreWorld hcWorld = new HardcoreWorld(normalWorld.getName());
                    boolean includeNetherOrEnd = world.getEnvironment() == World.Environment.NETHER ?
                            hcWorld.getConfiguration().isIncludeNether() : hcWorld.getConfiguration().isIncludeEnd();
                    return !includeNetherOrEnd ? world : normalWorld;
                }
            }
            if (hasSuffix(world)) {
                World normalWorld = getServer().getWorld(removeWorldSuffix(world));
                if (normalWorld == null) return world;
                HardcoreWorld hcWorld = new HardcoreWorld(normalWorld.getName());
                boolean includeNetherOrEnd = world.getEnvironment() == World.Environment.NETHER ?
                        hcWorld.getConfiguration().isIncludeNether() : hcWorld.getConfiguration().isIncludeEnd();
                return !includeNetherOrEnd ? world : normalWorld;
            }
            return world;
        } catch (WorldIsNotHardcoreException e) {
            return world;
        }
    }

    public static boolean respawnPlayer(@NotNull Player player) {
        return respawnPlayer(player, null);
    }

    public static boolean respawnPlayer(@NotNull Player player, @Nullable HardcoreWorld hardcoreWorld) {
        try {
            String worldName = getNormalWorld(player.getWorld()).getName();
            HardcoreWorld actualHardcoreWorld = (hardcoreWorld == null ? new HardcoreWorld(worldName) : hardcoreWorld);
            World respawnWorld = actualHardcoreWorld.getConfiguration().getSpawnWorld();
            if (respawnWorld == null) {
                getLogger().warning("Respawn world does not exist!");
                return false;
            }
            boolean teleportResponse = player.teleport(respawnWorld.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            if (MultiverseHardcore.getInstance().getServer().getPluginManager().getPlugin("StayPut") instanceof Main stayPut) {
                stayPut.getDatabase().deleteLocation(player, actualHardcoreWorld.getConfiguration().getWorld());
            }
            return teleportResponse;
        } catch (WorldIsNotHardcoreException e) {
            return false; // This cannot happen. If player is not in a hardcore world, this function will not be called.
        }
    }

    public static void handlePlayerRandomTeleport(@NotNull RandomTPOnPreviousLocationNullEvent event) {
        try {
            Player player = event.getPlayer();
            World world = getNormalWorld(event.getToWorld());
            if (!worldIsHardcore(world)) {
                setGameModeBackToDefaultIfNecessary(player, world);
                return;
            }
            addPlayerParticipationIfNotExists(player, world);
            PlayerParticipation participation = new PlayerParticipation(player, world);
            if (participation.isDeathBanned()) {
                // no need to send any messages as the normal teleport will do that
                event.setCancelled(true);
            }
        } catch (PlayerNotParticipatedException | WorldIsNotHardcoreException ignored) {
        } // This cannot happen.
    }

    public static void handlePlayerNormalTeleport(@NotNull PlayerTeleportEvent event) {
        try {
            Player player = event.getPlayer();
            if (event.getFrom().getWorld() == null || event.getTo() == null || event.getTo().getWorld() == null)
                return;
            if (event.getFrom().getWorld().equals(event.getTo().getWorld())) {
                //teleport occurred within the same world no need to run
                return;
            }
            World world = getNormalWorld(event.getTo().getWorld());
            if (!worldIsHardcore(world)) {
                if (worldIsHardcore(getNormalWorld(event.getFrom().getWorld()))) {
                    // If they came from a hardcore world then reset the texture pack
                    MultiverseHardcore plugin = MultiverseHardcore.getInstance();
                    String resourceURL = plugin.getConfig().getString("HardcoreResourcePackURL");
                    if (resourceURL != null && !resourceURL.isEmpty()) {
                        player.setResourcePack("http://cdn.moep.tv/files/Empty.zip");
                    }
                }
//                setGameModeBackToDefaultIfNecessary(player, world);
                return;
            }
            addPlayerParticipationIfNotExists(player, world);
            PlayerParticipation participation = new PlayerParticipation(player, world);
            if (participation.isDeathBanned()) {
                sendYouCantPlayMessage(participation);
                event.setCancelled(true);
                preventPlayerEnterWorld(participation);
            } else {
                setGameModeBackToDefaultIfNecessary(player, world);
                sendEnteringWorldMessage(player);
            }
        } catch (PlayerNotParticipatedException | WorldIsNotHardcoreException ignored) {
        } // This cannot happen.
    }

    public static void handlePlayerEnterWorld(@NotNull Event event) {
        try {
            Player player = event instanceof PlayerEvent ? ((PlayerEvent) event).getPlayer() : ((PlayerDeathEvent) event).getEntity();
            World world = getNormalWorld(player.getWorld());
            if (!worldIsHardcore(world)) {
                setGameModeBackToDefaultIfNecessary(player, world);
                return;
            }
            addPlayerParticipationIfNotExists(player, world);
            PlayerParticipation participation = new PlayerParticipation(player, world);
            if (participation.isDeathBanned()) {
                sendYouCantPlayMessage(participation);
                preventPlayerEnterWorld(participation);
            } else {
                setGameModeBackToDefaultIfNecessary(player, world);
                sendEnteringWorldMessage(player);
            }
        } catch (PlayerNotParticipatedException | WorldIsNotHardcoreException ignored) {
        } // This cannot happen.
    }

    private static FileConfiguration getConfig() {
        return MultiverseHardcore.getInstance().getConfig();
    }

    private static Logger getLogger() {
        return MultiverseHardcore.getInstance().getLogger();
    }

    private static Server getServer() {
        return MultiverseHardcore.getInstance().getServer();
    }

    private static boolean hasSuffix(@NotNull World world) {
        if (world.getEnvironment() == World.Environment.NORMAL) return false;
        String suffix = getWorldSuffix(world.getEnvironment());
        String worldName = world.getName();
        return worldName.endsWith(suffix);
    }

    private static String removeWorldSuffix(@NotNull World world) {
        if (world.getEnvironment() == World.Environment.NORMAL) return world.getName();
        String suffix = getWorldSuffix(world.getEnvironment());
        String worldName = world.getName();
        return worldName.substring(0, worldName.length() - suffix.length());
    }

    private static String getWorldSuffix(@NotNull World.Environment environment) {
        return environment == World.Environment.NETHER ? "_nether" : "_the_end";
    }

    public static void addPlayerParticipationIfNotExists(@NotNull Player player, @NotNull World world) {
        try {
            PlayerParticipation.addPlayerParticipation(player, world, new Date());
        } catch (PlayerParticipationAlreadyExistsException ignored) {
        }
    }

    private static void sendYouCantPlayMessage(@NotNull PlayerParticipation participation) {
        String message = participation.isBannedForever() ? "You can't play in this world since you died" :
                ChatColor.GRAY + "You cannot join the " + ChatColor.RED + "hardcore" + ChatColor.GRAY + " world again until " + ChatColor.AQUA + participation.getUnBanDate();
        MessageSender.sendNormal(participation.getPlayer(), message);
    }

    private static void sendEnteringWorldMessage(@NotNull Player player) {
        MultiverseHardcore plugin = MultiverseHardcore.getInstance();
        String resourceURL = plugin.getConfig().getString("HardcoreResourcePackURL");
        if (resourceURL != null && !resourceURL.isEmpty()) {
            player.setResourcePack(resourceURL);
        }
        player.setInvulnerable(true);
        player.setCanPickupItems(false);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.setInvulnerable(false);
            player.setCanPickupItems(true);
        }, 100); // 100 ticks 20 per second 5 seconds of invulnerability
        MessageSender.sendNormal(player, "You have entered a " + ChatColor.RED + "HARDCORE" + ChatColor.RESET + " world, be careful!");
    }

    public static void preventPlayerEnterWorld(@NotNull PlayerParticipation participation) {
        int enterWorldTicks = getConfig().getInt("enter_world_ticks");
        HardcoreWorld hcWorld = participation.getHcWorld();
        Player player = participation.getPlayer();
        MultiverseHardcore plugin = MultiverseHardcore.getInstance();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (hcWorld.getConfiguration().isSpectatorMode()) {
                player.setGameMode(GameMode.SPECTATOR);
            } else {
                respawnPlayer(player, hcWorld);
            }
        }, enterWorldTicks);
    }

    private static GameMode getDefaultGameMode(World world) {
        MVWorldManager worldManager = MultiverseHardcore.getInstance().getMVWorldManager();
        MultiverseWorld multiverseWorld = worldManager.getMVWorld(world);
        return multiverseWorld.getGameMode();
    }

    private static void setGameModeBackToDefaultIfNecessary(Player player, World world) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(getDefaultGameMode(world));
        }
    }

    public static String parseTime(long time) {
        long hours = time / 1000 + 6;
        long minutes = (time % 1000) * 60 / 1000;
        String ampm = "AM";
        if (hours >= 12) {
            hours -= 12;
            ampm = "PM";
        }
        if (hours >= 12) {
            hours -= 12;
            ampm = "AM";
        }
        if (hours == 0) hours = 12;
        String mm = "0" + minutes;
        mm = mm.substring(mm.length() - 2, mm.length());
        return hours + ":" + mm + " " + ampm;
    }
}
