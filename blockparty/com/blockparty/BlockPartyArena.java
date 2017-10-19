package com.blockparty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.games.Games;
import com.games.arena.GameArena;
import com.games.player.GamePlayer;
import com.games.utils.LocationUtil;
import com.games.utils.RandomUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;

public class BlockPartyArena extends GameArena {

	private World world;
	private Vector locMin;
	private Vector locMax;
	private Block currentBlock;

	private Location lobbyLocation;
	private Location gameLocation;

	private ArrayList<BlockPartyFloor> floors = new ArrayList<BlockPartyFloor>();
	private BlockPartyFloor currentFloor;

	public BlockPartyArena(BlockParty game,String name){
		super(game,name);
		Location location;
		location = LocationUtil.getConfigLocation(this.getConfig(),"custom.locMin");
		locMin = new com.sk89q.worldedit.util.Location(new BukkitWorld(this.getWorld()),location.getBlockX(),location.getBlockY(),location.getBlockZ()).toVector();
		location = LocationUtil.getConfigLocation(this.getConfig(),"custom.locMax");
		locMax = new com.sk89q.worldedit.util.Location(new BukkitWorld(this.getWorld()),location.getBlockX(),location.getBlockY(),location.getBlockZ()).toVector();
		this.loadFloors();
	}

	private void loadFloors(){
		for(String name : this.getConfig().getStringList("custom.floors")){
			File file = new File(Games.getInstance().getDataFolder()+"/"+this.getGame().getType().getName()+"/"+this.getName()+"/floors/"+name+".schematic");
			floors.add(new BlockPartyFloor(file));
		}
	}

	public BlockParty getGame(){
		return (BlockParty) super.getGame();
	}

	public World getWorld(){
		if(world == null) world = Bukkit.getWorld(this.getConfig().getString("custom.world"));
		return world;
	}

	public Location getLobbyLocation(){
		if(lobbyLocation == null) lobbyLocation = LocationUtil.getConfigLocation(this.getConfig(),"custom.lobbySpawn");
		return lobbyLocation;
	}

	public Location getGameLocation(){
		if(gameLocation == null) gameLocation = LocationUtil.getConfigLocation(this.getConfig(),"custom.gameSpawn");
		return gameLocation;
	}

	public Block getCurrentBlock(){
		return currentBlock;
	}

	public void chooseRandomBlock(){
		currentBlock = currentFloor.getRandomBlock(this.getWorld(),locMin);
		this.getGame().loadRoundInventory(currentBlock);
	}

	public ArrayList<BlockPartyFloor> getFloors(){
		return floors;
	}

	public BlockPartyFloor getCurrentFloor(){
		return currentFloor;
	}

	public void chooseDefaultFloor(){
		this.clearFloor(true);
		currentFloor = floors.get(0);
		currentFloor.paste(this.getWorld(),locMin);
		currentFloor.setUsed(true);
	}

	public void chooseRandomFloor(){
		this.clearFloor(true);
		currentFloor = this.getRandomFloor();
		currentFloor.paste(this.getWorld(),locMin);
		currentFloor.setUsed(true);
	}

	private BlockPartyFloor getRandomFloor(){
		BlockPartyFloor floor = floors.get(RandomUtil.getRandomInteger(0,floors.size()-1));
		if(floor.isUsed()) floor = this.getRandomFloor();
		return floor;
	}

	public void clearFloor(){
		this.clearFloor(false);
	}

	public void reset(){
		for(BlockPartyFloor floor : floors){
			floor.setUsed(false);
		}
	}

	@SuppressWarnings("deprecation")
	private void clearFloor(boolean force){
		for(int y=locMin.getBlockY();y<=locMax.getBlockY();y++){
			for(int x=locMin.getBlockX();x<=locMax.getBlockX();x++){
				for(int z=locMin.getBlockZ();z<=locMax.getBlockZ();z++){
					Block block = world.getBlockAt(x,y,z);
					if(force || block.getType() != this.getCurrentBlock().getType() || block.getData() != this.getCurrentBlock().getData()){
						block.setType(Material.AIR);
					}
					List<Entity> entities = (List<Entity>) world.getNearbyEntities(this.getGameLocation(),20,10,20);
					for(Entity entity : entities){
	                    if(entity instanceof Player) continue;
	                    entity.remove();
					}
				}
			}
		}
	}

	public void teleportAboveFloor(GamePlayer gPlayer){
		Location location = gPlayer.getPlayer().getLocation().clone();
		if(location.getBlockY() >= 0){
			Block topBlock = null;
			for(int y=locMax.getBlockY();y>=locMin.getBlockY();y--){
				if(!gPlayer.getPlayer().getWorld().getBlockAt(location.getBlockX(),y,location.getBlockZ()).isEmpty()){
					topBlock = gPlayer.getPlayer().getWorld().getBlockAt(location.getBlockX(),y,location.getBlockZ());
				}
			}
			if(topBlock != null){
				if(location.getBlockY() <= topBlock.getY()){
					location.setY(topBlock.getY()+1);
					gPlayer.getPlayer().teleport(location);
				}
			}
		}
	}
}