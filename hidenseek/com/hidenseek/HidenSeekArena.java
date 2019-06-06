package com.hidenseek;

import com.games.arena.GameArena;
import com.games.arena.data.GameArenaData;
import com.games.arena.data.GameArenaDataLocation;
import com.games.arena.data.GameArenaDataMap;
import com.hidenseek.HidenSeekTeam.HidenSeekTeamType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HidenSeekArena extends GameArena {

	private HashMap<HidenSeekTeamType,Location> spawns = new HashMap<>();
	private HashMap<Material,Block> blocks = new HashMap<Material,Block>();

	public HidenSeekArena(HidenSeek game,int id){
		super(game,id);
	}

	public HidenSeek getGame(){
		return (HidenSeek) super.getGame();
	}

	public Location getTeamSpawn(HidenSeekTeamType type){
		return spawns.get(type);
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
		GameArenaDataMap<GameArenaDataLocation> tmpData = new GameArenaDataMap<>(this,"spawns",GameArenaDataLocation.class);
		tmpData.loadData(data);
		for(Map.Entry<String,GameArenaDataLocation> entry : tmpData.getValues().entrySet()){
			spawns.put(HidenSeekTeamType.getByName(entry.getKey()),entry.getValue().getLocation());
		}
	}

	public Block getRandomBlock(){
		Random generator = new Random();
		Object [] values = blocks.values().toArray();
		return (Block) values[generator.nextInt(values.length)];
	}

	public void loadBlocks(){
		blocks.clear();
		for(int x=this.getRegion().getMinLocation().getBlockX();x<=this.getRegion().getMaxLocation().getBlockX();x++){
			for(int y=this.getRegion().getMinLocation().getBlockY();y<=this.getRegion().getMaxLocation().getBlockY();y++){
				for(int z=this.getRegion().getMinLocation().getBlockZ();z<=this.getRegion().getMaxLocation().getBlockZ();z++){
					Block block = this.getRegion().getMinLocation().getWorld().getBlockAt(x,y,z);
					if(this.getGame().isBlockValid(block)){
						blocks.put(block.getType(),block);
					}
				}
			}
		}
	}
}