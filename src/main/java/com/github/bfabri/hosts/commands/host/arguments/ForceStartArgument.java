package com.github.bfabri.hosts.commands.host.arguments;

import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class ForceStartArgument extends CommandArgument {
	public ForceStartArgument() {
		super("forcestart", "Force start game");
		this.permission = "hosts.command.host.admin";
		this.onlyPlayer = true;
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + this.getName();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (Hosts.getInstance().getGameManager().isGameAvailable()) {
			Hosts.getInstance().getGameManager().getGame().setStartTime(5);
			sender.sendMessage(Utils.translate(Utils.PREFIX + "&eGame has been forced to start!"));
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return Collections.emptyList();
	}
}
