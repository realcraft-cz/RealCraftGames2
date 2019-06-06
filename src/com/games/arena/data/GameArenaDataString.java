package com.games.arena.data;

import com.games.arena.GameArena;
import com.google.gson.JsonElement;

public class GameArenaDataString extends GameArenaDataEntry {

	private String value;

	public GameArenaDataString(GameArena arena,String name){
		super(arena,name);
	}

	public GameArenaDataString(GameArena arena,JsonElement element){
		super(arena);
		value = element.getAsString();
	}

	public String getValue(){
		return value;
	}

	@Override
	public void loadData(GameArenaData data){
		if(data.containsKey(this.getName())){
			GameArenaDataString tmp = new GameArenaDataString(data.getArena(),data.getElement(this.getName()));
			value = tmp.getValue();
		}
	}

	@Override
	public boolean equals(Object object){
		return false;
	}
}