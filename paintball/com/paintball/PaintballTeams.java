package com.paintball;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.games.player.GamePlayer;

public class PaintballTeams {

	private Paintball game;

	private PaintballTeam redTeam;
	private PaintballTeam blueTeam;

	public PaintballTeams(Paintball game){
		this.game = game;
		this.redTeam = new PaintballTeam(game,PaintballTeamType.RED);
		this.blueTeam = new PaintballTeam(game,PaintballTeamType.BLUE);
	}

	public Paintball getGame(){
		return game;
	}

	public PaintballTeam getTeam(PaintballTeamType type){
		if(type == PaintballTeamType.RED) return redTeam;
		else if(type == PaintballTeamType.BLUE) return blueTeam;
		return null;
	}

	public PaintballTeam getPlayerTeam(GamePlayer gPlayer){
		if(redTeam.isPlayerInTeam(gPlayer)) return redTeam;
		else if(blueTeam.isPlayerInTeam(gPlayer)) return blueTeam;
		return null;
	}

	public void setPlayerTeam(GamePlayer gPlayer,PaintballTeam team){
		PaintballTeam oldTeam = this.getPlayerTeam(gPlayer);
		if(oldTeam != null){
			if(oldTeam != team){
				oldTeam.removePlayer(gPlayer);
				team.addPlayer(gPlayer);
			}
		}
		else team.addPlayer(gPlayer);
	}

	public void resetTeams(){
		redTeam.resetTeam();
		blueTeam.resetTeam();
	}

	public void autoBalancingTeams(){
		Random generator = new Random();
		boolean teamChoose = generator.nextBoolean();
		ArrayList<GamePlayer> players = new ArrayList<GamePlayer>(game.getPlayers());
		Collections.shuffle(players);
		for(GamePlayer gPlayer : players){
			if(this.getPlayerTeam(gPlayer) == null){
				if(teamChoose) this.setPlayerTeam(gPlayer,this.getTeam(PaintballTeamType.BLUE));
				else this.setPlayerTeam(gPlayer,this.getTeam(PaintballTeamType.RED));
				teamChoose = !teamChoose;
			}
		}
		int difference = this.getTeam(PaintballTeamType.RED).getPlayers().size()-this.getTeam(PaintballTeamType.BLUE).getPlayers().size();
		if(difference > 1){
			Object [] values = this.getTeam(PaintballTeamType.RED).getPlayers().toArray();
			for(int i=0;i<difference/2;i++){
				GamePlayer gPlayer = (GamePlayer)values[generator.nextInt(values.length)];
				this.setPlayerTeam(gPlayer,this.getTeam(PaintballTeamType.BLUE));
			}
		}
		else if(difference < -1){
			Object [] values = this.getTeam(PaintballTeamType.BLUE).getPlayers().toArray();
			for(int i=0;i<Math.abs(difference)/2;i++){
				GamePlayer gPlayer = (GamePlayer)values[generator.nextInt(values.length)];
				this.setPlayerTeam(gPlayer,this.getTeam(PaintballTeamType.RED));
			}
		}
	}

	public boolean isTeamFull(PaintballTeamType type){
		int difference = this.getTeam(PaintballTeamType.RED).getPlayers().size()-this.getTeam(PaintballTeamType.BLUE).getPlayers().size();
		if(type == PaintballTeamType.RED && difference > 0) return true;
		else if(type == PaintballTeamType.BLUE && difference < 0) return true;
		return false;
	}

	public ArrayList<PaintballTeam> getActiveTeams(){
		ArrayList<PaintballTeam> teamsTmp = new ArrayList<PaintballTeam>();
		if(redTeam.getPlayers().size() > 0) teamsTmp.add(redTeam);
		if(blueTeam.getPlayers().size() > 0) teamsTmp.add(blueTeam);
		return teamsTmp;
	}

	public PaintballTeam getWinnerTeam(){
		List<PaintballTeam> teamsTmp = this.getActiveTeams();
		if(teamsTmp.size() == 1){
			return teamsTmp.get(0);
		}
		else if(this.getTeam(PaintballTeamType.RED).getKills() > this.getTeam(PaintballTeamType.BLUE).getKills()) return this.getTeam(PaintballTeamType.RED);
		return this.getTeam(PaintballTeamType.BLUE);
	}
}