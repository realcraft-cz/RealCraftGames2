package com.games.arena.data;

import com.games.arena.GameArena;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Location;

public class GameArenaDataLocation extends GameArenaDataEntry {

	private Location location;

	public GameArenaDataLocation(GameArena arena,String name){
		super(arena,name);
	}

	public GameArenaDataLocation(GameArena arena,JsonElement element){
		super(arena);
		JsonObject json = element.getAsJsonObject();
		if(json.has("x")){
			double x = json.get("x").getAsDouble();
			double y = json.get("y").getAsDouble();
			double z = json.get("z").getAsDouble();
			float yaw = json.get("yaw").getAsFloat();
			float pitch = json.get("pitch").getAsFloat();
			this.location = new Location(arena.getWorld(),x,y,z,yaw,pitch);
		}
	}

	public Location getLocation(){
		return location;
	}

	@Override
	public void loadData(GameArenaData data){
		if(data.containsKey(this.getName())){
			GameArenaDataLocation tmp = new GameArenaDataLocation(data.getArena(),data.getElement(this.getName()));
			location = tmp.getLocation();
		}
	}

	@Override
	public boolean equals(Object object){
		if(object instanceof GameArenaDataLocation){
			GameArenaDataLocation toCompare = (GameArenaDataLocation) object;
			return (toCompare.getLocation().getBlockX() == this.getLocation().getBlockX() &&
					toCompare.getLocation().getBlockY() == this.getLocation().getBlockY() &&
					toCompare.getLocation().getBlockZ() == this.getLocation().getBlockZ()
			);
		}
		return false;
	}
}