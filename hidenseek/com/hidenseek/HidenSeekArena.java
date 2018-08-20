package com.hidenseek;

import com.games.arena.GameArena;
import com.hidenseek.HidenSeekTeam.HidenSeekTeamType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import realcraft.bukkit.utils.LocationUtil;

import java.util.HashMap;
import java.util.Random;

public class HidenSeekArena extends GameArena {

	private Location minLocation;
	private Location maxLocation;

	private Location hidersSpawn;
	private Location seekersSpawn;

	private HashMap<Material,Block> blocks = new HashMap<Material,Block>();

	public HidenSeekArena(HidenSeek game,String name){
		super(game,name);
		minLocation = LocationUtil.getConfigLocation(this.getConfig(),"custom.locMin");
		maxLocation = LocationUtil.getConfigLocation(this.getConfig(),"custom.locMax");
		this.loadSpawns();
		this.loadBlocks();
	}

	public HidenSeek getGame(){
		return (HidenSeek) super.getGame();
	}

	public Location getTeamSpawn(HidenSeekTeamType type){
		if(type == HidenSeekTeamType.HIDERS) return hidersSpawn;
		else if(type == HidenSeekTeamType.SEEKERS) return seekersSpawn;
		return null;
	}

	private void loadSpawns(){
		this.hidersSpawn = LocationUtil.getConfigLocation(this.getConfig(),"custom.hidersSpawn");
		this.seekersSpawn = LocationUtil.getConfigLocation(this.getConfig(),"custom.seekersSpawn");
	}

	public Block getRandomBlock(){
		Random generator = new Random();
		Object [] values = blocks.values().toArray();
		return (Block) values[generator.nextInt(values.length)];
	}

	private void loadBlocks(){
		int minX = minLocation.getBlockX();
		int minY = minLocation.getBlockY();
		int minZ = minLocation.getBlockZ();
		for(int x=minX;x<=maxLocation.getBlockX();x++){
			for(int y=minY;y<=maxLocation.getBlockY();y++){
				for(int z=minZ;z<=maxLocation.getBlockZ();z++){
					Block block = minLocation.getWorld().getBlockAt(x,y,z);
					if(this.getGame().isBlockValid(block)){
						blocks.put(block.getType(),block);
					}
				}
			}
		}
	}
}