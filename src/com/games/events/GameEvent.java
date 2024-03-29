package com.games.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.games.game.Game;

public abstract class GameEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private Game game;
	private boolean cancelled = false;

	public GameEvent(Game game){
		this.game = game;
	}

	public Game getGame(){
		return game;
	}

	@Override
	public boolean isCancelled(){
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel){
		cancelled = cancel;
	}

	@Override
	public HandlerList getHandlers(){
		return handlers;
	}

	public static HandlerList getHandlerList(){
		return handlers;
	}
}