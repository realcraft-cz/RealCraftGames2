package com.games.events;

import com.games.game.Game;

public class GameEndEvent extends GameEvent {

	public GameEndEvent(Game game){
		super(game);
	}
}