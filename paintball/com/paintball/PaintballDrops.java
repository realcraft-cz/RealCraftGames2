package com.paintball;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;

import com.games.utils.RandomUtil;
import com.paintball.drops.PaintballDrop;
import com.paintball.drops.PaintballDrop.PaintballDropType;
import com.paintball.drops.PaintballDropAmmo;
import com.paintball.drops.PaintballDropGlow;
import com.paintball.drops.PaintballDropGrenade;
import com.paintball.drops.PaintballDropSpeed;

public class PaintballDrops {

	private Paintball game;

	private HashMap<Location,PaintballDrop> drops = new HashMap<Location,PaintballDrop>();

	public PaintballDrops(Paintball game){
		this.game = game;
	}

	public Paintball getGame(){
		return game;
	}

	public ArrayList<PaintballDrop> getDrops(){
		return new ArrayList<PaintballDrop>(drops.values());
	}

	public void addDrop(){
		PaintballDrop drop = this.getRandomDrop();
		if(drop != null){
			drops.put(drop.getLocation(),drop);
			drop.drop();
		}
	}

	public void removeDrop(PaintballDrop drop){
		drops.remove(drop.getLocation());
	}

	public PaintballDrop getRandomDrop(){
		if(game.getArena().getDropLocations().isEmpty()) return null;
		Location location = this.getRandomDropLocation();
		if(location != null){
			switch(PaintballDropType.getRandomType()){
				case GLOW: return new PaintballDropGlow(game,location);
				case SPEED: return new PaintballDropSpeed(game,location);
				case GRENADE: return new PaintballDropGrenade(game,location);
				case AMMO: return new PaintballDropAmmo(game,location);
			}
		}
		return null;
	}

	public Location getRandomDropLocation(){
		return this.getRandomDropLocation(1);
	}

	public Location getRandomDropLocation(int step){
		if(step > 100) return null;
		Location location = game.getArena().getDropLocations().get(RandomUtil.getRandomInteger(0,game.getArena().getDropLocations().size()-1));
		for(PaintballDrop drop : this.getDrops()){
			if(drop.getLocation().equals(location)) return this.getRandomDropLocation(step+1);
		}
		return location.clone();
	}

	public void clear(){
		for(PaintballDrop drop : this.getDrops()){
			drop.clear();
		}
		drops.clear();
	}
}