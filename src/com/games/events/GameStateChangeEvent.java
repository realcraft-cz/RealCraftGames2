package com.games.events;

import com.games.game.Game;

public class GameStateChangeEvent extends GameEvent {

	public GameStateChangeEvent(Game game){
		super(game);
	}
}