package com.paintball;

import org.bukkit.Location;

import com.games.arena.GameArena;
import com.games.utils.LocationUtil;

public class PaintballArena extends GameArena {

	private Location redSpawn;
	private Location blueSpawn;

	public PaintballArena(Paintball game,String name){
		super(game,name);
		this.loadSpawns();
	}

	public Paintball getGame(){
		return (Paintball) super.getGame();
	}

	public Location getTeamSpawn(PaintballTeamType type){
		if(type == PaintballTeamType.RED) return redSpawn;
		else if(type == PaintballTeamType.BLUE) return blueSpawn;
		return null;
	}

	private void loadSpawns(){
		this.redSpawn = LocationUtil.getConfigLocation(this.getConfig(),"custom.spawns."+PaintballTeamType.RED.toName().toLowerCase());
		this.blueSpawn = LocationUtil.getConfigLocation(this.getConfig(),"custom.spawns."+PaintballTeamType.BLUE.toName().toLowerCase());
	}
}