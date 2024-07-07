package com.races;

import com.games.player.GamePlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.ItemStack;
import realcraft.share.utils.RandomUtil;

public class RaceUser {

	private Races game;
	private GamePlayer gPlayer;

	private Boat boat;
	private Horse horse;

	private int rounds = 1;
	private RaceCheckpoint lastCheckpoint;

	private Location spawnLoc;

	private long lastKeyAction = 0;

	public RaceUser(Races game,GamePlayer gPlayer){
		this.game = game;
		this.gPlayer = gPlayer;
	}

	public Races getGame(){
		return game;
	}

	public GamePlayer getGamePlayer(){
		return gPlayer;
	}

	public int getRounds(){
		return rounds;
	}

	public void addRound(){
		this.rounds ++;
	}

	public RaceCheckpoint getLastCheckpoint(){
		return lastCheckpoint;
	}

	public void setLastCheckpoint(RaceCheckpoint checkpoint){
		this.lastCheckpoint = checkpoint;
	}

	public Location getSpawn(){
		return spawnLoc;
	}

	public void setSpawn(Location spawnLoc){
		this.spawnLoc = spawnLoc;
	}

	public long getLastKeyAction(){
		return lastKeyAction;
	}

	public void setLastKeyAction(){
		lastKeyAction = System.currentTimeMillis();
	}

	public void respawn(){
		if(game.getArena().getRaceType() == RaceType.BOAT) this.equipBoat();
		else if(game.getArena().getRaceType() == RaceType.HORSE) this.equipHorse();
	}

	public void clear(){
		this.clearBoat();
		this.clearHorse();
		rounds = 1;
		lastCheckpoint = null;
		lastKeyAction = System.currentTimeMillis();
	}

	public void exitVehicle(){
		this.clearBoat();
		this.clearHorse();
	}

	private void equipBoat(){
		this.clearBoat();
		boat = (Boat) gPlayer.getPlayer().getWorld().spawnEntity(gPlayer.getPlayer().getLocation(),EntityType.BOAT);
		boat.setBoatType(Boat.Type.SPRUCE);
		boat.addPassenger(gPlayer.getPlayer());
	}

	private void clearBoat(){
		if(boat != null){
			boat.eject();
			boat.remove();
			boat = null;
		}
	}

	private void equipHorse(){
		this.clearHorse();
		horse = (Horse) gPlayer.getPlayer().getWorld().spawnEntity(gPlayer.getPlayer().getLocation(),EntityType.HORSE);
		horse.setAdult();
		horse.setAI(false);
		horse.setTamed(true);
		horse.setJumpStrength(0.8);
		horse.setInvulnerable(true);
		horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
		horse.setColor(Horse.Color.values()[RandomUtil.getRandomInteger(0,Horse.Color.values().length-1)]);
		horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.4d);
		horse.addPassenger(gPlayer.getPlayer());
	}

	private void clearHorse(){
		if(horse != null){
			horse.eject();
			horse.remove();
			horse = null;
		}
	}

	public double getPosition(){
		double distance = 1;
		if(game.getWinner(gPlayer) == null){
			if(this.getLastCheckpoint() != null){
				distance = this.getLastCheckpoint().getIndex()+2;
				distance -= this.getLastCheckpoint().distance(this.getLastCheckpoint().getNextCheckpoint(),gPlayer.getPlayer().getLocation());
			} else {
				distance -= game.getArena().getCheckpoints().get(0).distance(this.getSpawn(),gPlayer.getPlayer().getLocation());
			}
		}
		else distance = 1000-game.getWinner(gPlayer).getPosition();
		return distance+(rounds*game.getArena().getRounds());
	}
}