package com.paintball.specials;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.games.Games;
import com.paintball.Paintball;

public abstract class PaintballSpecial implements Listener {

	private PaintballSpecialType type;
	private Paintball game;

	public PaintballSpecial(PaintballSpecialType type,Paintball game){
		this.type = type;
		this.game = game;
	}

	public Paintball getGame(){
		return game;
	}

	public PaintballSpecialType getType(){
		return type;
	}

	public void setEnabled(boolean enabled){
		if(enabled){
			Bukkit.getPluginManager().registerEvents(this,Games.getInstance());
		} else {
			HandlerList.unregisterAll(this);
			this.clear();
		}
	}

	public abstract void clear();

	public enum PaintballSpecialType {
		JUMP, SPEED, MACHINEGUN, GRENADE;
	}
}