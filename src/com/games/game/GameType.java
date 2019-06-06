package com.games.game;

import com.games.arena.GameArenaDimension;
import com.games.arena.GameArenaDimension.GameArenaDimensionDefault;
import org.bukkit.ChatColor;

public enum GameType {
	BEDWARS, HIDENSEEK, BLOCKPARTY, RAGEMODE, PAINTBALL, DOMINATE, RACES;

	public static GameType getByName(String name){
		return GameType.valueOf(name.toUpperCase());
	}

	public static GameType getById(int id){
		for(GameType type : GameType.values()){
			if(type.getId() == id) return type;
		}
		return null;
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
			case RACES: return 12;
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
			case RACES: return "Races";
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
			case RACES: return ChatColor.DARK_AQUA;
		}
		return ChatColor.WHITE;
	}

	public GameArenaDimension getDimension(){
		switch(this){
			case BEDWARS: return new GameArenaDimensionDefault();
			case HIDENSEEK: return new GameArenaDimensionDefault();
			case BLOCKPARTY: return new GameArenaDimensionDefault();//31,7,31
			case RAGEMODE: return new GameArenaDimensionDefault();
			case PAINTBALL: return new GameArenaDimensionDefault();
			case DOMINATE: return new GameArenaDimensionDefault();
			case RACES: return new GameArenaDimensionDefault();
		}
		return new GameArenaDimensionDefault();
	}
}