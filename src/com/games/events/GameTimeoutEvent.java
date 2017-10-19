package com.games.events;

import com.games.game.Game;

public class GameTimeoutEvent extends GameEvent {

	public GameTimeoutEvent(Game game){
		super(game);
	}
}