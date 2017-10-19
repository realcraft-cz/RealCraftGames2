package com.games.game;

public enum GameState {
	LOBBY, STARTING, INGAME, ENDING;

	public static GameState getByName(String name){
		return GameState.valueOf(name.toUpperCase());
	}

	public String toString(){
		return this.name().toLowerCase();
	}

	public boolean isLobby(){
		if(this == LOBBY || this == STARTING) return true;
		return false;
	}

	public boolean isGame(){
		if(this == INGAME || this == ENDING) return true;
		return false;
	}
}