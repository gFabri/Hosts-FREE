package com.github.bfabri.hosts.commands.arena.arguments;

import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.arenas.Arena;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.commands.utils.framework.CommandsUtils;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PositionArgument extends CommandArgument {

    private final List<CommandArgument> arguments;

    public PositionArgument() {
        super("position", "Manage positions");
        this.onlyPlayer = true;
        arguments = new ArrayList<>();
        arguments.add(new PositionAdd());
        arguments.add(new PositionRemove());
        arguments.add(new PositionList());
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + this.getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            CommandsUtils.printUsage(sender, label + " position", this.arguments);
            return true;
        }

        CommandArgument argument = CommandsUtils.matchArgument(args[2], sender, this.arguments);

        if (argument == null) {
            CommandsUtils.printUsage(sender, label + " position", arguments);
            return true;
        }
        return argument.onCommand(sender, command, label + " position", args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> results;
        if (args.length <= 3) {
            results = CommandsUtils.getAccessibleArgumentNames(sender, this.arguments);
        } else {
            CommandArgument argument = CommandsUtils.matchArgument(args[2], sender, this.arguments);
            if (argument == null) {
                return Collections.emptyList();
            }
            results = argument.onTabComplete(sender, command, label + " position", args);
        }
        return (results == null) ? null : Utils.getCompletions(args, results);
    }

    private static class PositionAdd
            extends CommandArgument {

        public PositionAdd() {
            super("add", "Add a position");
        }

        public String getUsage(String label) {
            return '/' + label + ' ' + getName() + " <arenaName>";
        }

        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	        if (args.length < 4) {
		        sender.sendMessage(Utils.PREFIX + ChatColor.RED + getUsage(label));
		        return true;
	        }

	        Arena arena = Hosts.getInstance().getArenaManager().getArena(args[3]);
	        if (arena == null) {
		        sender.sendMessage(Utils.PREFIX + Utils.translate("&cThis arena does not exit!"));
		        return true;
	        }
	        switch (arena.getGameName()) {
		        case "Sumo":
			        if (arena.getLocations().size() >= 4) {
				        sender.sendMessage(Utils.PREFIX + Utils.translate("&cYou cannot add more positions for this game."));
				        return true;
			        }
			        break;
		        case "1v1":
			        if (arena.getLocations().size() >= 2) {
				        sender.sendMessage(Utils.PREFIX + Utils.translate("&cYou cannot add more positions for this game."));
				        return true;
			        }
			        break;
	        }
	        Location location = ((Player) sender).getLocation();
	        arena.getLocations().add(location.serialize());
	        sender.sendMessage(Utils.PREFIX + Utils.translate("&ePosition &7" + location.getBlockX() + "&e,&7" + location.getBlockY() + "&e,&7" + location.getBlockZ() + " &ehas been added"));
	        Hosts.getInstance().getArenaManager().saveArenas();
	        return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            return Hosts.getInstance().getArenaManager().getArenas().values().stream().map(Arena::getArenaName).collect(Collectors.toList());
        }
    }

    private static class PositionRemove
            extends CommandArgument {

        public PositionRemove() {
            super("remove", "Remove the last position");
        }

        public String getUsage(String label) {
            return '/' + label + ' ' + getName() + ' ' + "<arenaName>";
        }

        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length < 4) {
                sender.sendMessage(Utils.PREFIX + ChatColor.RED + getUsage(label));
                return true;
            }
            Arena arena = Hosts.getInstance().getArenaManager().getArena(args[3]);
            if (arena == null) {
                sender.sendMessage(Utils.PREFIX + Utils.translate("&cThis arena does not exit!"));
                return true;
            }
            if (arena.getLocations().isEmpty()) {
                sender.sendMessage(Utils.PREFIX + Utils.translate("&cThere are no positions to delete."));
                return true;
            }
            Location location = Location.deserialize(arena.getLocations().get(arena.getLocations().size() - 1));
            arena.getLocations().remove((arena.getLocations().size() - 1));
            sender.sendMessage(Utils.PREFIX + Utils.translate("&ePosition &7" + location.getBlockX() + "&e,&7" + location.getBlockY() + "&e,&7" + location.getBlockZ() + " &ehas been removed &c(Last added position)"));
            Hosts.getInstance().getArenaManager().saveArenas();
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            return Hosts.getInstance().getArenaManager().getArenas().values().stream().map(Arena::getArenaName).collect(Collectors.toList());
        }
    }

    private static class PositionList
            extends CommandArgument {

        public PositionList() {
            super("list", "Show all positions");
        }

        public String getUsage(String label) {
            return '/' + label + ' ' + getName() + ' ' + "<arenaName>";
        }

        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length < 4) {
                sender.sendMessage(Utils.PREFIX + ChatColor.RED + getUsage(label));
                return true;
            }
            Arena arena = Hosts.getInstance().getArenaManager().getArena(args[3]);
            if (arena == null) {
                sender.sendMessage(Utils.PREFIX + Utils.translate("&cThis arena does not exit!"));
                return true;
            }
            sender.sendMessage(Utils.translate("&c" + arena.getLocations().stream().map(stringObjectMap -> Utils.translate("&7(&e" + Location.deserialize(stringObjectMap).getBlockX() + ", " + Location.deserialize(stringObjectMap).getBlockY() + ", " + Location.deserialize(stringObjectMap).getBlockZ()) + "&7)").collect(Collectors.joining(","))));
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            return Hosts.getInstance().getArenaManager().getArenas().values().stream().map(Arena::getArenaName).collect(Collectors.toList());
        }
    }
}
