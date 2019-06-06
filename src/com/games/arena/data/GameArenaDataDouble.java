package com.games.arena.data;

import com.games.arena.GameArena;
import com.google.gson.JsonElement;

public class GameArenaDataDouble extends GameArenaDataEntry {

	private double value;

	public GameArenaDataDouble(GameArena arena,String name){
		super(arena,name);
	}

	public GameArenaDataDouble(GameArena arena,JsonElement element){
		super(arena);
		value = element.getAsDouble();
	}

	public double getValue(){
		return value;
	}

	@Override
	public void loadData(GameArenaData data){
		if(data.containsKey(this.getName())){
			GameArenaDataDouble tmp = new GameArenaDataDouble(data.getArena(),data.getElement(this.getName()));
			value = tmp.getValue();
		}
	}

	@Override
	public boolean equals(Object object){
		return false;
	}
}