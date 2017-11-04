package com.games.events;

import com.games.game.Game;
import com.games.player.GamePlayer;

public class GamePlayerLeaveEvent extends GameEvent {

	private GamePlayer gPlayer;

	public GamePlayerLeaveEvent(Game game,GamePlayer gPlayer){
		super(game);
		this.gPlayer = gPlayer;
	}

	public GamePlayer getPlayer(){
		return gPlayer;
	}
}