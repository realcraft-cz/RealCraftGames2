package com.hidenseek;

import java.util.ArrayList;
import java.util.Random;

import com.games.player.GamePlayer;
import com.hidenseek.HidenSeekTeam.HidenSeekTeamType;

public class HidenSeekTeams {

	private HidenSeek game;

	private HidenSeekTeam hidersTeam;
	private HidenSeekTeam seekersTeam;

	public HidenSeekTeams(HidenSeek game){
		this.game = game;
		this.hidersTeam = new HidenSeekTeam(game,HidenSeekTeamType.HIDERS);
		this.seekersTeam = new HidenSeekTeam(game,HidenSeekTeamType.SEEKERS);
	}

	public HidenSeek getGame(){
		return game;
	}

	public HidenSeekTeam getTeam(HidenSeekTeamType type){
		if(type == HidenSeekTeamType.HIDERS) return hidersTeam;
		else if(type == HidenSeekTeamType.SEEKERS) return seekersTeam;
		return null;
	}

	public HidenSeekTeam getPlayerTeam(GamePlayer gPlayer){
		if(hidersTeam.isPlayerInTeam(gPlayer)) return hidersTeam;
		else if(seekersTeam.isPlayerInTeam(gPlayer)) return seekersTeam;
		return null;
	}

	public void setPlayerTeam(GamePlayer gPlayer,HidenSeekTeam team){
		HidenSeekTeam oldTeam = this.getPlayerTeam(gPlayer);
		if(oldTeam != null){
			if(oldTeam != team){
				oldTeam.removePlayer(gPlayer);
				team.addPlayer(gPlayer);
			}
		}
		else team.addPlayer(gPlayer);
	}

	public void resetTeams(){
		for(GamePlayer gPlayer : game.getPlayers()){
			if(this.getGame().getTeams().getPlayerTeam(gPlayer) != null){
				this.getGame().getTeams().getPlayerTeam(gPlayer).removePlayer(gPlayer);
			}
		}
		hidersTeam.resetTeam();
		seekersTeam.resetTeam();
	}

	public void autoBalancingTeams(){
		Random generator = new Random();
		ArrayList<String> randomSeekers = new ArrayList<String>();
		int seekers = 3;
		if(game.getGamePlayers().size() <= 6) seekers = 1;
		else if(game.getGamePlayers().size() <= 11) seekers = 2;
		Object [] values = game.getGamePlayers().toArray();
		for(int i=0;i<seekers;i++){
			GamePlayer gPlayer = (GamePlayer) values[generator.nextInt(values.length)];
			if(!randomSeekers.contains(gPlayer.getPlayer().getName())) randomSeekers.add(gPlayer.getPlayer().getName());
			else i--;
		}
		for(GamePlayer gPlayer : game.getGamePlayers()){
			if(randomSeekers.contains(gPlayer.getPlayer().getName())){
				this.setPlayerTeam(gPlayer,seekersTeam);
			} else {
				this.setPlayerTeam(gPlayer,hidersTeam);
			}
		}
	}

	public void autoBalancingAfterLeft(){
		Random generator = new Random();
		int seekers = 3;
		if(game.getGamePlayers().size() <= 6) seekers = 1;
		else if(game.getGamePlayers().size() <= 11) seekers = 2;
		if(game.getGamePlayers().size() > 0 && seekersTeam.getPlayers().size() < seekers){
			ArrayList<String> randomSeekers = new ArrayList<String>();
			Object [] values = hidersTeam.getPlayers().toArray();
			for(int i=0;i<seekers;i++){
				GamePlayer gPlayer = (GamePlayer) values[generator.nextInt(values.length)];
				if(!randomSeekers.contains(gPlayer.getPlayer().getName())) randomSeekers.add(gPlayer.getPlayer().getName());
				else i--;
			}
			for(GamePlayer gPlayer : game.getGamePlayers()){
				if(randomSeekers.contains(gPlayer.getPlayer().getName())){
					game.getUser(gPlayer).cancelDisguise();
					this.setPlayerTeam(gPlayer,seekersTeam);
					gPlayer.getPlayer().teleport(seekersTeam.getSpawnLocation());
					gPlayer.resetPlayer();
					game.loadGameInventory(gPlayer);
					game.getScoreboard().updateForPlayer(gPlayer);
					game.getUser(gPlayer).setSpawnTime(System.currentTimeMillis()+(20*1000));
				}
			}
		}
	}

	public ArrayList<HidenSeekTeam> getActiveTeams(){
		ArrayList<HidenSeekTeam> teamsTmp = new ArrayList<HidenSeekTeam>();
		if(hidersTeam.getPlayers().size() > 0) teamsTmp.add(hidersTeam);
		if(seekersTeam.getPlayers().size() > 0) teamsTmp.add(seekersTeam);
		return teamsTmp;
	}

	public HidenSeekTeam getWinnerTeam(){
		if(hidersTeam.getPlayers().size() == 0) return seekersTeam;
		return hidersTeam;
	}

	public HidenSeekTeam getLoserTeam(){
		if(hidersTeam.getPlayers().size() != 0) return seekersTeam;
		return hidersTeam;
	}
}