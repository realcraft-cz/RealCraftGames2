package com.games.arena.data;

import com.games.arena.GameArena;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class GameArenaDataList<E extends GameArenaDataEntry> extends GameArenaDataEntry {

	private Class<E> clazz;
	private ArrayList<E> values = new ArrayList<>();

	public GameArenaDataList(GameArena arena,String name,Class<E> clazz){
		super(arena,name);
		this.clazz = clazz;
	}

	public ArrayList<E> getValues(){
		return values;
	}

	@Override
	public void loadData(GameArenaData data){
		if(data.containsKey(this.getName())){
			JsonArray array = data.getElement(this.getName()).getAsJsonArray();
			for(JsonElement element : array){
				try {
					values.add(clazz.getConstructor(GameArena.class,JsonElement.class).newInstance(data.getArena(),element));
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