package com.github.bfabri.hosts.commands.arena.arguments;

import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.arenas.Arena;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class SaveArgument extends CommandArgument {
	public SaveArgument() {
		super("save", "Save arena");
		this.onlyPlayer = true;
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + this.getName() + ' ' + "<arena>";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 3) {
			sender.sendMessage(Utils.PREFIX + ChatColor.RED + getUsage(label));
			return true;
		}

		Arena arena = Hosts.getInstance().getArenaManager().getArena(args[2]);
		if (arena != null) {
			arena.setConfigured(arena.getArea() != null &&
					arena.getPreSpawn() != null &&
					arena.getServerSpawn() != null &&
					arena.getWorldName() != null &&
					((!arena.getGameName().equalsIgnoreCase("Sumo") &&
							!arena.getGameName().equalsIgnoreCase("FFA") &&
							!arena.getGameName().equalsIgnoreCase("1v1")) ||
							!arena.getModesName().isEmpty()));

			if (!arena.isConfigured()) {
				sender.sendMessage(Utils.PREFIX + Utils.translate("&a" + arena.getArenaName() + "&c is misconfigured"));
				return true;
			}
			sender.sendMessage(Utils.PREFIX + Utils.translate("&eArenas saved!&7"));
			Hosts.getInstance().getArenaManager().saveArenas();
		} else {
			sender.sendMessage(Utils.PREFIX + Utils.translate("&cThis arena does not exit!"));
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return Hosts.getInstance().getArenaManager().getArenas().values().stream().map(Arena::getArenaName).collect(Collectors.toList());
	}
}