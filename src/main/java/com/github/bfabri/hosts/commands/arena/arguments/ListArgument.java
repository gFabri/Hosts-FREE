package com.github.bfabri.hosts.commands.arena.arguments;

import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ListArgument extends CommandArgument {
	public ListArgument() {
		super("list", "Show a list of all arenas");
		this.onlyPlayer = true;
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + this.getName();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		sender.sendMessage(Utils.PREFIX + Utils.translate("&eArenas available&7: " + Hosts.getInstance().getArenaManager().getArenas().entrySet().stream().map(hash -> "&e" + hash.getKey() + "&7(&6" + hash.getValue().getGameName() + "&7)").collect(Collectors.joining(", "))));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return Collections.emptyList();
	}
}
