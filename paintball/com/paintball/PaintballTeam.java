package com.paintball;

import java.util.ArrayList;

import org.bukkit.Location;

import com.games.player.GamePlayer;
import com.realcraft.nicks.NickManager;

public class PaintballTeam {

	private Paintball game;
	private PaintballTeamType type;
	private ArrayList<GamePlayer> players = new ArrayList<GamePlayer>();

	private int kills = 0;

	public PaintballTeam(Paintball game,PaintballTeamType type){
		this.game = game;
		this.type = type;
	}

	public Paintball getGame(){
		return game;
	}

	public PaintballTeamType getType(){
		return type;
	}

	public int getKills(){
		return kills;
	}

	public void addKill(){
		kills ++;
	}

	public void resetTeam(){
		kills = 0;
		players.clear();
	}

	public ArrayList<GamePlayer> getPlayers(){
		return players;
	}

	public boolean isPlayerInTeam(GamePlayer gPlayer){
		return players.contains(gPlayer);
	}

	public Location getSpawnLocation(){
		return game.getArena().getTeamSpawn(type);
	}

	public void addPlayer(GamePlayer gPlayer){
		players.add(gPlayer);
		this.setPlayerTeamInventory(gPlayer);
	}

	public void removePlayer(GamePlayer gPlayer){
		players.remove(gPlayer);
	}

	public void setPlayerNickColor(GamePlayer gPlayer){
		NickManager.setPlayerPrefix(gPlayer.getPlayer(),type.getChatColor().toString());

		//Game.essentials.getUser(player).setNickname(type.getChatColor()+Game.essentials.getUser(player).getName()+"§r");
		//Game.essentials.getUser(player).setDisplayNick();
	}

	public void setPlayerTeamInventory(GamePlayer gPlayer){
		this.getType().setPlayerInventory(gPlayer);
	}
}