package com.github.bfabri.hosts.commands.utils.framework;

import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CommandsUtils {

	public static void printUsage(CommandSender sender, String label, Collection<CommandArgument> arguments) {
		sender.sendMessage(ChatColor.DARK_GRAY + "----------------------------------------------");
		for (CommandArgument argument : arguments) {
			String permission = argument.getPermission();
			if (argument.getDescription() != null) {
				if (permission == null || sender.hasPermission(permission)) {
					ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, argument.getUsage(label));
					String message = Utils.translate(String.format("&e%s &7- &f%s", argument.getUsage(label), argument.getDescription()));
					TextComponent component = new TextComponent(TextComponent.fromLegacyText(message));
					component.setClickEvent(clickEvent);
					if (sender instanceof Player) {
						((Player) sender).spigot().sendMessage(component);
					} else {
						sender.sendMessage(BaseComponent.toLegacyText(component));
					}
				}
			}
		}
		sender.sendMessage(ChatColor.DARK_GRAY + "----------------------------------------------");
	}

	public static CommandArgument matchArgument(final String id, final CommandSender sender, final Collection<CommandArgument> arguments) {
		for (final CommandArgument argument : arguments) {
			final String permission = argument.getPermission();
			if ((permission == null || sender.hasPermission(permission)) && (argument.getName().equalsIgnoreCase(id) || Arrays.asList(argument.getAliases()).contains(id))) {
				return argument;
			}
		}
		return null;
	}

	public static List<String> getAccessibleArgumentNames(final CommandSender sender, final Collection<CommandArgument> arguments) {
		final List<String> results = new ArrayList<String>();
		for (final CommandArgument argument : arguments) {
			final String permission = argument.getPermission();
			if (permission == null || sender.hasPermission(permission)) {
				results.add(argument.getName());
			}
		}
		return results;
	}
}
