package com.github.bfabri.hosts.commands.arena.arguments;

import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.utils.GameUtils;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CreateArgument extends CommandArgument {
	public CreateArgument() {
		super("create", "Create an arena for a game");
		this.onlyPlayer = true;
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + this.getName() + ' ' + "<arenaName> <game>";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 4) {
			sender.sendMessage(Utils.PREFIX + ChatColor.RED + getUsage(label));
			return true;
		}

		if (!Arrays.stream(GameUtils.games).collect(Collectors.toList()).contains(args[3])) {
			sender.sendMessage(Utils.PREFIX + Utils.translate("&cThe event " + args[3] + " does not exit"));
			return true;
		}
		if (Hosts.getInstance().getArenaManager().getArena(args[2]) == null) {
			Hosts.getInstance().getArenaManager().createArena(args[2], args[3]);
			sender.sendMessage(Utils.PREFIX + Utils.translate("&eArena &7" + args[2] + " &ehas been created for the &c" + args[3] + " &eevent"));
		} else {
			sender.sendMessage(Utils.PREFIX + Utils.translate("&cThis arena already exit!"));
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return Arrays.asList(GameUtils.games);
	}
}
