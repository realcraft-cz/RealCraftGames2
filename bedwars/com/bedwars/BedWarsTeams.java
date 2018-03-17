package com.bedwars;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.block.Block;

import com.bedwars.BedWarsTeam.BedWarsTeamType;
import com.games.player.GamePlayer;
import com.games.utils.RandomUtil;

public class BedWarsTeams {

	private BedWars game;

	private LinkedHashMap<BedWarsTeamType,BedWarsTeam> teams = new LinkedHashMap<BedWarsTeamType,BedWarsTeam>();

	public BedWarsTeams(BedWars game){
		this.game = game;
		teams.put(BedWarsTeamType.RED,new BedWarsTeam(game,BedWarsTeamType.RED));
		teams.put(BedWarsTeamType.BLUE,new BedWarsTeam(game,BedWarsTeamType.BLUE));
		teams.put(BedWarsTeamType.YELLOW,new BedWarsTeam(game,BedWarsTeamType.YELLOW));
		teams.put(BedWarsTeamType.GREEN,new BedWarsTeam(game,BedWarsTeamType.GREEN));
	}

	public BedWars getGame(){
		return game;
	}

	public ArrayList<BedWarsTeam> getTeams(){
		return new ArrayList<BedWarsTeam>(teams.values());
	}

	public BedWarsTeam getTeam(BedWarsTeamType type){
		return teams.get(type);
	}

	public BedWarsTeam getPlayerTeam(GamePlayer gPlayer){
		for(BedWarsTeam team : this.getTeams()){
			if(team.isPlayerInTeam(gPlayer)) return team;
		}
		return null;
	}

	public void setPlayerTeam(GamePlayer gPlayer,BedWarsTeam team){
		BedWarsTeam oldTeam = this.getPlayerTeam(gPlayer);
		if(oldTeam != null){
			if(oldTeam != team){
				oldTeam.removePlayer(gPlayer);
				team.addPlayer(gPlayer);
			}
		}
		else team.addPlayer(gPlayer);
	}

	public BedWarsTeam getBedTeam(Block block){
		for(BedWarsTeam team : this.getTeams()){
			if(team.getBedBlock().equals(block)) return team;
		}
		return null;
	}

	public BedWarsTeam getChestTeam(Block block){
		for(BedWarsTeam team : this.getTeams()){
			for(Block chest : team.getChests()){
				if(chest.equals(block)) return team;
			}
		}
		return null;
	}

	public void resetTeams(){
		for(GamePlayer gPlayer : game.getPlayers()){
			if(this.getGame().getTeams().getPlayerTeam(gPlayer) != null){
				this.getGame().getTeams().getPlayerTeam(gPlayer).removePlayer(gPlayer);
			}
		}
		for(BedWarsTeam team : this.getTeams()){
			team.resetTeam();
		}
	}

	public void autoBalancingTeams(){
		ArrayList<BedWarsTeam> aTeams = this.getActiveTeams();
		if(this.getActiveTeams().size() == 1){
			ArrayList<GamePlayer> players = new ArrayList<GamePlayer>(game.getPlayers());
			Collections.shuffle(players);
			for(GamePlayer gPlayer : players){
				if(this.getPlayerTeam(gPlayer) == null){
					this.setPlayerTeam(gPlayer,this.getLowestBalancingTeam(true));
					this.autoBalancingTeams();
					break;
				}
			}
		}
		else if(this.getActiveTeams().size() == 2){
			ArrayList<GamePlayer> players = new ArrayList<GamePlayer>(game.getPlayers());
			Collections.shuffle(players);
			for(GamePlayer gPlayer : players){
				if(this.getPlayerTeam(gPlayer) == null){
					this.setPlayerTeam(gPlayer,this.getLowestBalancingTeam(false));
				}
			}
			int difference = aTeams.get(0).getPlayers().size()-aTeams.get(1).getPlayers().size();
			if(difference > 1){
				Object [] values = aTeams.get(0).getPlayers().toArray();
				for(int i=0;i<difference/2;i++){
					GamePlayer gPlayer = (GamePlayer)values[RandomUtil.getRandomInteger(0,values.length-1)];
					this.setPlayerTeam(gPlayer,aTeams.get(1));
				}
			}
			else if(difference < -1){
				Object [] values = aTeams.get(1).getPlayers().toArray();
				for(int i=0;i<Math.abs(difference)/2;i++){
					GamePlayer gPlayer = (GamePlayer)values[RandomUtil.getRandomInteger(0,values.length-1)];
					this.setPlayerTeam(gPlayer,aTeams.get(0));
				}
			}
		} else {
			ArrayList<GamePlayer> players = new ArrayList<GamePlayer>(game.getPlayers());
			Collections.shuffle(players);
			for(GamePlayer gPlayer : players){
				if(this.getPlayerTeam(gPlayer) == null){
					this.setPlayerTeam(gPlayer,this.getLowestBalancingTeam(true));
				}
			}
		}
	}

	public BedWarsTeam getLowestBalancingTeam(boolean empty){
		BedWarsTeam lowest = this.getTeams().get(0);
		for(BedWarsTeam team : this.getTeams()){
			if(lowest != null && (empty || team.getPlayers().size() > 0) && team.getPlayers().size() < lowest.getPlayers().size()){
				lowest = team;
			}
		}
		return lowest;
	}

	public ArrayList<BedWarsTeam> getActiveTeams(){
		ArrayList<BedWarsTeam> teamsTmp = new ArrayList<BedWarsTeam>();
		for(BedWarsTeam team : this.getTeams()){
			if(team.getPlayers().size() > 0) teamsTmp.add(team);
		}
		return teamsTmp;
	}

	public BedWarsTeam getWinnerTeam(){
		List<BedWarsTeam> teamsTmp = this.getActiveTeams();
		if(teamsTmp.size() == 1){
			return teamsTmp.get(0);
		}
		else if(teamsTmp.size() > 1){
			int teamsWithBed = 0;
			for(BedWarsTeam team : teamsTmp){
				if(team.hasBed()) teamsWithBed ++;
			}
			if(teamsWithBed == 1){
				for(BedWarsTeam team : teamsTmp){
					if(team.hasBed()) return team;
				}
			}
		}
		return null;
	}
}