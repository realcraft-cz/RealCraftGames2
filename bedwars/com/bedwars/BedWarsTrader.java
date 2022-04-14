package com.bedwars;

import com.games.Games;
import com.games.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

public class BedWarsTrader implements Runnable {

	private final BedWarsArena arena;
	private final Location location;
	private Villager entity;

	public BedWarsTrader(BedWarsArena arena,Location location){
		this.arena = arena;
		this.location = location;
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Games.getInstance(),this,2*20,2*20);
	}

	@Override
	public void run(){
		if(arena.getGame().getState() == GameState.INGAME && arena.getGame().getArena() == arena){
			this._spawn();
		} else {
			this._remove();
		}
	}

	private void _spawn() {
		if (entity != null && !entity.isDead()) {
			return;
		}

		entity = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER, false);
		entity.setAI(false);
		entity.setRemoveWhenFarAway(false);
		entity.setInvulnerable(true);
		entity.setPersistent(false);
		entity.setCustomNameVisible(true);
		entity.setCustomName("§lObchod");
		entity.teleport(location);
	}

	private void _remove() {
		if (entity == null || entity.isDead()) {
			return;
		}

		entity.remove();
	}
}
