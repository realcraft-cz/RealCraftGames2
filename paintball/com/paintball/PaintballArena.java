package com.paintball;

import com.games.arena.GameArena;
import com.games.arena.data.*;
import com.google.gson.JsonElement;
import com.paintball.PaintballTeam.PaintballTeamType;
import com.paintball.specials.*;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PaintballArena extends GameArena {

	private HashMap<PaintballTeamType,Location> spawns = new HashMap<>();
	private ArrayList<Location> dropLocations = new ArrayList<>();
	private ArrayList<PaintballSpecial> specials = new ArrayList<>();

	public PaintballArena(Paintball game,int id){
		super(game,id);
		specials.add(new PaintballSpecialGrenade(game));
	}

	public Paintball getGame(){
		return (Paintball) super.getGame();
	}

	public Location getTeamSpawn(PaintballTeamType type){
		return spawns.get(type);
	}

	public ArrayList<Location> getDropLocations(){
		return dropLocations;
	}

	public ArrayList<PaintballSpecial> getSpecials(){
		return specials;
	}

	@Override
	public void resetRegion(){
		this.getRegion().reset();
	}

	@Override
	public void loadData(GameArenaData data){
		this.loadSpawns(data);
		this.loadDrops(data);
		this.loadJumps(data);
		this.loadSpeeds(data);
		this.loadMachineGuns(data);
	}

	private void loadSpawns(GameArenaData data){
		GameArenaDataMap<GameArenaDataLocation> tmpData = new GameArenaDataMap<>(this,"spawns",GameArenaDataLocation.class);
		tmpData.loadData(data);
		for(Map.Entry<String,GameArenaDataLocation> entry : tmpData.getValues().entrySet()){
			spawns.put(PaintballTeamType.getByName(entry.getKey()),entry.getValue().getLocation());
		}
	}

	private void loadDrops(GameArenaData data){
		GameArenaDataList<GameArenaDataLocation> tmpData = new GameArenaDataList<>(this,"drops",GameArenaDataLocation.class);
		tmpData.loadData(data);
		for(GameArenaDataLocation entry : tmpData.getValues()){
			dropLocations.add(entry.getLocation());
		}
	}

	private void loadJumps(GameArenaData data){
		GameArenaDataList<GameArenaDataJumpArea> tmpData = new GameArenaDataList<>(this,"jumps",GameArenaDataJumpArea.class);
		tmpData.loadData(data);
		for(GameArenaDataJumpArea entry : tmpData.getValues()){
			specials.add(new PaintballSpecialJump(this.getGame(),entry.getForce(),entry.getMinLocation().getLocation(),entry.getMaxLocation().getLocation()));
		}
	}

	private void loadSpeeds(GameArenaData data){
		GameArenaDataList<GameArenaDataSpeed> tmpData = new GameArenaDataList<>(this,"speeds",GameArenaDataSpeed.class);
		tmpData.loadData(data);
		for(GameArenaDataSpeed entry : tmpData.getValues()){
			specials.add(new PaintballSpecialSpeed(this.getGame(),entry.getDuration(),entry.getLocation()));
		}
	}

	private void loadMachineGuns(GameArenaData data){
		GameArenaDataList<GameArenaDataLocation> tmpData = new GameArenaDataList<>(this,"machineguns",GameArenaDataLocation.class);
		tmpData.loadData(data);
		for(GameArenaDataLocation entry : tmpData.getValues()){
			specials.add(new PaintballSpecialMachineGun(this.getGame(),entry.getLocation()));
		}
	}

	public static class GameArenaDataJumpArea extends GameArenaDataLocationArea {

		private double force;

		public GameArenaDataJumpArea(GameArena arena,String name){
			super(arena,name);
		}

		public GameArenaDataJumpArea(GameArena arena,JsonElement element){
			super(arena,element);
			this.force = element.getAsJsonObject().get("force").getAsDouble();
		}

		public double getForce(){
			return force;
		}
	}

	public static class GameArenaDataSpeed extends GameArenaDataLocation {

		private int duration;

		public GameArenaDataSpeed(GameArena arena,String name){
			super(arena,name);
		}

		public GameArenaDataSpeed(GameArena arena,JsonElement element){
			super(arena,element);
			this.duration = element.getAsJsonObject().get("duration").getAsInt();
		}

		public int getDuration(){
			return duration;
		}
	}
}