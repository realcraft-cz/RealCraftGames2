package com.dominate;

import com.dominate.DominateKit.DominateKitType;
import com.dominate.DominateTeam.DominateTeamType;
import com.games.arena.GameArena;
import com.games.arena.data.GameArenaData;
import com.games.arena.data.GameArenaDataList;
import com.games.arena.data.GameArenaDataLocation;
import com.games.arena.data.GameArenaDataMap;
import com.google.gson.JsonElement;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DominateArena extends GameArena {

	private HashMap<DominateTeamType,Location> spawns = new HashMap<>();
	private ArrayList<DominateEmerald> emeralds = new ArrayList<>();
	private ArrayList<DominatePoint> points = new ArrayList<>();
	private ArrayList<DominateKit> kits = new ArrayList<>();

	public DominateArena(Dominate game,int id){
		super(game,id);
	}

	public Dominate getGame(){
		return (Dominate) super.getGame();
	}

	public Location getTeamSpawn(DominateTeamType type){
		return spawns.get(type);
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

	@Override
	public void resetRegion(){
		this.getRegion().reset();
	}

	@Override
	public void loadData(GameArenaData data){
		this.loadSpawns(data);
		this.loadEmeralds(data);
		this.loadPoints(data);
		this.loadKits(data);
	}

	private void loadSpawns(GameArenaData data){
		GameArenaDataMap<GameArenaDataLocation> tmpData = new GameArenaDataMap<>(this,"spawns",GameArenaDataLocation.class);
		tmpData.loadData(data);
		for(Map.Entry<String,GameArenaDataLocation> entry : tmpData.getValues().entrySet()){
			spawns.put(DominateTeamType.getByName(entry.getKey()),entry.getValue().getLocation());
		}
	}

	private void loadEmeralds(GameArenaData data){
		GameArenaDataList<GameArenaDataLocation> tmpData = new GameArenaDataList<>(this,"emeralds",GameArenaDataLocation.class);
		tmpData.loadData(data);
		for(GameArenaDataLocation entry : tmpData.getValues()){
			emeralds.add(new DominateEmerald(this.getGame(),this,entry.getLocation().add(0.5,0,0.5)));
		}
	}

	private void loadPoints(GameArenaData data){
		GameArenaDataList<GameArenaDataPoint> tmpData = new GameArenaDataList<>(this,"beacons",GameArenaDataPoint.class);
		tmpData.loadData(data);
		for(GameArenaDataPoint entry : tmpData.getValues()){
			points.add(new DominatePoint(this.getGame(),this,entry.getName(),entry.getLocation().add(0.5,-1,0.5)));
		}
	}

	private void loadKits(GameArenaData data){
		GameArenaDataList<GameArenaDataLocation> tmpData = new GameArenaDataList<>(this,"kits",GameArenaDataLocation.class);
		tmpData.loadData(data);
		int index = 0;
		for(GameArenaDataLocation entry : tmpData.getValues()){
			DominateKitType type = DominateKitType.values()[index++];
			if(index >= 5) index = 0;
			kits.add(new DominateKit(this.getGame(),this,type,entry.getLocation()));
		}
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

	public static class GameArenaDataPoint extends GameArenaDataLocation {

		private String name;

		public GameArenaDataPoint(GameArena arena,String name){
			super(arena,name);
		}

		public GameArenaDataPoint(GameArena arena,JsonElement element){
			super(arena,element);
			this.name = element.getAsJsonObject().get("name").getAsString();
		}

		public String getName(){
			return name;
		}
	}
}