package com.github.bfabri.hosts.commands.host.arguments;

import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.arenas.Arena;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.game.Game;
import com.github.bfabri.hosts.utils.GameUtils;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StartArgument extends CommandArgument {

    public StartArgument() {
        super("start", "Start a game.");
        this.permission = "hosts.command.host.admin";
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + this.getName() + ' ' + "<game>" + ' ' + "<modeName>" + ' ' + "<reward>" + ' ' + "<map>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "This command is only for console");
            return true;
        }
        if (args.length < 5) {
            sender.sendMessage(Utils.PREFIX + ChatColor.RED + getUsage(label));
            return true;
        }

        Arena arena = Hosts.getInstance().getArenaManager().getArena(args[4]);
        String gameName = args[1];
        String modeName = args[2];
        String rewardType = args[3];

        if (Arrays.stream(GameUtils.games).noneMatch(game -> game.equalsIgnoreCase(gameName))) {
            Bukkit.getConsoleSender().sendMessage(Utils.PREFIX + Utils.translate("&c" + gameName + " does not exist!"));
            return true;
        }

        if (arena == null) {
            Bukkit.getConsoleSender().sendMessage(Utils.PREFIX + Utils.translate("&cThis arena does not exit!"));
            return true;
        }

        if (!modeName.equalsIgnoreCase("NONE") && !arena.getModesName().contains(modeName.toUpperCase())) {
            Bukkit.getConsoleSender().sendMessage(Utils.PREFIX + Utils.translate("&cInvalid modeName"));
            return true;
        }

        if (Arrays.stream(Game.RewardTypes.values()).noneMatch(rewardTypes -> rewardTypes.name().equalsIgnoreCase(rewardType))) {
            Bukkit.getConsoleSender().sendMessage(Utils.PREFIX + Utils.translate("&c" + rewardType + " this type of reward does not exist!"));
            return true;
        }

        Hosts.getInstance().getGameManager().setRewardTypes(Game.RewardTypes.valueOf(rewardType.toUpperCase()));
        Hosts.getInstance().getGameManager().setSelected(arena);
        Hosts.getInstance().getGameManager().newGameWithMode(gameName, modeName, sender);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 3) {
            if (args[1].equalsIgnoreCase("Sumo")) {
                return Utils.getCompletions(args, GameUtils.sumoModes);
            } else if (args[1].equalsIgnoreCase("FFA")) {
                return Utils.getCompletions(args, GameUtils.ffaModes);
            } else if (args[1].equalsIgnoreCase("1v1")) {
                return Utils.getCompletions(args, GameUtils.oneVSoneModes);
            }
            if (args[1].equalsIgnoreCase("Spleef")) {
                return Utils.getCompletions(args, GameUtils.spleefModes);
            } else {
                return Utils.getCompletions(args, "NONE");
            }
        }
        if (args.length == 4) {
            return Utils.getCompletions(args, Arrays.stream(Game.RewardTypes.values()).map(Enum::name).collect(Collectors.toList()));
        }
        if (args.length == 5) {
            return Utils.getCompletions(args, new ArrayList<>(Hosts.getInstance().getArenaManager().getArenas().values().stream().filter(arena -> arena.getModesName().contains(args[2].toUpperCase())).map(Arena::getArenaName).collect(Collectors.toList())));
        }
        return Utils.getCompletions(args, GameUtils.games);
    }
}
