package com.github.bfabri.hosts.commands.arena.arguments;

import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.arenas.Arena;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.utils.CustomItem;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.stream.Collectors;

public class LocationArgument extends CommandArgument {
	public LocationArgument() {
		super("location", "Join a game");
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

		if (Hosts.getInstance().getArenaManager().getArena(args[2]) != null) {
			((Player) sender).getInventory().addItem(new CustomItem(Material.STICK, 1, 0).setName("&eSet corners location").addLore("&fLeft click for corner #1", "&fRight click for corner #2").create());
			((Player) sender).setMetadata("arenaName", new FixedMetadataValue(Hosts.getInstance(), args[2]));
			sender.sendMessage(Utils.PREFIX + Utils.translate("&eYou have been given the items"));
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
