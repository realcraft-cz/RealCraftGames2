package com.games.events;

import com.games.game.Game;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;

public class GamePlayerStateChangeEvent extends GameEvent {

	private GamePlayer gPlayer;
	private GamePlayerState oldState;

	public GamePlayerStateChangeEvent(Game game,GamePlayer gPlayer,GamePlayerState oldState){
		super(game);
		this.gPlayer = gPlayer;
		this.oldState = oldState;
	}

	public GamePlayer getPlayer(){
		return gPlayer;
	}

	public GamePlayerState getOldState(){
		return oldState;
	}
}