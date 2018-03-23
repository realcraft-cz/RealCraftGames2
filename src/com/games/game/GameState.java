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
		return (this == LOBBY || this == STARTING);
	}

	public boolean isGame(){
		return (this == INGAME || this == ENDING);
	}
}