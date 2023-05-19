package com.github.bfabri.hosts.commands.host.arguments;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.commands.utils.CommandArgument;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class StopArgument extends CommandArgument {

    public StopArgument() {
	    super("stop", "Stop a game.");
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
            Hosts.getInstance().getGameManager().getGame().onStop();
        } else {
            sender.sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("GAME-OFFLINE")));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
