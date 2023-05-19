package com.github.bfabri.hosts.commands.team;

import com.github.bfabri.hosts.commands.team.arguments.AcceptArgument;
import com.github.bfabri.hosts.commands.team.arguments.CreateArgument;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.commands.utils.framework.CommandsUtils;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeamExecutor extends CommandArgument {
	private final List<CommandArgument> arguments;

	public TeamExecutor() {
		super("team", "Commands to create and accept teams");
		this.arguments = new ArrayList<>();
		this.arguments.add(new CreateArgument());
		this.arguments.add(new AcceptArgument());
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + this.getName();
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length <= 1) {
			CommandsUtils.printUsage(sender, label + " team", this.arguments);
			return true;
		}

		CommandArgument argument = CommandsUtils.matchArgument(args[1], sender, this.arguments);

		if (argument == null) {
			CommandsUtils.printUsage(sender, label + " team", arguments);
			return true;
		}
		return argument.onCommand(sender, command, label + " team", args);
	}

	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return Collections.emptyList();
		}
		List<String> results;
		if (args.length <= 2) {
			results = CommandsUtils.getAccessibleArgumentNames(sender, this.arguments);
		} else {
			CommandArgument argument = CommandsUtils.matchArgument(args[1], sender, this.arguments);
			if (argument == null) {
				return Collections.emptyList();
			}
			results = argument.onTabComplete(sender, command, label + " team", args);
		}
		return (results == null) ? null : Utils.getCompletions(args, results);
	}
}