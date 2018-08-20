package com.races.arenas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import com.games.arena.GameArena;
import com.races.RaceCheckpoint;
import com.races.RaceCheckpoint.RaceCheckpointType;
import com.races.RaceType;
import com.races.Races;

import realcraft.bukkit.utils.LocationUtil;

public abstract class RaceArena extends GameArena {

	private RaceType type;
	private int rounds;
	private ArrayList<Location> spawns = new ArrayList<Location>();
	private ArrayList<RaceCheckpoint> checkpoints = new ArrayList<RaceCheckpoint>();
	private RaceBarrier barrier;

	public RaceArena(Races game,String name,RaceType type){
		super(game,name);
		this.type = type;
		this.loadSpawns();
		this.loadCheckpoints();
		this.loadBarrier();
	}

	public Races getGame(){
		return (Races) super.getGame();
	}

	public RaceType getRaceType(){
		return type;
	}

	public int getRounds(){
		if(rounds == 0) rounds = this.getConfig().getInt("rounds");
		return rounds;
	}

	public ArrayList<Location> getSpawns(){
		return spawns;
	}

	public ArrayList<RaceCheckpoint> getCheckpoints(){
		return checkpoints;
	}

	public RaceBarrier getBarrier(){
		return barrier;
	}

	private void loadSpawns(){
		List<Map<String, Object>> tempSpawns = (List<Map<String, Object>>) this.getConfig().get("spawns");
		if(tempSpawns != null && !tempSpawns.isEmpty()){
			for(Map<String, Object> spawn : tempSpawns){
				double x = Double.valueOf(spawn.get("x").toString());
				double y = Double.valueOf(spawn.get("y").toString());
				double z = Double.valueOf(spawn.get("z").toString());
				float yaw = Float.valueOf(spawn.get("yaw").toString());
				float pitch = Float.valueOf(spawn.get("pitch").toString());
				World world = Bukkit.getWorld(spawn.get("world").toString());
				if(world == null) continue;
				spawns.add(new Location(world,x,y,z,yaw,pitch));
			}
		}
	}

	private void loadCheckpoints(){
		if(this.getConfig().getConfigurationSection("checkpoints") != null){
			int index = 0;
			for(String key : this.getConfig().getConfigurationSection("checkpoints").getKeys(false)){
				ConfigurationSection section = this.getConfig().getConfigurationSection("checkpoints."+key);
				RaceCheckpointType type = RaceCheckpointType.fromName(section.getString("type"));
				Location location1 = LocationUtil.getConfigLocation(section,"locFrom");
				Location location2 = LocationUtil.getConfigLocation(section,"locTo");
				checkpoints.add(new RaceCheckpoint(this,index++,type,location1,location2));
			}
		}
	}

	private void loadBarrier(){
		Location location1 = LocationUtil.getConfigLocation(this.getConfig(),"barrier.locFrom");
		Location location2 = LocationUtil.getConfigLocation(this.getConfig(),"barrier.locTo");
		barrier = new RaceBarrier(location1,location2);
	}

	public class RaceBarrier {

		private Location minLoc;
		private Location maxLoc;

		public RaceBarrier(Location locFrom,Location locTo){
			this.minLoc = Vector.getMinimum(locFrom.toVector(),locTo.toVector()).toLocation(locFrom.getWorld());
			this.maxLoc = Vector.getMaximum(locFrom.toVector(),locTo.toVector()).toLocation(locFrom.getWorld());
		}

		public ArrayList<Block> getBlocks(){
			ArrayList<Block> blocks = new ArrayList<Block>();
			for(int x=minLoc.getBlockX();x<=maxLoc.getBlockX();x++){
				for(int y=minLoc.getBlockY();y<=maxLoc.getBlockY();y++){
					for(int z=minLoc.getBlockZ();z<=maxLoc.getBlockZ();z++){
						if(minLoc.getWorld().getBlockAt(x,y,z).getType() == Material.BARRIER){
							blocks.add(minLoc.getWorld().getBlockAt(x,y,z));
						}
					}
				}
			}
			return blocks;
		}
	}
}