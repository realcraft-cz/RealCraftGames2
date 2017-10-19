package com.ragemode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import com.games.arena.GameArena;

public class RageModeArena extends GameArena {

	private ArrayList<Location> spawns = new ArrayList<Location>();

	public RageModeArena(RageMode game,String name){
		super(game,name);
		this.loadSpawns();
	}

	public RageMode getGame(){
		return (RageMode) super.getGame();
	}

	public ArrayList<Location> getSpawns(){
		return spawns;
	}

	public Location getRandomSpawn(){
		return spawns.get(new Random().nextInt(spawns.size()));
	}

	@SuppressWarnings("unchecked")
	private void loadSpawns(){
		List<Map<String, Object>> tempSpawns = (List<Map<String, Object>>) this.getConfig().get("custom.spawns");
		if(tempSpawns != null && !tempSpawns.isEmpty()){
			for(Map<String, Object> spawn : tempSpawns){
				double x = Double.valueOf(spawn.get("x").toString());
				double y = Double.valueOf(spawn.get("y").toString());
				double z = Double.valueOf(spawn.get("z").toString());
				float yaw = Float.valueOf(spawn.get("yaw").toString());
				float pitch = Float.valueOf(spawn.get("pitch").toString());
				World world = Bukkit.getWorld(spawn.get("world").toString());
				if(world == null){
					world = Bukkit.createWorld(new WorldCreator(spawn.get("world").toString()));
					if(world == null){
						continue;
					}
				}
				spawns.add(new Location(world,x,y,z,yaw,pitch));
			}
		}
	}
}