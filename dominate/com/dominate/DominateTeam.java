package com.dominate;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.games.Games;
import com.games.player.GamePlayer;
import com.games.utils.Title;

public class DominateTeam {

	private Dominate game;
	private DominateTeamType type;
	private ArrayList<GamePlayer> players = new ArrayList<GamePlayer>();

	private int points = 0;

	public DominateTeam(Dominate game,DominateTeamType type){
		this.game = game;
		this.type = type;
	}

	public Dominate getGame(){
		return game;
	}

	public DominateTeamType getType(){
		return type;
	}

	public int getPoints(){
		return points;
	}

	public void addPoint(){
		points ++;
		if(points > DominateTeams.WIN_SCORE) points = DominateTeams.WIN_SCORE;
	}

	public void resetTeam(){
		points = 0;
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

	public void capturePoint(DominatePoint point){
		for(GamePlayer gPlayer : game.getGamePlayers()){
			Title.showTitle(gPlayer.getPlayer()," ",0.5,2,0.5);
			Title.showSubTitle(gPlayer.getPlayer(),this.getType().getChatColor()+this.getType().toName()+" obsadili "+point.getName(),0.5,2,0.5);
		}
	}

	public void pickupEmerald(){
		points += DominateEmerald.POINTS;
	}

	public boolean isLocationInSpawn(Location location){
		return (game.getArena().getSpawnArea(type).contains(location.getX(),location.getZ()));
	}

	public void addPlayer(GamePlayer gPlayer){
		players.add(gPlayer);
		this.setPlayerInventory(gPlayer);
		Games.getEssentials().getUser(gPlayer.getPlayer()).setNickname(type.getChatColor()+gPlayer.getPlayer().getName()+"§r");
		Games.getEssentials().getUser(gPlayer.getPlayer()).setDisplayNick();
	}

	public void removePlayer(GamePlayer gPlayer){
		players.remove(gPlayer);
		Games.getEssentials().getUser(gPlayer.getPlayer()).setNickname(Games.getEssentials().getUser(gPlayer.getPlayer()).getName());
		Games.getEssentials().getUser(gPlayer.getPlayer()).setDisplayNick();
	}

	public void setPlayerInventory(GamePlayer gPlayer){
		ItemStack itemStack;
		LeatherArmorMeta meta;

		itemStack = new ItemStack(Material.LEATHER_CHESTPLATE,1);
		meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(this.getType().getColor());
        itemStack.setItemMeta(meta);
        gPlayer.getPlayer().getInventory().setChestplate(itemStack);
	}

	public enum DominateTeamType {
		RED, BLUE, NONE;

		public String toString(){
			return this.name().toLowerCase();
		}

		public String toName(){
			switch(this){
				case RED: return "Red";
				case BLUE: return "Blue";
				default:break;
			}
			return null;
		}

		public ChatColor getChatColor(){
			switch(this){
				case RED: return ChatColor.RED;
				case BLUE: return ChatColor.BLUE;
				default:break;
			}
			return ChatColor.WHITE;
		}

		public ChatColor getScoreboardColor(){
			switch(this){
				case RED: return ChatColor.RED;
				case BLUE: return ChatColor.AQUA;
				default:break;
			}
			return ChatColor.WHITE;
		}

		public Color getColor(){
			switch(this){
				case RED: return Color.RED;
				case BLUE: return Color.BLUE;
				case NONE: return Color.WHITE;
			}
			return Color.WHITE;
		}

		public DyeColor getDyeColor(){
			switch(this){
				case RED: return DyeColor.RED;
				case BLUE: return DyeColor.BLUE;
				case NONE: return DyeColor.WHITE;
			}
			return DyeColor.WHITE;
		}

		public int getProgressValue(){
			switch(this){
				case RED: return 1;
				case BLUE: return -1;
				default:break;
			}
			return 0;
		}
	}
}