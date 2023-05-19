package com.github.bfabri.hosts.commands.arena.arguments;

import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.arenas.Arena;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.commands.utils.framework.CommandsUtils;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModesArgument extends CommandArgument {

	private final List<CommandArgument> arguments;

	public ModesArgument() {
		super("modes", "Manage modes");
		this.onlyPlayer = true;
		arguments = new ArrayList<>();
		arguments.add(new ModesAdd());
		arguments.add(new ModesRemove());
		arguments.add(new ModesList());
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + this.getName();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 3) {
			CommandsUtils.printUsage(sender, label + " modes", this.arguments);
			return true;
		}

		CommandArgument argument = CommandsUtils.matchArgument(args[2], sender, this.arguments);

		if (argument == null) {
			CommandsUtils.printUsage(sender, label + " modes", arguments);
			return true;
		}
		return argument.onCommand(sender, command, label + " modes", args);
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
			results = argument.onTabComplete(sender, command, label + " modes", args);
		}
		return (results == null) ? null : Utils.getCompletions(args, results);
	}

	private static class ModesAdd
			extends CommandArgument {

		public ModesAdd() {
			super("add", "To add a mode to an arena");

		}

		public String getUsage(String label) {
			return '/' + label + ' ' + getName() + " <arenaName> <modeName>";
		}

		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (args.length < 5) {
				sender.sendMessage(Utils.PREFIX + ChatColor.RED + getUsage(label));
				return true;
			}

			Arena arena = Hosts.getInstance().getArenaManager().getArena(args[3]);
			if (arena == null) {
				sender.sendMessage(Utils.PREFIX + Utils.translate("&cThis arena does not exit!"));
				return true;
			}

			String type = args[4].toUpperCase();

			if (arena.getModesName().contains(type)) {
				sender.sendMessage(Utils.PREFIX + Utils.translate("&cThis mode name already exist!"));
				return true;
			}

			List<String> allowedTypes = new ArrayList<>();
			allowedTypes.add("ALL");

			if (arena.getGameName().equalsIgnoreCase("Sumo")) {
				allowedTypes.addAll(Arrays.asList("1V1", "2V2", "FFA", "SPLIT"));
			} else if (arena.getGameName().equalsIgnoreCase("FFA")) {
				allowedTypes.addAll(Arrays.asList("POT", "GAPPLE", "AXE", "SOUP", "BUILDUHC", "SPLIT"));
			} else if (arena.getGameName().equalsIgnoreCase("1v1")) {
				allowedTypes.addAll(Arrays.asList("POT", "GAPPLE", "AXE", "SOUP", "BUILDUHC"));
			} else if (arena.getGameName().equalsIgnoreCase("Spleef")) {
				allowedTypes.addAll(Arrays.asList("SHOVEL", "SNOWBALL", "WALK"));
			}

			if (!allowedTypes.contains(type)) {
				sender.sendMessage(Utils.PREFIX + Utils.translate("&cInvalid modeName type\n&cAvailables: " + String.join(", ", allowedTypes)));
				return true;
			}

			arena.getModesName().add(type.toUpperCase());

			sender.sendMessage(Utils.PREFIX + Utils.translate("&eMode &7" + type + " &ehas been added to arena &7" + arena.getArenaName()));
			Hosts.getInstance().getArenaManager().saveArenas();
			return true;
		}

		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 5) {
				List<String> allowedTypes = new ArrayList<>();
				allowedTypes.add("ALL");

				Arena arena = Hosts.getInstance().getArenaManager().getArena(args[3]);
				if (arena == null) {
					return Utils.getCompletions(args, "Invalid arena name");
				}

				if (arena.getGameName().equalsIgnoreCase("Sumo")) {
					allowedTypes.addAll(Arrays.asList("1V1", "2V2", "FFA", "SPLIT"));
				} else if (arena.getGameName().equalsIgnoreCase("FFA")) {
					allowedTypes.addAll(Arrays.asList("POT", "GAPPLE", "AXE", "SOUP", "BUILDUHC", "SPLIT"));
				} else if (arena.getGameName().equalsIgnoreCase("1v1")) {
					allowedTypes.addAll(Arrays.asList("POT", "GAPPLE", "AXE", "SOUP", "BUILDUHC"));
				} else if (arena.getGameName().equalsIgnoreCase("Spleef")) {
					allowedTypes.addAll(Arrays.asList("SHOVEL", "SNOWBALL", "WALK"));
				}
				return Utils.getCompletions(args, allowedTypes);
			}
			return Hosts.getInstance().getArenaManager().getArenas().values().stream().map(Arena::getArenaName).collect(Collectors.toList());
		}
	}

	private static class ModesRemove
			extends CommandArgument {

		public ModesRemove() {
			super("remove", "Remove mode from arena modes");
		}

		public String getUsage(String label) {
			return '/' + label + ' ' + getName() + ' ' + "<arenaName> <modeName>";
		}

		List<String> allowedTypes;

		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (args.length < 5) {
				sender.sendMessage(Utils.PREFIX + ChatColor.RED + getUsage(label));
				return true;
			}

			Arena arena = Hosts.getInstance().getArenaManager().getArena(args[3]);
			if (arena == null) {
				sender.sendMessage(Utils.PREFIX + Utils.translate("&cThis arena does not exit!"));
				return true;
			}

			String type = args[4].toUpperCase();
			if (!arena.getModesName().contains(type)) {
				sender.sendMessage(Utils.PREFIX + Utils.translate("&cThis mode name does not exist!"));
				return true;
			}

			allowedTypes = new ArrayList<>();
			allowedTypes.add("ALL");

			if (arena.getGameName().equalsIgnoreCase("Sumo")) {
				allowedTypes.addAll(Arrays.asList("1V1", "2V2", "FFA", "SPLIT"));
			} else if (arena.getGameName().equalsIgnoreCase("FFA")) {
				allowedTypes.addAll(Arrays.asList("POT", "GAPPLE", "AXE", "SOUP", "BUILDUHC", "SPLIT"));
			} else if (arena.getGameName().equalsIgnoreCase("1v1")) {
				allowedTypes.addAll(Arrays.asList("POT", "GAPPLE", "AXE", "SOUP", "BUILDUHC"));
			} else if (arena.getGameName().equalsIgnoreCase("Spleef")) {
				allowedTypes.addAll(Arrays.asList("SHOVEL", "SNOWBALL", "WALK"));
			}

			if (!allowedTypes.contains(type)) {
				sender.sendMessage(Utils.PREFIX + Utils.translate("&cInvalid modeName type\n&cAvailables: " + String.join(", ", allowedTypes)));
				return true;
			}

			arena.getModesName().remove(type);

			sender.sendMessage(Utils.PREFIX + Utils.translate("&eMode &7" + type + " &ehas been removed from arena &7" + arena.getArenaName()));
			Hosts.getInstance().getArenaManager().saveArenas();
			return true;
		}

		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 5) {
				List<String> allowedTypes = new ArrayList<>();
				allowedTypes.add("ALL");

				Arena arena = Hosts.getInstance().getArenaManager().getArena(args[3]);
				if (arena == null) {
					return Utils.getCompletions(args, "Invalid arena name");
				}

				if (arena.getGameName().equalsIgnoreCase("Sumo")) {
					allowedTypes.addAll(Arrays.asList("1V1", "2V2", "FFA", "SPLIT"));
				} else if (arena.getGameName().equalsIgnoreCase("FFA")) {
					allowedTypes.addAll(Arrays.asList("POT", "GAPPLE", "AXE", "SOUP", "BUILDUHC", "SPLIT"));
				} else if (arena.getGameName().equalsIgnoreCase("1v1")) {
					allowedTypes.addAll(Arrays.asList("POT", "GAPPLE", "AXE", "SOUP", "BUILDUHC"));
				} else if (arena.getGameName().equalsIgnoreCase("Spleef")) {
					allowedTypes.addAll(Arrays.asList("SHOVEL", "SNOWBALL", "WALK"));
				}
				return Utils.getCompletions(args, allowedTypes);
			}
			return Hosts.getInstance().getArenaManager().getArenas().values().stream().map(Arena::getArenaName).collect(Collectors.toList());
		}
	}

	private static class ModesList
			extends CommandArgument {

		public ModesList() {
			super("list", "Show all modes");
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
			sender.sendMessage(Utils.translate("&c" + String.join("&7, &c", arena.getModesName())));
			return true;
		}

		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			return Hosts.getInstance().getArenaManager().getArenas().values().stream().map(Arena::getArenaName).collect(Collectors.toList());
		}
	}
}
