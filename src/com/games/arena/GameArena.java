package com.games.arena;

import java.io.File;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.games.Games;
import com.games.game.Game;
import com.games.utils.LocationUtil;

public abstract class GameArena {

	private Game game;
	private String name;

	private GameArenaImage image;
	private GameArenaRegion region;

	private FileConfiguration config;

	private int spectatorRadius;
	private Location spectatorLocation;

	public GameArena(Game game,String name){
		this.game = game;
		this.name = name;
		this.image = new GameArenaImage(this);
		this.region = new GameArenaRegion(this);
		this.game.addArena(this);
	}

	public Game getGame(){
		return game;
	}

	public String getName(){
		return name;
	}

	public GameArenaImage getImage(){
		return image;
	}

	public GameArenaRegion getRegion(){
		return region;
	}

	public FileConfiguration getConfig(){
		if(config == null){
			File file = new File(Games.getInstance().getDataFolder()+"/"+game.getType().getName()+"/"+this.getName()+"/"+"config.yml");
			if(file.exists()){
				config = new YamlConfiguration();
				try {
					config.load(file);
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		return config;
	}

	public int getSpectatorRadius(){
		if(spectatorRadius == 0) spectatorRadius = this.getConfig().getInt("spectator.radius");
		return spectatorRadius;
	}

	public Location getSpectatorLocation(){
		if(spectatorLocation == null) spectatorLocation = LocationUtil.getConfigLocation(this.getConfig(),"spectator");
		return spectatorLocation;
	}
}