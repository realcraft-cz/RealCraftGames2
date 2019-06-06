package com.games.arena.data;

import com.games.arena.GameArena;
import com.google.gson.JsonElement;

public class GameArenaDataInteger extends GameArenaDataEntry {

	private int value;

	public GameArenaDataInteger(GameArena arena,String name){
		super(arena,name);
	}

	public GameArenaDataInteger(GameArena arena,JsonElement element){
		super(arena);
		value = element.getAsInt();
	}

	public int getValue(){
		return value;
	}

	@Override
	public void loadData(GameArenaData data){
		if(data.containsKey(this.getName())){
			GameArenaDataInteger tmp = new GameArenaDataInteger(data.getArena(),data.getElement(this.getName()));
			value = tmp.getValue();
		}
	}

	@Override
	public boolean equals(Object object){
		return false;
	}
}