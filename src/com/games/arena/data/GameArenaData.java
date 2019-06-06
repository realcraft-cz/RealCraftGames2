package com.games.arena.data;

import com.games.arena.GameArena;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GameArenaData {

	private GameArena arena;
	private JsonObject objects;

	public GameArenaData(GameArena arena,String data){
		this.arena = arena;
		JsonElement element = new JsonParser().parse(data);
		if(element.isJsonObject()) objects = element.getAsJsonObject();
		else objects = new JsonObject();
	}

	public GameArena getArena(){
		return arena;
	}

	public boolean containsKey(String key){
		return objects.has(key);
	}

	public JsonElement getElement(String key){
		return objects.get(key);
	}
}