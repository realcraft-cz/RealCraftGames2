package com.races;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.races.arenas.RaceArena;

public class RaceCheckpoint {

	private RaceArena arena;
	private int index;
	private RaceCheckpointType type;
	private Location minLoc;
	private Location maxLoc;
	private Location centerLoc;

	public RaceCheckpoint(RaceArena arena,int index,RaceCheckpointType type,Location locFrom,Location locTo){
		this.arena = arena;
		this.index = index;
		this.type = type;
		this.minLoc = Vector.getMinimum(locFrom.toVector(),locTo.toVector()).toLocation(locFrom.getWorld());
		this.maxLoc = Vector.getMaximum(locFrom.toVector(),locTo.toVector()).toLocation(locFrom.getWorld());
	}

	public int getIndex(){
		return index;
	}

	public RaceCheckpointType getType(){
		return type;
	}

	public boolean isLocationInside(Location location){
		return (location.getBlockX() >= minLoc.getBlockX() && location.getBlockX() <= maxLoc.getBlockX()
				&& location.getBlockY() >= minLoc.getBlockY() && location.getBlockY() <= maxLoc.getBlockY()
				&& location.getBlockZ() >= minLoc.getBlockZ() && location.getBlockZ() <= maxLoc.getBlockZ());
	}

	public Location getCenterLocation(){
		if(centerLoc == null){
			centerLoc = new Location(minLoc.getWorld(),
					maxLoc.getBlockX()-((maxLoc.getBlockX()-minLoc.getBlockX())/2),
					maxLoc.getBlockY()-((maxLoc.getBlockY()-minLoc.getBlockY())/2),
					maxLoc.getBlockZ()-((maxLoc.getBlockZ()-minLoc.getBlockZ())/2)
			);
		}
		return centerLoc;
	}

	public RaceCheckpoint getNextCheckpoint(){
		if(index < arena.getCheckpoints().size()-1) return arena.getCheckpoints().get(index+1);
		return arena.getCheckpoints().get(0);
	}

	public double distance(RaceCheckpoint checkpoint,Location location){
		double maxDist = this.getCenterLocation().distanceSquared(checkpoint.getCenterLocation());
		return location.distanceSquared(checkpoint.getCenterLocation())/maxDist;
	}

	public double distance(Location from,Location to){
		double maxDist = from.distanceSquared(this.getCenterLocation());
		return to.distanceSquared(this.getCenterLocation())/maxDist;
	}

	@Override
	public boolean equals(Object object){
		if(object instanceof RaceCheckpoint){
			RaceCheckpoint toCompare = (RaceCheckpoint) object;
			return (toCompare.getIndex() == this.getIndex());
		}
		return false;
	}

	public enum RaceCheckpointType {
		CHECKPOINT, FINISH;

		@Override
		public String toString(){
			return this.name().toLowerCase();
		}

		public static RaceCheckpointType fromName(String name){
			return RaceCheckpointType.valueOf(name.toUpperCase());
		}
	}
}