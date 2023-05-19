package com.github.bfabri.hosts.game;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class Team {

	@Getter
	protected String name;

	@Setter
	@Getter
	protected GamePlayer playerA;

	@Setter
	@Getter
	protected GamePlayer playerB;

	@Getter
	protected ArrayList<GamePlayer> teamPlayers = new ArrayList<>();

	@Setter
	@Getter
	protected ArrayList<GamePlayer> players;

	@Getter
	@Setter
	protected Team opponent;

	@Setter
	@Getter
	protected boolean eliminatedA;

	@Setter
	@Getter
	protected boolean eliminatedB;

	public Team(String name, GamePlayer playerA, GamePlayer playerB) {
		this.name = name;
		this.playerA = playerA;
		this.playerB = playerB;

		this.eliminatedA = false;
		teamPlayers.add(playerA);

		if (playerB == null) {
			this.eliminatedB = true;
			return;
		}

		teamPlayers.add(playerB);
	}

	public Team(String name, ArrayList<GamePlayer> players) {
		this.name = name;
		this.players = players;

		this.eliminatedA = false;
		this.eliminatedB = false;

		teamPlayers.addAll(players);
	}

	public boolean isEliminated() {
		return players != null && players.size() < 1;
	}
}
