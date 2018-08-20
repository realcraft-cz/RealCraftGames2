package com.bedwars;

import com.bedwars.BedWarsResource.BedWarsResourceType;
import com.bedwars.BedWarsTeam.BedWarsTeamType;
import com.games.arena.GameArena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import realcraft.bukkit.utils.LocationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BedWarsArena extends GameArena {

	private Location minLocation;
	private Location maxLocation;
	private Vector minVector;
	private Vector maxVector;

	private HashMap<BedWarsTeamType,Location> spawns = new HashMap<BedWarsTeamType,Location>();
	private HashMap<BedWarsTeamType,Location> beds = new HashMap<BedWarsTeamType,Location>();
	private ArrayList<Block> playerBlocks = new ArrayList<Block>();

	public BedWarsArena(BedWars game,String name){
		super(game,name);
		this.loadSpawns();
		this.loadBeds();
		this.loadResources(BedWarsResourceType.BRONZE);
		this.loadResources(BedWarsResourceType.IRON);
		this.loadResources(BedWarsResourceType.GOLD);
		this.loadTraders();
		minLocation = LocationUtil.getConfigLocation(this.getConfig(),"custom.locMin");
		maxLocation = LocationUtil.getConfigLocation(this.getConfig(),"custom.locMax");
		minVector = minLocation.toVector();
		maxVector = maxLocation.toVector();
	}

	public BedWars getGame(){
		return (BedWars) super.getGame();
	}

	public Location getTeamSpawn(BedWarsTeamType type){
		return spawns.get(type);
	}

	public Location getTeamBed(BedWarsTeamType type){
		return beds.get(type);
	}

	private void loadSpawns(){
		spawns.put(BedWarsTeamType.RED,LocationUtil.getConfigLocation(this.getConfig(),"custom.teams."+BedWarsTeamType.RED.toString()+".spawn"));
		spawns.put(BedWarsTeamType.GREEN,LocationUtil.getConfigLocation(this.getConfig(),"custom.teams."+BedWarsTeamType.GREEN.toString()+".spawn"));
		spawns.put(BedWarsTeamType.BLUE,LocationUtil.getConfigLocation(this.getConfig(),"custom.teams."+BedWarsTeamType.BLUE.toString()+".spawn"));
		spawns.put(BedWarsTeamType.YELLOW,LocationUtil.getConfigLocation(this.getConfig(),"custom.teams."+BedWarsTeamType.YELLOW.toString()+".spawn"));
	}

	private void loadBeds(){
		beds.put(BedWarsTeamType.RED,LocationUtil.getConfigLocation(this.getConfig(),"custom.teams."+BedWarsTeamType.RED.toString()+".bed"));
		beds.put(BedWarsTeamType.GREEN,LocationUtil.getConfigLocation(this.getConfig(),"custom.teams."+BedWarsTeamType.GREEN.toString()+".bed"));
		beds.put(BedWarsTeamType.BLUE,LocationUtil.getConfigLocation(this.getConfig(),"custom.teams."+BedWarsTeamType.BLUE.toString()+".bed"));
		beds.put(BedWarsTeamType.YELLOW,LocationUtil.getConfigLocation(this.getConfig(),"custom.teams."+BedWarsTeamType.YELLOW.toString()+".bed"));
	}

	@SuppressWarnings("unchecked")
	private void loadResources(BedWarsResourceType type){
		List<Map<String, Object>> temps = (List<Map<String, Object>>) this.getConfig().get("custom.resources."+type.toString());
		if(temps != null && !temps.isEmpty()){
			for(Map<String, Object> resource : temps){
				double x = Double.valueOf(resource.get("x").toString());
				double y = Double.valueOf(resource.get("y").toString());
				double z = Double.valueOf(resource.get("z").toString());
				float yaw = Float.valueOf(resource.get("yaw").toString());
				float pitch = Float.valueOf(resource.get("pitch").toString());
				World world = Bukkit.getWorld(resource.get("world").toString());
				if(world == null){
					world = Bukkit.createWorld(new WorldCreator(resource.get("world").toString()));
					if(world == null){
						continue;
					}
				}
				new BedWarsResource(this.getGame(),this,type,new Location(world,x,y,z,yaw,pitch));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void loadTraders(){
		List<Map<String, Object>> temps = (List<Map<String, Object>>) this.getConfig().get("custom.traders");
		if(temps != null && !temps.isEmpty()){
			for(Map<String, Object> trader : temps){
				double x = Double.valueOf(trader.get("x").toString());
				double y = Double.valueOf(trader.get("y").toString());
				double z = Double.valueOf(trader.get("z").toString());
				float yaw = Float.valueOf(trader.get("yaw").toString());
				float pitch = Float.valueOf(trader.get("pitch").toString());
				World world = Bukkit.getWorld(trader.get("world").toString());
				if(world == null){
					world = Bukkit.createWorld(new WorldCreator(trader.get("world").toString()));
					if(world == null){
						continue;
					}
				}
				new BedWarsTrader(this,new Location(world,x,y,z,yaw,pitch));
			}
		}
	}

	public boolean isPlayerBlock(Block block){
		return this.playerBlocks.contains(block);
	}

	public void addPlayerBlock(Block block){
		playerBlocks.add(block);
	}

	public void removePlayerBlock(Block block){
		playerBlocks.remove(block);
	}

	public boolean isLocationInArena(Location location){
		return location.toVector().isInAABB(minVector,maxVector);
	}

	public boolean isBlockInArena(Block block){
		return block.getLocation().toVector().isInAABB(minVector,maxVector);
	}
}