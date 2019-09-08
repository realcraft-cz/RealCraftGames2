package com.bedwars;

import com.bedwars.BedWarsResource.BedWarsResourceType;
import com.bedwars.BedWarsTeam.BedWarsTeamType;
import com.games.arena.GameArena;
import com.games.arena.data.GameArenaData;
import com.games.arena.data.GameArenaDataList;
import com.games.arena.data.GameArenaDataLocation;
import com.games.arena.data.GameArenaDataMap;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BedWarsArena extends GameArena {

	private BedWarsArenaRegion region = new BedWarsArenaRegion(this);

	private HashMap<BedWarsTeamType,Location> spawns = new HashMap<>();
	private HashMap<BedWarsTeamType,Location> beds = new HashMap<>();
	private ArrayList<Block> playerBlocks = new ArrayList<>();

	public BedWarsArena(BedWars game,int id){
		super(game,id);
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

	public BedWarsArenaRegion getRegion(){
		return region;
	}

	@Override
	public void resetRegion(){
		this.getRegion().reset();
	}

	@Override
	public void loadData(GameArenaData data){
		this.loadSpawns(data);
		this.loadBeds(data);
		this.loadResources(data);
		this.loadTraders(data);
	}

	private void loadSpawns(GameArenaData data){
		GameArenaDataMap<GameArenaDataLocation> tmpData = new GameArenaDataMap<>(this,"spawns",GameArenaDataLocation.class);
		tmpData.loadData(data);
		for(Map.Entry<String,GameArenaDataLocation> entry : tmpData.getValues().entrySet()){
			spawns.put(BedWarsTeamType.getByName(entry.getKey()),entry.getValue().getLocation());
		}
	}

	private void loadBeds(GameArenaData data){
		GameArenaDataMap<GameArenaDataLocation> tmpData = new GameArenaDataMap<>(this,"beds",GameArenaDataLocation.class);
		tmpData.loadData(data);
		for(Map.Entry<String,GameArenaDataLocation> entry : tmpData.getValues().entrySet()){
			beds.put(BedWarsTeamType.getByName(entry.getKey()),entry.getValue().getLocation());
		}
	}

	private void loadResources(GameArenaData data){
		GameArenaDataList<GameArenaDataLocation> tmpData;
		tmpData = new GameArenaDataList<>(this,"bronze",GameArenaDataLocation.class);
		tmpData.loadData(data);
		for(GameArenaDataLocation entry : tmpData.getValues()){
			new BedWarsResource(this.getGame(),this,BedWarsResourceType.BRONZE,entry.getLocation());
		}
		tmpData = new GameArenaDataList<>(this,"iron",GameArenaDataLocation.class);
		tmpData.loadData(data);
		for(GameArenaDataLocation entry : tmpData.getValues()){
			new BedWarsResource(this.getGame(),this,BedWarsResourceType.IRON,entry.getLocation());
		}
		tmpData = new GameArenaDataList<>(this,"gold",GameArenaDataLocation.class);
		tmpData.loadData(data);
		for(GameArenaDataLocation entry : tmpData.getValues()){
			new BedWarsResource(this.getGame(),this,BedWarsResourceType.GOLD,entry.getLocation());
		}
	}

	private void loadTraders(GameArenaData data){
		GameArenaDataList<GameArenaDataLocation> tmpData = new GameArenaDataList<>(this,"traders",GameArenaDataLocation.class);
		tmpData.loadData(data);
		for(GameArenaDataLocation entry : tmpData.getValues()){
			new BedWarsTrader(this,entry.getLocation());
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
}