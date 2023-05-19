package com.github.bfabri.hosts.commands.team.arguments;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.game.GamePlayer;
import com.github.bfabri.hosts.game.games.modes.sumo.TwoVSTwo;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AcceptArgument extends CommandArgument {
    public AcceptArgument() {
	    super("accept", "Accept the invitation from someone");
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
		    player.sendMessage(Utils.PREFIX + Utils.translate("&cThis player does not exist!"));
		    return true;
	    }

        GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(playerTeam);
        GamePlayer sender = Hosts.getInstance().getGameManager().getPlayer((Player) player);

        if (gamePlayer == null) {
            return true;
        }


        if (!gamePlayer.getPlayer().isOnline()) {
            gamePlayer.getPlayer().sendMessage(Utils.PREFIX + "&cThe player who invited you is no longer online.");
            return true;
        }

        if (!Hosts.getInstance().getTeamManager().getInvitations().containsKey(gamePlayer.getPlayer().getUniqueId())) {
            player.sendMessage(Utils.translate(Utils.PREFIX + "&cYou don't have any invitation."));
            return true;
        }

        if (Hosts.getInstance().getTeamManager().getTeams().stream().anyMatch(team -> team.getTeamPlayers().contains(gamePlayer))) {
            player.sendMessage(Utils.PREFIX + Utils.translate("&cThis player already has a team!"));
            return true;
        }

        if (!Hosts.getInstance().getTeamManager().getInvitations().get(gamePlayer.getPlayer().getUniqueId()).equals(sender.getPlayer().getUniqueId())) {
            player.sendMessage(Utils.PREFIX + Utils.translate("&cThis player has not sent you an invitation."));
            return true;
        }

        Hosts.getInstance().getTeamManager().createTeam(gamePlayer, Hosts.getInstance().getGameManager().getPlayer((Player) player));
        if (Hosts.getInstance().getGameManager().isGameAvailable() && Hosts.getInstance().getGameManager().getGame().getName().equalsIgnoreCase("SUMO") && Hosts.getInstance().getGameManager().getMode().equalsIgnoreCase("2v2")) {
            ((TwoVSTwo) Hosts.getInstance().getGameManager().getGame()).getPlayers().remove(Hosts.getInstance().getGameManager().getPlayer((Player) player));
            ((TwoVSTwo) Hosts.getInstance().getGameManager().getGame()).getPlayers().remove(gamePlayer);
        }
        player.sendMessage(Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("ANNOUNCEMENTS.GAMES.SUMO.TEAM.INVITATION-ACCEPTED").replace("{player}", gamePlayer.getPlayer().getName())));
        gamePlayer.getPlayer().sendMessage(Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("ANNOUNCEMENTS.GAMES.SUMO.TEAM.INVITATION-ACCEPTED").replace("{player}", player.getName())));
        Hosts.getInstance().getTeamManager().getInvitations().remove(gamePlayer.getPlayer().getUniqueId());
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
