package com.github.bfabri.hosts.commands.host;

import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.commands.arena.ArenaExecutor;
import com.github.bfabri.hosts.commands.host.arguments.*;
import com.github.bfabri.hosts.commands.team.TeamExecutor;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.commands.utils.framework.BaseCommand;
import com.github.bfabri.hosts.commands.utils.framework.CommandsUtils;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HostExecutor extends BaseCommand {
	private final List<CommandArgument> arguments;

	public HostExecutor() {
		super("host", "Start an event.");
		this.arguments = new ArrayList<>();
		this.setUsage("/(command)");

		this.arguments.add(new JoinArgument());
		this.arguments.add(new LeaveArgument());
		this.arguments.add(new ForceStartArgument());
		this.arguments.add(new StopArgument());
		this.arguments.add(new StartArgument());

		this.arguments.add(new TeamExecutor());
		this.arguments.add(new ArenaExecutor());
		this.onlyPlayer = true;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			if (isOnlyPlayer() && sender instanceof ConsoleCommandSender) {
				sender.sendMessage(ChatColor.RED + "This command is only for players");
				return true;
			}
			Hosts.getInstance().getInventoryManager().openHostAndReloadMenu((Player) sender);
			return true;
		}
		CommandArgument argument = CommandsUtils.matchArgument(args[0], sender, this.arguments);
		if (argument == null) {
			CommandsUtils.printUsage(sender, label, this.arguments);
			return true;
		}
		return argument.onCommand(sender, command, label, args);
	}

	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return Collections.emptyList();
		}
		List<String> results;
		if (args.length <= 1) {
			results = CommandsUtils.getAccessibleArgumentNames(sender, this.arguments);
		} else {
			CommandArgument argument = CommandsUtils.matchArgument(args[0], sender, this.arguments);
			if (argument == null) {
				return Collections.emptyList();
			}
			results = argument.onTabComplete(sender, command, label, args);
		}
		return (results == null) ? null : Utils.getCompletions(args, results);
	}
}
