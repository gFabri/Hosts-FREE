package com.github.bfabri.hosts.commands.arena;

import com.github.bfabri.hosts.commands.arena.arguments.*;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.commands.utils.framework.CommandsUtils;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArenaExecutor extends CommandArgument {
	private final List<CommandArgument> arguments;

	public ArenaExecutor() {
		super("arena", "Arena configuration");
		this.permission = "hosts.command.arena";
		this.arguments = new ArrayList<>();
		this.arguments.add(new CreateArgument());
		this.arguments.add(new DeleteArgument());
		this.arguments.add(new KitArgument());
		this.arguments.add(new ListArgument());
		this.arguments.add(new LocationArgument());
		this.arguments.add(new ModesArgument());
		this.arguments.add(new PositionArgument());
		this.arguments.add(new PreSpawnArgument());
		this.arguments.add(new SpawnArgument());
		this.arguments.add(new SpectatorArgument());
		this.arguments.add(new SaveArgument());
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + this.getName();
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length <= 1) {
			CommandsUtils.printUsage(sender, label + " arena", this.arguments);
			return true;
		}

		CommandArgument argument = CommandsUtils.matchArgument(args[1], sender, this.arguments);

		if (argument == null) {
			CommandsUtils.printUsage(sender, label + " arena", arguments);
			return true;
		}
		return argument.onCommand(sender, command, label + " arena", args);
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
			results = argument.onTabComplete(sender, command, label + " arena", args);
		}
		return (results == null) ? null : Utils.getCompletions(args, results);
	}
}