package com.hidenseek;

import com.games.Games;
import com.games.player.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class HidenSeekTeam {

	private HidenSeek game;
	private HidenSeekTeamType type;
	private ArrayList<GamePlayer> players = new ArrayList<GamePlayer>();

	public HidenSeekTeam(HidenSeek game,HidenSeekTeamType type){
		this.game = game;
		this.type = type;
	}

	public HidenSeek getGame(){
		return game;
	}

	public HidenSeekTeamType getType(){
		return type;
	}

	public void resetTeam(){
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
		Games.getEssentials().getUser(gPlayer.getPlayer()).setNickname(type.getChatColor()+gPlayer.getPlayer().getName()+"§r");
		Games.getEssentials().getUser(gPlayer.getPlayer()).setDisplayNick();
	}

	public void removePlayer(GamePlayer gPlayer){
		players.remove(gPlayer);
		Games.getEssentials().getUser(gPlayer.getPlayer()).setNickname(Games.getEssentials().getUser(gPlayer.getPlayer()).getName());
		Games.getEssentials().getUser(gPlayer.getPlayer()).setDisplayNick();
		gPlayer.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
	}

	public enum HidenSeekTeamType {
		HIDERS, SEEKERS;

		public String toString(){
			return this.name().toLowerCase();
		}

		public static HidenSeekTeamType getByName(String name){
			return HidenSeekTeamType.valueOf(name.toUpperCase());
		}

		public String toName(){
			switch(this){
				case HIDERS: return "Hiders";
				case SEEKERS: return "Seekers";
			}
			return null;
		}

		public ChatColor getChatColor(){
			switch(this){
				case HIDERS: return ChatColor.AQUA;
				case SEEKERS: return ChatColor.RED;
			}
			return ChatColor.WHITE;
		}

		public Color getColor(){
			switch(this){
				case HIDERS: return Color.AQUA;
				case SEEKERS: return Color.RED;
			}
			return Color.WHITE;
		}
	}
}