package com.paintball;

import com.games.Games;
import com.games.player.GamePlayer;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;

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

		itemStack = new ItemStack(Material.LEATHER_HELMET,1);
		meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(this.getType().getColor());
        itemStack.setItemMeta(meta);
        gPlayer.getPlayer().getInventory().setHelmet(itemStack);

		itemStack = new ItemStack(Material.LEATHER_CHESTPLATE,1);
		meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(this.getType().getColor());
        itemStack.setItemMeta(meta);
        gPlayer.getPlayer().getInventory().setChestplate(itemStack);

        itemStack = new ItemStack(Material.LEATHER_LEGGINGS,1);
        meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(this.getType().getColor());
        itemStack.setItemMeta(meta);
        gPlayer.getPlayer().getInventory().setLeggings(itemStack);

        itemStack = new ItemStack(Material.LEATHER_BOOTS,1);
        meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(this.getType().getColor());
        itemStack.setItemMeta(meta);
        gPlayer.getPlayer().getInventory().setBoots(itemStack);
	}

	public enum PaintballTeamType {
		RED, BLUE;

		public String toString(){
			return this.name().toLowerCase();
		}

		public static PaintballTeamType getByName(String name){
			return PaintballTeamType.valueOf(name.toUpperCase());
		}

		public String toName(){
			switch(this){
				case RED: return "Red";
				case BLUE: return "Blue";
			}
			return null;
		}

		public ChatColor getChatColor(){
			switch(this){
				case RED: return ChatColor.RED;
				case BLUE: return ChatColor.BLUE;
			}
			return ChatColor.WHITE;
		}

		public Color getColor(){
			switch(this){
				case RED: return Color.RED;
				case BLUE: return Color.BLUE;
			}
			return Color.WHITE;
		}

		public DyeColor getDyeColor(){
			switch(this){
				case RED: return DyeColor.RED;
				case BLUE: return DyeColor.BLUE;
			}
			return DyeColor.WHITE;
		}
	}
}