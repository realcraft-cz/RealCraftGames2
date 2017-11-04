package com.bedwars;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import com.games.Games;
import com.games.game.GameState;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.npc.SimpleNPCDataStore;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.trait.LookClose;

public class BedWarsTrader implements Runnable {

	private static NPCRegistry npcRegistry;

	private BedWarsArena arena;
	private Location location;
	private NPC npc;

	public BedWarsTrader(BedWarsArena arena,Location location){
		this.arena = arena;
		this.location = location;
		if(npcRegistry == null) npcRegistry = CitizensAPI.createAnonymousNPCRegistry(SimpleNPCDataStore.create(new YamlStorage(new File(Games.getInstance().getDataFolder()+"/citizens.tmp.yml"))));

		npc = npcRegistry.createNPC(EntityType.VILLAGER,"§lObchod");
		npc.setProtected(true);
		npc.getTrait(LookClose.class).setRealisticLooking(true);
		npc.getTrait(LookClose.class).toggle();
		npc.spawn(location);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Games.getInstance(),this,2*20,2*20);
	}

	@Override
	public void run(){
		if(arena.getGame().getState() == GameState.INGAME && arena.getGame().getArena() == arena){
			npc.spawn(location);
		} else {
			npc.despawn(DespawnReason.PLUGIN);
		}
	}
}