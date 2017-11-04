package com.dominate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;

import com.dominate.DominateTeam.DominateTeamType;
import com.games.player.GamePlayer;

public class DominateTeams {

	private Dominate game;
	private HashMap<DominateTeamType,DominateTeam> teams = new HashMap<DominateTeamType,DominateTeam>();
	public static final int WIN_SCORE = 200;

	public DominateTeams(Dominate game){
		this.game = game;
		teams.put(DominateTeamType.RED,new DominateTeam(game,DominateTeamType.RED));
		teams.put(DominateTeamType.BLUE,new DominateTeam(game,DominateTeamType.BLUE));
	}

	public Dominate getGame(){
		return game;
	}

	public DominateTeam getTeam(DominateTeamType type){
		return teams.get(type);
	}

	public ArrayList<DominateTeam> getTeams(){
		return new ArrayList<DominateTeam>(teams.values());
	}

	public void setPlayerTeam(GamePlayer gPlayer,DominateTeam team){
		DominateTeam oldTeam = this.getPlayerTeam(gPlayer);
		if(oldTeam != null){
			if(oldTeam != team){
				oldTeam.removePlayer(gPlayer);
				team.addPlayer(gPlayer);
			}
		}
		else team.addPlayer(gPlayer);
	}

	public DominateTeam getPlayerTeam(GamePlayer gPlayer){
		for(DominateTeam team : teams.values()){
			if(team.isPlayerInTeam(gPlayer)) return team;
		}
		return null;
	}

	public void resetTeams(){
		for(GamePlayer gPlayer : game.getPlayers()){
			if(this.getGame().getTeams().getPlayerTeam(gPlayer) != null){
				this.getGame().getTeams().getPlayerTeam(gPlayer).removePlayer(gPlayer);
			}
		}
		for(DominateTeam team : teams.values()){
			team.resetTeam();
		}
	}

	public boolean isLocationInSpawn(Location location){
		for(DominateTeam team : teams.values()){
			if(team.isLocationInSpawn(location)) return true;
		}
		return false;
	}

	public void autoBalancingTeams(){
		Random generator = new Random();
		boolean teamChoose = generator.nextBoolean();
		ArrayList<GamePlayer> players = new ArrayList<GamePlayer>(game.getPlayers());
		Collections.shuffle(players);
		for(GamePlayer gPlayer : players){
			if(this.getPlayerTeam(gPlayer) == null){
				if(teamChoose) this.setPlayerTeam(gPlayer,this.getTeam(DominateTeamType.BLUE));
				else this.setPlayerTeam(gPlayer,this.getTeam(DominateTeamType.RED));
				teamChoose = !teamChoose;
			}
		}
		int difference = this.getTeam(DominateTeamType.RED).getPlayers().size()-this.getTeam(DominateTeamType.BLUE).getPlayers().size();
		if(difference > 1){
			Object [] values = this.getTeam(DominateTeamType.RED).getPlayers().toArray();
			for(int i=0;i<difference/2;i++){
				GamePlayer gPlayer = (GamePlayer)values[generator.nextInt(values.length)];
				this.setPlayerTeam(gPlayer,this.getTeam(DominateTeamType.BLUE));
			}
		}
		else if(difference < -1){
			Object [] values = this.getTeam(DominateTeamType.BLUE).getPlayers().toArray();
			for(int i=0;i<Math.abs(difference)/2;i++){
				GamePlayer gPlayer = (GamePlayer)values[generator.nextInt(values.length)];
				this.setPlayerTeam(gPlayer,this.getTeam(DominateTeamType.RED));
			}
		}
	}

	public boolean isTeamFull(DominateTeamType type){
		int difference = this.getTeam(DominateTeamType.RED).getPlayers().size()-this.getTeam(DominateTeamType.BLUE).getPlayers().size();
		if(type == DominateTeamType.RED && difference > 0) return true;
		else if(type == DominateTeamType.BLUE && difference < 0) return true;
		return false;
	}

	public int getPlayerAmount(){
		int amount = 0;
		for(DominateTeam team : teams.values()){
			amount = team.getPlayers().size();
		}
		return amount;
	}

	public List<DominateTeam> getActiveTeams(){
		List<DominateTeam> teamsTmp = new ArrayList<DominateTeam>();
		for(DominateTeam team : this.teams.values()){
			if(team.getPlayers().size() > 0) teamsTmp.add(team);
		}
		return teamsTmp;
	}

	public DominateTeam getWinnerTeam(){
		List<DominateTeam> teamsTmp = this.getActiveTeams();
		if(teamsTmp.size() == 1){
			return teamsTmp.get(0);
		}
		else if(teams.get(DominateTeamType.RED).getPoints() > teams.get(DominateTeamType.BLUE).getPoints()) return teams.get(DominateTeamType.RED);
		else if(teams.get(DominateTeamType.RED).getPoints() < teams.get(DominateTeamType.BLUE).getPoints()) return teams.get(DominateTeamType.BLUE);
		return null;
	}
}