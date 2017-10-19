package com.games.events;

import com.games.game.Game;

public class GameStartEvent extends GameEvent {

	public GameStartEvent(Game game){
		super(game);
	}
}