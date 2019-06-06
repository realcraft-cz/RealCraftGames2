package com.games.arena.data;

import com.games.arena.GameArena;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class GameArenaDataMap<E extends GameArenaDataEntry> extends GameArenaDataEntry {

	private Class<E> clazz;
	private HashMap<String,E> values = new HashMap<>();

	public GameArenaDataMap(GameArena arena,String name,Class<E> clazz){
		super(arena,name);
		this.clazz = clazz;
	}

	public HashMap<String,E> getValues(){
		return values;
	}

	@Override
	public void loadData(GameArenaData data){
		if(data.containsKey(this.getName())){
			JsonObject object = data.getElement(this.getName()).getAsJsonObject();
			for(java.util.Map.Entry<String,JsonElement> entry : object.entrySet()){
				try {
					values.put(entry.getKey(),clazz.getConstructor(GameArena.class,JsonElement.class).newInstance(data.getArena(),entry.getValue()));
				} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public boolean equals(Object object){
		return false;
	}
}