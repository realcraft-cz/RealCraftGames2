package com.ragemode;

import com.games.arena.GameArena;
import com.games.arena.data.GameArenaData;
import com.games.arena.data.GameArenaDataList;
import com.games.arena.data.GameArenaDataLocation;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Random;

public class RageModeArena extends GameArena {

	private ArrayList<Location> spawns = new ArrayList<Location>();

	public RageModeArena(RageMode game,int id){
		super(game,id);
	}

	public RageMode getGame(){
		return (RageMode) super.getGame();
	}

	public Location getRandomSpawn(){
		return spawns.get(new Random().nextInt(spawns.size()));
	}

	@Override
	public void resetRegion(){
		this.getRegion().reset();
	}

	@Override
	public void loadData(GameArenaData data){
		this.loadSpawns(data);
	}

	private void loadSpawns(GameArenaData data){
		GameArenaDataList<GameArenaDataLocation> tmpData = new GameArenaDataList<>(this,"spawns",GameArenaDataLocation.class);
		tmpData.loadData(data);
		for(GameArenaDataLocation entry : tmpData.getValues()){
			spawns.add(entry.getLocation());
		}
	}
}