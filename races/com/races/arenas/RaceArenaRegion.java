package com.races.arenas;

import com.games.arena.GameArena;
import com.games.arena.GameArenaRegion;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class RaceArenaRegion extends GameArenaRegion {

	public RaceArenaRegion(GameArena arena){
		super(arena);
	}

	public boolean isEntityValidToClear(Entity entity) {
		return super.isEntityValidToClear(entity) && ((entity.getType() == EntityType.BOAT || entity.getType() == EntityType.HORSE) && entity.getPassengers().isEmpty());
	}
}