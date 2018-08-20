package com.dominate;

import com.dominate.DominateKit.DominateKitType;
import com.dominate.DominateTeam.DominateTeamType;
import com.games.arena.GameArena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.util.Vector;
import realcraft.bukkit.utils.LocationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DominateArena extends GameArena {

	private Location redSpawn;
	private Location blueSpawn;

	private ArrayList<DominateEmerald> emeralds = new ArrayList<DominateEmerald>();
	private ArrayList<DominatePoint> points = new ArrayList<DominatePoint>();
	private ArrayList<DominateKit> kits = new ArrayList<DominateKit>();

	private Location minLocation;
	private Location maxLocation;
	private Vector minVector;
	private Vector maxVector;

	public DominateArena(Dominate game,String name){
		super(game,name);
		this.loadSpawns();
		this.loadEmeralds();
		this.loadPoints();
		this.loadKits();
		minLocation = LocationUtil.getConfigLocation(this.getConfig(),"custom.locMin");
		maxLocation = LocationUtil.getConfigLocation(this.getConfig(),"custom.locMax");
		minVector = minLocation.toVector();
		maxVector = maxLocation.toVector();
	}

	public Dominate getGame(){
		return (Dominate) super.getGame();
	}

	public Location getTeamSpawn(DominateTeamType type){
		if(type == DominateTeamType.RED) return redSpawn;
		else if(type == DominateTeamType.BLUE) return blueSpawn;
		return null;
	}

	/*public DominateSpawnArea getSpawnArea(DominateTeamType type){
		if(type == DominateTeamType.RED) return redSpawnArea;
		else if(type == DominateTeamType.BLUE) return blueSpawnArea;
		return null;
	}*/

	public Location getMinLocation(){
		return minLocation;
	}

	public boolean isLocationInArena(Location location){
		return location.toVector().isInAABB(minVector,maxVector);
	}

	public ArrayList<DominateEmerald> getEmeralds(){
		return emeralds;
	}

	public ArrayList<DominatePoint> getPoints(){
		return points;
	}

	public ArrayList<DominateKit> getKits(){
		return kits;
	}

	public void reset(){
		for(DominateEmerald emerald : this.getEmeralds()){
			emerald.reset();
		}
		for(DominatePoint point : this.getPoints()){
			point.reset();
		}
		for(DominateKit kit : this.getKits()){
			kit.reset();
		}
	}

	private void loadSpawns(){
		this.redSpawn = LocationUtil.getConfigLocation(this.getConfig(),"custom.spawns."+DominateTeamType.RED.toString()+".spawn");
		this.blueSpawn = LocationUtil.getConfigLocation(this.getConfig(),"custom.spawns."+DominateTeamType.BLUE.toString()+".spawn");
	}

	/*@SuppressWarnings("unchecked")
	private void loadSpawnAreas(){
		List<Map<String, Object>> tempPoints;
		tempPoints = (List<Map<String, Object>>) this.getConfig().get("custom.spawns."+DominateTeamType.RED.toString()+".area");
		if(tempPoints != null && !tempPoints.isEmpty()){
			for(Map<String, Object> point : tempPoints){
				double x = Math.round(Double.valueOf(point.get("x").toString()))+0.5;
				double z = Math.round(Double.valueOf(point.get("z").toString()))+0.5;
				redSpawnArea.addPoint(x,z);
			}
		}
		tempPoints = (List<Map<String, Object>>) this.getConfig().get("custom.spawns."+DominateTeamType.BLUE.toString()+".area");
		if(tempPoints != null && !tempPoints.isEmpty()){
			for(Map<String, Object> point : tempPoints){
				double x = Math.round(Double.valueOf(point.get("x").toString()))+0.5;
				double z = Math.round(Double.valueOf(point.get("z").toString()))+0.5;
				blueSpawnArea.addPoint(x,z);
			}
		}
	}*/

	@SuppressWarnings("unchecked")
	private void loadEmeralds(){
		List<Map<String, Object>> tempPoints = (List<Map<String, Object>>) this.getConfig().get("custom.emeralds");
		if(tempPoints != null && !tempPoints.isEmpty()){
			for(Map<String, Object> point : tempPoints){
				double x = Math.floor(Double.valueOf(point.get("x").toString()))+0.5;
				double y = Math.floor(Double.valueOf(point.get("y").toString()))+1.0;
				double z = Math.floor(Double.valueOf(point.get("z").toString()))+0.5;
				float yaw = Float.valueOf(point.get("yaw").toString());
				float pitch = Float.valueOf(point.get("pitch").toString());
				World world = Bukkit.getWorld(point.get("world").toString());
				if(world == null){
					world = Bukkit.createWorld(new WorldCreator(point.get("world").toString()));
					if(world == null){
						continue;
					}
				}
				Location location = new Location(world,x,y,z,yaw,pitch);
				emeralds.add(new DominateEmerald(this.getGame(),this,location));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void loadPoints(){
		List<Map<String, Object>> tempPoints = (List<Map<String, Object>>) this.getConfig().get("custom.points");
		if(tempPoints != null && !tempPoints.isEmpty()){
			for(Map<String, Object> point : tempPoints){
				String name = point.get("name").toString();
				double x = Math.floor(Double.valueOf(point.get("x").toString()))+0.5;
				double y = Math.floor(Double.valueOf(point.get("y").toString()))+0.5;
				double z = Math.floor(Double.valueOf(point.get("z").toString()))+0.5;
				float yaw = Float.valueOf(point.get("yaw").toString());
				float pitch = Float.valueOf(point.get("pitch").toString());
				World world = Bukkit.getWorld(point.get("world").toString());
				if(world == null){
					world = Bukkit.createWorld(new WorldCreator(point.get("world").toString()));
					if(world == null){
						continue;
					}
				}
				Location location = new Location(world,x,y,z,yaw,pitch);
				points.add(new DominatePoint(this.getGame(),this,name,location));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void loadKits(){
		List<Map<String, Object>> tempPoints = (List<Map<String, Object>>) this.getConfig().get("custom.kits");
		if(tempPoints != null && !tempPoints.isEmpty()){
			int index = 0;
			for(Map<String, Object> point : tempPoints){
				DominateKitType type = DominateKitType.values()[index++];
				if(index >= 5) index = 0;
				double x = Math.floor(Double.valueOf(point.get("x").toString()))+0.5;
				double y = Math.round(Double.valueOf(point.get("y").toString()));
				double z = Math.floor(Double.valueOf(point.get("z").toString()))+0.5;
				float yaw = Float.valueOf(point.get("yaw").toString());
				float pitch = Float.valueOf(point.get("pitch").toString());
				World world = Bukkit.getWorld(point.get("world").toString());
				if(world == null){
					world = Bukkit.createWorld(new WorldCreator(point.get("world").toString()));
					if(world == null){
						continue;
					}
				}
				if(type != null){
					Location location = new Location(world,x,y,z,yaw,pitch);
					kits.add(new DominateKit(this.getGame(),this,type,location));
				}
			}
		}
	}

	/*public class DominateSpawnArea {
		ArrayList<Double> pointsX = new ArrayList<Double>();
		ArrayList<Double> pointsZ = new ArrayList<Double>();

		public void addPoint(double x,double z){
			pointsX.add(x);
			pointsZ.add(z);
		}

		public boolean contains(double x,double z){
			int nvert = pointsX.size();
		    int i,j;
		    boolean result = false;
		    for(i=0,j=nvert-1;i<nvert;j=i++){
		        if(((pointsZ.get(i)>z) != (pointsZ.get(j)>z)) && (x < (pointsX.get(j)-pointsX.get(i)) * (z-pointsZ.get(i)) / (pointsZ.get(j)-pointsZ.get(i)) + pointsX.get(i))) result = !result;
		    }
		    return result;
		}
	}*/
}