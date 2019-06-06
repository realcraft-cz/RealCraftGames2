package com.games.arena.data;

import com.games.arena.GameArena;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GameArenaDataLocationArea extends GameArenaDataEntry {

	private GameArenaDataLocation minLoc;
	private GameArenaDataLocation maxLoc;

	public GameArenaDataLocationArea(GameArena arena,String name){
		super(arena,name);
	}

	public GameArenaDataLocationArea(GameArena arena,JsonElement element){
		super(arena);
		JsonObject json = element.getAsJsonObject();
		this.minLoc = new GameArenaDataLocation(arena,json.get("from").getAsJsonObject());
		this.maxLoc = new GameArenaDataLocation(arena,json.get("to").getAsJsonObject());
	}

	public GameArenaDataLocation getMinLocation(){
		return minLoc;
	}

	public GameArenaDataLocation getMaxLocation(){
		return maxLoc;
	}

	@Override
	public void loadData(GameArenaData data){
		if(data.containsKey(this.getName())){
			GameArenaDataLocationArea tmp = new GameArenaDataLocationArea(data.getArena(),data.getElement(this.getName()));
			minLoc = tmp.getMinLocation();
			maxLoc = tmp.getMaxLocation();
		}
	}

	@Override
	public boolean equals(Object object){
		if(object instanceof GameArenaDataLocationArea){
			GameArenaDataLocationArea toCompare = (GameArenaDataLocationArea) object;
			return (toCompare.getMinLocation().equals(this.getMinLocation()) && toCompare.getMaxLocation().equals(this.getMaxLocation()));
		}
		return false;
	}
}