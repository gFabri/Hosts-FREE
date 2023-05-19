package com.github.bfabri.hosts.commands.arena.arguments;

import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.arenas.Arena;
import com.github.bfabri.hosts.arenas.kits.Kit;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.commands.utils.framework.CommandsUtils;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KitArgument extends CommandArgument {

	private final List<CommandArgument> arguments;

	public KitArgument() {
		super("kit", "Manage kits");
		this.onlyPlayer = true;
		arguments = new ArrayList<>();
		arguments.add(new KitAdd());
		arguments.add(new KitRemove());
		arguments.add(new KitList());
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + this.getName();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 3) {
			CommandsUtils.printUsage(sender, label + " kit", this.arguments);
			return true;
		}

		CommandArgument argument = CommandsUtils.matchArgument(args[2], sender, this.arguments);

		if (argument == null) {
			CommandsUtils.printUsage(sender, label + " kit", arguments);
			return true;
		}
		return argument.onCommand(sender, command, label + " kit", args);
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
			results = argument.onTabComplete(sender, command, label + " kit", args);
		}
		return (results == null) ? null : Utils.getCompletions(args, results);
	}

	private static class KitAdd
			extends CommandArgument {

		public KitAdd() {
			super("add", "To add a kit to an arena");

		}

		public String getUsage(String label) {
			return '/' + label + ' ' + getName() + " <arenaName> <modeName>";
		}

		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (args.length < 6) {
				sender.sendMessage(Utils.PREFIX + ChatColor.RED + "Usage: /host arena kit add <arenaName> <kitName> <type>");
				return true;
			}

			Arena arena = Hosts.getInstance().getArenaManager().getArena(args[3]);
			if (arena == null) {
				sender.sendMessage(Utils.PREFIX + Utils.translate("&cThis arena does not exit!"));
				return true;
			}

			String type = args[5].toLowerCase();

			List<String> allowedTypes = new ArrayList<>();
			allowedTypes.add("global");

			if (arena.getGameName().equalsIgnoreCase("FFA")) {
				allowedTypes.addAll(Arrays.asList("builduhc", "soup", "axe", "gapple", "pot", "split"));
			} else if (arena.getGameName().equalsIgnoreCase("1v1")) {
				allowedTypes.addAll(Arrays.asList("builduhc", "soup", "axe", "gapple", "pot"));
			} else if (arena.getGameName().equalsIgnoreCase("Sumo")) {
				allowedTypes.addAll(Arrays.asList("1v1", "2v2", "FFA", "Split"));
			} else if (arena.getGameName().equalsIgnoreCase("Spleef")) {
				allowedTypes.addAll(Arrays.asList("snowball", "walk", "shovel"));
			}

			if (!allowedTypes.contains(type)) {
				sender.sendMessage(Utils.PREFIX + Utils.translate("&cInvalid game type\n&cAvailables: " + String.join(", ", allowedTypes)));
				return true;
			}

			boolean kitExists = arena.getKits().stream().anyMatch(kit -> kit.getName().equalsIgnoreCase(args[4]));
			if (kitExists) {
				sender.sendMessage(Utils.PREFIX + Utils.translate("&cThis kit already exists!"));
				return true;
			}
			Kit kit = new Kit(args[4], ((Player) sender), type);
			arena.getKits().add(kit);
			arena.setHasKit(true);
			sender.sendMessage(Utils.PREFIX + Utils.translate("&eKit &7" + args[4] + " &ewith type &7" + type + " &ehas been added"));
			Hosts.getInstance().getArenaManager().saveArenas();
			return true;
		}

		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 6) {
				Arena arena = Hosts.getInstance().getArenaManager().getArena(args[3]);
				if (arena == null) {
					return Utils.getCompletions(args, "Invalid arena name");
				}

				List<String> allowedTypes = new ArrayList<>();
				allowedTypes.add("global");

				if (arena.getGameName().equalsIgnoreCase("FFA")) {
					allowedTypes.addAll(Arrays.asList("builduhc", "soup", "axe", "gapple", "pot", "split"));
				} else if (arena.getGameName().equalsIgnoreCase("1v1")) {
					allowedTypes.addAll(Arrays.asList("builduhc", "soup", "axe", "gapple", "pot"));
				} else if (arena.getGameName().equalsIgnoreCase("Sumo")) {
					allowedTypes.addAll(Arrays.asList("1v1", "2v2", "FFA", "Split"));
				} else if (arena.getGameName().equalsIgnoreCase("Spleef")) {
					allowedTypes.addAll(Arrays.asList("snowball", "walk", "shovel"));
				}

				return Utils.getCompletions(args, allowedTypes);
			}
			return Hosts.getInstance().getArenaManager().getArenas().values().stream().map(Arena::getArenaName).collect(Collectors.toList());
		}
	}

	private static class KitRemove
			extends CommandArgument {

		public KitRemove() {
			super("remove", "Remove kit from arena modes");
		}

		public String getUsage(String label) {
			return '/' + label + ' ' + getName() + ' ' + "<arenaName> <kitName>";
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
			if (arena.getKits().stream().noneMatch(kit -> kit.getName().equalsIgnoreCase(args[4]))) {
				sender.sendMessage(Utils.PREFIX + Utils.translate("&cThis kit does not exit!"));
				return true;
			}
			arena.getKits().remove(arena.getKits().stream().filter(kit -> kit.getName().equalsIgnoreCase(args[4])).collect(Collectors.toList()).get(0));
			if (arena.getKits().isEmpty()) {
				arena.setHasKit(false);
			}
			sender.sendMessage(Utils.PREFIX + Utils.translate("&eKit &7" + args[4] + " &ehas been removed!"));
			Hosts.getInstance().getArenaManager().saveArenas();
			return true;
		}

		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 5) {
				Arena arena = Hosts.getInstance().getArenaManager().getArena(args[3]);
				if (arena == null) {
					return Utils.getCompletions(args, "Invalid arena name");
				}

				return Utils.getCompletions(args, arena.getKits().stream().map(Kit::getName).collect(Collectors.toList()));
			}
			return Hosts.getInstance().getArenaManager().getArenas().values().stream().map(Arena::getArenaName).collect(Collectors.toList());
		}
	}

	private static class KitList
			extends CommandArgument {

		public KitList() {
			super("list", "Show all kits");
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
			sender.sendMessage(Utils.translate("&c" + arena.getKits().stream().map(Kit::getName).collect(Collectors.joining(","))));
			return true;
		}

		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			return Hosts.getInstance().getArenaManager().getArenas().values().stream().map(Arena::getArenaName).collect(Collectors.toList());
		}
	}
}
