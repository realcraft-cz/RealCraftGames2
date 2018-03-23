package com.bedwars.specials;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import com.bedwars.BedWars;
import com.games.Games;
import com.games.player.GamePlayer;

public abstract class BedWarsSpecial implements Runnable {

	private BedWarsSpecialType type;
	private BedWars game;
	private BukkitTask task;

	public BedWarsSpecial(BedWarsSpecialType type,BedWars game){
		this.type = type;
		this.game = game;
	}

	public BedWars getGame(){
		return game;
	}

	public BedWarsSpecialType getType(){
		return type;
	}

	public abstract void activate(GamePlayer gPlayer);
	public abstract void clear();

	public void runTaskLater(long delay){
		this.cancelTask();
		task = Bukkit.getScheduler().runTaskLater(Games.getInstance(),this,delay);
	}

	public void runTaskTimer(long delay,long period){
		this.cancelTask();
		task = Bukkit.getScheduler().runTaskTimer(Games.getInstance(),this,delay,period);
	}

	public void cancelTask(){
		if(task != null) task.cancel();
	}

	public enum BedWarsSpecialType {
		SHEEP, STRAY;
	}
}