package com.games.arena.data;

import com.games.arena.GameArena;

public abstract class GameArenaDataEntry {

	private GameArena arena;
	private String name;

	public GameArenaDataEntry(GameArena arena){
		this(arena,null);
	}

	public GameArenaDataEntry(GameArena arena,String name){
		this.arena = arena;
		this.name = name;
	}

	public GameArena getArena(){
		return arena;
	}

	public String getName(){
		return name;
	}

	public abstract void loadData(GameArenaData data);
	public abstract boolean equals(Object object);
}