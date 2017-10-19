package com.games.game;

import org.bukkit.ChatColor;

public enum GameType {
	BEDWARS, HIDENSEEK, BLOCKPARTY, RAGEMODE, PAINTBALL, DOMINATE;

	public static GameType getByName(String name){
		return GameType.valueOf(name.toUpperCase());
	}

	public String toString(){
		return this.name().toLowerCase();
	}

	public int getId(){
		switch(this){
			case BEDWARS: return 3;
			case HIDENSEEK: return 4;
			case BLOCKPARTY: return 5;
			case RAGEMODE: return 6;
			case PAINTBALL: return 7;
			case DOMINATE: return 10;
		}
		return 0;
	}

	public String getName(){
		switch(this){
			case BEDWARS: return "BedWars";
			case HIDENSEEK: return "Hide & Seek";
			case BLOCKPARTY: return "BlockParty";
			case RAGEMODE: return "RageMode";
			case PAINTBALL: return "Paintball";
			case DOMINATE: return "Dominate";
		}
		return "unknown";
	}

	public ChatColor getColor(){
		switch(this){
			case BEDWARS: return ChatColor.RED;
			case HIDENSEEK: return ChatColor.BLUE;
			case BLOCKPARTY: return ChatColor.LIGHT_PURPLE;
			case RAGEMODE: return ChatColor.RED;
			case PAINTBALL: return ChatColor.GOLD;
			case DOMINATE: return ChatColor.YELLOW;
		}
		return ChatColor.WHITE;
	}
}