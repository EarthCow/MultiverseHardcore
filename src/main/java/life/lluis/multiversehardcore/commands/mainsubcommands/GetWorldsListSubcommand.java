package life.lluis.multiversehardcore.commands.mainsubcommands;

import life.lluis.multiversehardcore.commands.MainSubcommand;
import life.lluis.multiversehardcore.exceptions.InvalidCommandInputException;
import life.lluis.multiversehardcore.models.HardcoreWorld;
import life.lluis.multiversehardcore.utils.MessageSender;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GetWorldsListSubcommand extends MainSubcommand {
    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        try {
            initProperties(sender, args);
            checkSenderIsOp();
            MessageSender.sendInfo(sender, "Worlds list: ");
            List<HardcoreWorld> hcWorlds = HardcoreWorld.getHardcoreWorlds();
            StringBuilder worldsListMessage = new StringBuilder();
            for (HardcoreWorld hcWorld : hcWorlds) {
                appendWorldInfo(worldsListMessage, hcWorld);
            }
            sender.sendMessage(worldsListMessage.toString());
        } catch (InvalidCommandInputException e) {
            MessageSender.sendError(sender, e.getMessage());
        }
    }

    private void appendWorldInfo(@NotNull StringBuilder worldsListMessage, @NotNull HardcoreWorld hcWorld) {
        String worldName = hcWorld.getConfiguration().getWorld().getName();
        worldsListMessage.append(worldName);
        worldsListMessage.append("\n");
        if (hcWorld.getConfiguration().hasNether()) {
            worldsListMessage.append("\t").append(worldName).append("_nether\n");
        }
        if (hcWorld.getConfiguration().hasTheEnd()) {
            worldsListMessage.append("\t").append(worldName).append("_the_end\n");
        }
    }
}
