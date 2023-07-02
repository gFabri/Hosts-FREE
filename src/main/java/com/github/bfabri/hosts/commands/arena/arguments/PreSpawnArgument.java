package com.github.bfabri.hosts.commands.arena.arguments;

import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.arenas.Arena;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.utils.Cuboid;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class PreSpawnArgument extends CommandArgument {
	public PreSpawnArgument() {
		super("prespawn", "Set the pre-spawn for an arena");
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
			if (arena.getArea() == null) {
				sender.sendMessage(Utils.PREFIX + Utils.translate("&cArena location has not been set!"));
				return true;
			}
			Cuboid arenaCuboid = new Cuboid(arena.getArea());
			if (arenaCuboid.contains(((Player) sender).getLocation())) {
				arena.setPreSpawn(((Player) sender).getLocation().serialize());
				sender.sendMessage(Utils.PREFIX + Utils.translate("&ePreSpawn location for arena &7" + args[2] + " &ehas been set!"));
				Hosts.getInstance().getArenaManager().saveArenas();
			} else {
				sender.sendMessage(Utils.PREFIX + Utils.translate("&cYou have to set the prespawn inside the arena."));
			}
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
