package com.github.bfabri.hosts.game;

import com.github.bfabri.hosts.utils.Utils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamManager {

	@Getter
	private final ArrayList<Team> teams = new ArrayList<>();

	@Getter
	private final Map<UUID, UUID> invitations = new HashMap<>();


	public void createTeam(GamePlayer playerA, GamePlayer playerB) {
		Team team = new Team("normalTeam", playerA, playerB);
		teams.add(team);
	}


	public void sendMessage(String message) {
		teams.forEach(teams -> teams.getTeamPlayers().forEach(players -> players.getPlayer().sendMessage(Utils.translate(message))));
	}

	public Team getTeamByName(String name) {
		for (Team team : teams) {
			if (team.getName().equalsIgnoreCase(name)) {
				return team;
			}
		}
		return null;
	}

	public Team getTeamByPlayer(GamePlayer gamePlayer) {
		for (Team team : teams) {
			if (team.getTeamPlayers().contains(gamePlayer)) {
				return team;
			}
		}
		return null;
	}
}
