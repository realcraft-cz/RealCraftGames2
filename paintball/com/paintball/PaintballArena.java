package com.paintball;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;

import com.games.arena.GameArena;
import com.games.utils.LocationUtil;
import com.paintball.PaintballTeam.PaintballTeamType;
import com.paintball.specials.PaintballSpecial;
import com.paintball.specials.PaintballSpecialGrenade;
import com.paintball.specials.PaintballSpecialJump;
import com.paintball.specials.PaintballSpecialMachineGun;
import com.paintball.specials.PaintballSpecialSpeed;

public class PaintballArena extends GameArena {

	private Location redSpawn;
	private Location blueSpawn;

	private ArrayList<Location> dropLocations = new ArrayList<Location>();
	private ArrayList<PaintballSpecial> specials = new ArrayList<PaintballSpecial>();

	public PaintballArena(Paintball game,String name){
		super(game,name);
		this.loadSpawns();
		this.loadDrops();
		this.loadJumps();
		this.loadSpeeds();
		this.loadMachineGuns();
		specials.add(new PaintballSpecialGrenade(game));
	}

	public Paintball getGame(){
		return (Paintball) super.getGame();
	}

	public Location getTeamSpawn(PaintballTeamType type){
		if(type == PaintballTeamType.RED) return redSpawn;
		else if(type == PaintballTeamType.BLUE) return blueSpawn;
		return null;
	}

	public ArrayList<Location> getDropLocations(){
		return dropLocations;
	}

	public ArrayList<PaintballSpecial> getSpecials(){
		return specials;
	}

	private void loadSpawns(){
		this.redSpawn = LocationUtil.getConfigLocation(this.getConfig(),"custom.spawns."+PaintballTeamType.RED.toString());
		this.blueSpawn = LocationUtil.getConfigLocation(this.getConfig(),"custom.spawns."+PaintballTeamType.BLUE.toString());
	}

	@SuppressWarnings("unchecked")
	private void loadDrops(){
		List<Map<String, Object>> temps = (List<Map<String, Object>>) this.getConfig().get("custom.drops");
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
				dropLocations.add(new Location(world,x,y,z,yaw,pitch));
			}
		}
	}

	private void loadJumps(){
		if(this.getConfig().getConfigurationSection("custom.jumps") != null){
			for(String key : this.getConfig().getConfigurationSection("custom.jumps").getKeys(false)){
				ConfigurationSection section = this.getConfig().getConfigurationSection("custom.jumps."+key);
				double force = section.getDouble("force");
				Location location1 = LocationUtil.getConfigLocation(section,"minLoc");
				Location location2 = LocationUtil.getConfigLocation(section,"maxLoc");
				specials.add(new PaintballSpecialJump(this.getGame(),force,location1,location2));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void loadSpeeds(){
		List<Map<String, Object>> temps = (List<Map<String, Object>>) this.getConfig().get("custom.speeds");
		if(temps != null && !temps.isEmpty()){
			for(Map<String, Object> resource : temps){
				int duration = Integer.valueOf(resource.get("duration").toString());
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
				specials.add(new PaintballSpecialSpeed(this.getGame(),duration,new Location(world,x,y,z,yaw,pitch)));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void loadMachineGuns(){
		List<Map<String, Object>> temps = (List<Map<String, Object>>) this.getConfig().get("custom.machineguns");
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
				specials.add(new PaintballSpecialMachineGun(this.getGame(),new Location(world,x,y,z,yaw,pitch)));
			}
		}
	}
}