package com.github.bfabri.hosts.commands.arena.arguments;

import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.arenas.Arena;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class SpawnArgument extends CommandArgument {
	public SpawnArgument() {
		super("spawn", "Set the server spawn for an arena");
		this.onlyPlayer = true;
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + this.getName() + ' ' + "<arenaName>";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 3) {
			sender.sendMessage(Utils.PREFIX + ChatColor.RED + getUsage(label));
			return true;
		}

		Arena arena = Hosts.getInstance().getArenaManager().getArena(args[2]);
		if (arena != null) {
			arena.setServerSpawn(((Player) sender).getLocation().serialize());
			sender.sendMessage(Utils.PREFIX + Utils.translate("&eServer spawn location for arena &7" + args[2] + " &ehas been set!"));
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
