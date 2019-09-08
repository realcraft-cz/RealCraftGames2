package com.bedwars;

import com.games.arena.GameArena;
import com.games.arena.GameArenaRegion;
import org.bukkit.Location;
import org.bukkit.Material;

public class BedWarsArenaRegion extends GameArenaRegion {

	public BedWarsArenaRegion(GameArena arena){
		super(arena);
	}

	public void reset(boolean async){
		this.resetOuterRegion();
		super.reset(async);
	}

	private void resetOuterRegion(){
		for(int x=this.getMinLocation().getBlockX();x<=this.getMaxLocation().getBlockX();x++){
			for(int y=this.getMinLocation().getBlockY();y<=this.getMaxLocation().getBlockY();y++){
				for(int z=this.getMinLocation().getBlockZ();z<=this.getMaxLocation().getBlockZ();z++){
					Location location = new Location(this.getWorld(),x,y,z);
					if(!this.isLocationInsideClipboard(location)){
						location.getBlock().setType(Material.AIR);
					}
				}
			}
		}
	}
}