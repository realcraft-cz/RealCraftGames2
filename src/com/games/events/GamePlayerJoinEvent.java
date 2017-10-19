package com.games.events;

import com.games.game.Game;
import com.games.player.GamePlayer;

public class GamePlayerJoinEvent extends GameEvent {

	private GamePlayer gPlayer;

	public GamePlayerJoinEvent(Game game,GamePlayer gPlayer){
		super(game);
		this.gPlayer = gPlayer;
	}

	public GamePlayer getPlayer(){
		return gPlayer;
	}
}