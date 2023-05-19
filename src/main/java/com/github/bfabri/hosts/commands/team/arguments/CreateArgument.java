package com.github.bfabri.hosts.commands.team.arguments;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.game.GamePlayer;
import com.github.bfabri.hosts.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CreateArgument extends CommandArgument {
    public CreateArgument() {
        super("create", "Create a team invitation to someone");
        this.onlyPlayer = true;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + this.getName() + ' ' + "<player>";
    }

    @Override
    public boolean onCommand(CommandSender player, Command command, String label, String[] args) {
	    if (args.length < 3) {
		    player.sendMessage(Utils.PREFIX + ChatColor.RED + getUsage(label));
		    return true;
	    }

	    Player playerTeam = Bukkit.getPlayer(args[2]);

	    if (playerTeam == null) {
		    player.sendMessage(Utils.PREFIX + Utils.translate("&cThis player does not exit!"));
		    return true;
	    }

        if (playerTeam == player) {
            player.sendMessage(Utils.PREFIX + Utils.translate("&cYou cannot invite yourself!"));
            return true;
        }

        GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(playerTeam);
        GamePlayer sender = Hosts.getInstance().getGameManager().getPlayer((Player) player);

        if (gamePlayer == null) {
            return true;
        }

        if (Hosts.getInstance().getTeamManager().getTeams().stream().anyMatch(team -> team.getTeamPlayers().contains(sender))) {
            player.sendMessage(Utils.PREFIX + Utils.translate("&cYou already have a team!"));
            return true;
        }

        if (Hosts.getInstance().getTeamManager().getInvitations().containsKey(sender.getPlayer().getUniqueId())) {
            player.sendMessage(Utils.PREFIX + Utils.translate("&cYou have already sent an invitation."));
            return true;
        }

        if (Hosts.getInstance().getTeamManager().getInvitations().containsValue(gamePlayer.getPlayer().getUniqueId())) {
            player.sendMessage(Utils.PREFIX + Utils.translate("&cThis player already has an invitation."));
            return true;
        }

        Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> {
            if (Hosts.getInstance().getTeamManager().getInvitations().containsKey(((Player) player).getUniqueId()) && Hosts.getInstance().getTeamManager().getInvitations().get(((Player) player).getUniqueId()).equals(gamePlayer.getPlayer().getUniqueId())) {
                Hosts.getInstance().getTeamManager().getInvitations().remove(((Player) player).getUniqueId());
                player.sendMessage(Utils.PREFIX + Utils.translate("&cThe invitation to " + gamePlayer.getPlayer().getName() + " has expired."));
            }
        }, 2000L);

        Hosts.getInstance().getTeamManager().getInvitations().put(((Player) player).getUniqueId(), gamePlayer.getPlayer().getUniqueId());

        player.sendMessage(Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("ANNOUNCEMENTS.GAMES.SUMO.TEAM.SENDED-INVITATION").replace("{player}", gamePlayer.getPlayer().getName())));

        TextComponent message = new TextComponent(Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("ANNOUNCEMENTS.GAMES.SUMO.TEAM.INVITATION")
                .replace("{player}", player.getName())));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Utils.translate("&7Right-click to accept the invitation")).create()));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/host team accept " + player.getName()));
        gamePlayer.getPlayer().spigot().sendMessage(message);
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            return Collections.emptyList();
        }
        if (args.length == 4) {
            return Utils.getCompletions(args, new ArrayList<>(Bukkit.getOnlinePlayers().stream().map(Player::getDisplayName).collect(Collectors.toList())));
        }
        return null;
    }
}
