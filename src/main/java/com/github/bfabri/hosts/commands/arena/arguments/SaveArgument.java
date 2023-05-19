package com.github.bfabri.hosts.commands.arena.arguments;

import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class SaveArgument extends CommandArgument {
	public SaveArgument() {
		super("save", "Save arena");
		this.onlyPlayer = true;
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + this.getName();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Hosts.getInstance().getArenaManager().saveArenas();
		sender.sendMessage(Utils.PREFIX + Utils.translate("&eArenas saved!&7"));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return Collections.emptyList();
	}
}
