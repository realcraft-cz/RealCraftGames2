package com.games.events;

import com.games.arena.GameArena;
import com.games.game.Game;

public class GameRegionLoadEvent extends GameEvent {

	private GameArena arena;

	public GameRegionLoadEvent(Game game,GameArena arena){
		super(game);
		this.arena = arena;
	}

	public GameArena getArena(){
		return arena;
	}
}