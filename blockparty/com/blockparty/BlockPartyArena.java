package com.blockparty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import com.games.Games;
import com.games.arena.GameArena;
import com.games.player.GamePlayer;
import com.games.utils.LocationUtil;
import com.games.utils.RandomUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;

public class BlockPartyArena extends GameArena {

	private Vector locMin;
	private Vector locMax;
	private BlockPartyBlock currentBlock;

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

	public Vector getLocMin(){
		return locMin;
	}

	public Vector getLocMax(){
		return locMax;
	}

	public Location getLobbyLocation(){
		if(lobbyLocation == null) lobbyLocation = LocationUtil.getConfigLocation(this.getConfig(),"custom.lobbySpawn");
		return lobbyLocation;
	}

	public Location getGameLocation(){
		if(gameLocation == null) gameLocation = LocationUtil.getConfigLocation(this.getConfig(),"custom.gameSpawn");
		return gameLocation;
	}

	public BlockPartyBlock getCurrentBlock(){
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
					Block block = this.getWorld().getBlockAt(x,y,z);
					if(force || block.getType() != this.getCurrentBlock().getType() || block.getData() != this.getCurrentBlock().getData()){
						block.setType(Material.AIR);
					}
					List<Entity> entities = (List<Entity>) this.getWorld().getNearbyEntities(this.getGameLocation(),20,10,20);
					for(Entity entity : entities){
	                    if(!(entity instanceof Item)) continue;
	                    entity.remove();
					}
				}
			}
		}
	}

	public void teleportAboveFloor(GamePlayer gPlayer){
		Location location = gPlayer.getPlayer().getLocation().clone();
		if(location.getBlockY() >= 0){
			int maxY = 0;
			for(Vector2D vector : POS_VOLUMES){
				for(int y=locMax.getBlockY();y>=locMin.getBlockY();y--){
					if(!location.getWorld().getBlockAt(location.getBlockX()+vector.x,y,location.getBlockZ()+vector.z).isEmpty()){
						if(maxY < y) maxY = y;
					}
				}
			}
			if(maxY != 0){
				if(location.getBlockY() <= maxY){
					location.setY(maxY+1.1);
					gPlayer.getPlayer().teleport(location);
				}
			}
		}
	}

	public Location getRandomPickupLocation(){
		int randX = RandomUtil.getRandomInteger(locMin.getBlockX(),locMax.getBlockX());
		int randZ = RandomUtil.getRandomInteger(locMin.getBlockZ(),locMax.getBlockZ());
		int randY = 0;
		for(int y=locMax.getBlockY();y>=locMin.getBlockY();y--){
			if(this.getWorld().getBlockAt(randX,y,randZ).isEmpty() && !this.getWorld().getBlockAt(randX,y-1,randZ).isEmpty()){
				randY = y;
				break;
			}
		}
		if(randY == 0) return this.getRandomPickupLocation();
		return new Location(this.getWorld(),randX,randY,randZ);
	}

	public boolean isBlockInArena(Location location){
		Vector vec = new Vector(location.getBlockX(),location.getBlockY(),location.getBlockZ());
		return vec.containedWithinBlock(locMin,locMax);
	}

	public Location getStartLocation(int index,int max){
		Location location = this.getGameLocation().clone();
		double angle = index*(2*Math.PI)/max;
		Vector vector = new Vector(Math.cos(angle),0,Math.sin(angle)).multiply(4.0);
		location.add(vector.getX(),vector.getY(),vector.getZ());
		location.setDirection(this.getGameLocation().getDirection());
		location = this.setLocationLookingAt(location,this.getGameLocation());
		return location;
	}

	private Location setLocationLookingAt(Location loc,Location lookat){
		loc = loc.clone();

		double dx = lookat.getX() - loc.getX();
		double dy = lookat.getY() - loc.getY();
		double dz = lookat.getZ() - loc.getZ();

		if(dx != 0){
			if(dx < 0){
				loc.setYaw((float) (1.5 * Math.PI));
			} else {
				loc.setYaw((float) (0.5 * Math.PI));
			}
			loc.setYaw(loc.getYaw() - (float) Math.atan(dz / dx));
		} else if(dz < 0){
			loc.setYaw((float) Math.PI);
		}

		double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
		loc.setPitch((float) -Math.atan(dy / dxz));
		loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
		loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);

		return loc;
    }

	private static final Vector2D[] POS_VOLUMES = new Vector2D[]{
		new Vector2D(0,0),
		new Vector2D(0,1),
		new Vector2D(1,0),
		new Vector2D(0,-1),
		new Vector2D(-1,0),
	};

	public static class Vector2D {
		public int x;
		public int z;

		public Vector2D(int x,int z){
			this.x = x;
			this.z = z;
		}
	}
}