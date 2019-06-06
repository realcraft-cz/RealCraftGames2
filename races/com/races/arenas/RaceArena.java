package com.races.arenas;

import com.games.arena.GameArena;
import com.games.arena.data.*;
import com.races.RaceCheckpoint;
import com.races.RaceCheckpoint.RaceCheckpointType;
import com.races.RaceType;
import com.races.Races;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class RaceArena extends GameArena {

	private RaceType type;
	private int rounds;
	private ArrayList<Location> spawns = new ArrayList<Location>();
	private ArrayList<RaceCheckpoint> checkpoints = new ArrayList<RaceCheckpoint>();
	private RaceBarrier barrier;

	public RaceArena(Races game,int id){
		super(game,id);
	}

	public Races getGame(){
		return (Races) super.getGame();
	}

	public RaceType getRaceType(){
		return type;
	}

	public int getRounds(){
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

	@Override
	public void resetRegion(){
		this.getRegion().reset();
	}

	@Override
	public void loadData(GameArenaData data){
		GameArenaDataString type = new GameArenaDataString(this,"type");
		type.loadData(data);
		this.type = RaceType.fromName(type.getValue());
		GameArenaDataInteger rounds = new GameArenaDataInteger(this,"rounds");
		rounds.loadData(data);
		this.rounds = rounds.getValue();
		this.loadSpawns(data);
		this.loadCheckpoints(data);
		this.loadBarrier(data);
	}

	private void loadSpawns(GameArenaData data){
		GameArenaDataList<GameArenaDataLocation> tmpData = new GameArenaDataList<>(this,"spawns",GameArenaDataLocation.class);
		tmpData.loadData(data);
		for(GameArenaDataLocation entry : tmpData.getValues()){
			spawns.add(entry.getLocation());
		}
	}

	private void loadCheckpoints(GameArenaData data){
		GameArenaDataList<GameArenaDataLocationArea> tmpData = new GameArenaDataList<>(this,"checkpoints",GameArenaDataLocationArea.class);
		tmpData.loadData(data);
		int index = 0;
		for(GameArenaDataLocationArea entry : tmpData.getValues()){
			checkpoints.add(new RaceCheckpoint(this,index++,(index < tmpData.getValues().size() ? RaceCheckpointType.CHECKPOINT : RaceCheckpointType.FINISH),entry.getMinLocation().getLocation(),entry.getMaxLocation().getLocation()));
		}
	}

	private void loadBarrier(GameArenaData data){
		GameArenaDataLocationArea entry = new GameArenaDataLocationArea(this,"barrier");
		entry.loadData(data);
		barrier = new RaceBarrier(entry.getMinLocation().getLocation(),entry.getMaxLocation().getLocation());
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