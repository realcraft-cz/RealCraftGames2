package com.bedwars.specials;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.TNTPrimed;

import com.bedwars.BedWars;
import com.bedwars.BedWarsTeam;
import com.games.game.GameState;
import com.games.player.GamePlayer;

import net.minecraft.server.v1_13_R2.EntityInsentient;
import net.minecraft.server.v1_13_R2.GenericAttributes;

public class BedWarsSpecialSheep extends BedWarsSpecialTeam {

	private Entity sheep;
	private Entity tnt;

	public BedWarsSpecialSheep(BedWars game,BedWarsTeam team){
		super(BedWarsSpecialType.SHEEP,game,team);
	}

	@Override
	public void activate(GamePlayer gPlayer){
		sheep = gPlayer.getPlayer().getWorld().spawnEntity(gPlayer.getPlayer().getLocation(),EntityType.SHEEP);
    	((Sheep)sheep).setAdult();
    	((Sheep)sheep).setAgeLock(true);
    	((Sheep)sheep).setInvulnerable(true);
    	((Sheep)sheep).setRemoveWhenFarAway(false);
    	((Sheep)sheep).setColor(this.getTeam().getType().getDyeColor());
    	tnt = gPlayer.getPlayer().getWorld().spawnEntity(gPlayer.getPlayer().getLocation(),EntityType.PRIMED_TNT);
    	((TNTPrimed)tnt).setFuseTicks(8*20);
    	((TNTPrimed)tnt).setIsIncendiary(false);
    	sheep.addPassenger(tnt);
		this.runTaskTimer(1,10);
	}

	@Override
	public void clear(){
		sheep.remove();
		tnt.remove();
	}

	@Override
	public void run(){
		GamePlayer gPlayer = this.getNearestEnemy(sheep.getLocation());
		if(gPlayer != null && !sheep.isDead()){
			((EntityInsentient) ((CraftEntity)sheep).getHandle()).getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(128);
			((EntityInsentient) ((CraftEntity)sheep).getHandle()).getNavigation().a(((CraftEntity)gPlayer.getPlayer()).getHandle(),2);
		}
		if(this.getGame().getState() != GameState.INGAME || sheep.isDead() || tnt.isDead()){
			this.cancelTask();
			this.clear();
		}
	}

	public GamePlayer getNearestEnemy(Location location){
		double distance = Double.MAX_VALUE;
		GamePlayer nearest = null;
		for(BedWarsTeam team : this.getGame().getTeams().getTeams()){
			if(team.getType() == this.getTeam().getType()) continue;
			for(GamePlayer gPlayer : team.getPlayers()){
				double tmpDistance = gPlayer.getPlayer().getLocation().distanceSquared(location);
				if(tmpDistance < distance){
					distance = tmpDistance;
					nearest = gPlayer;
				}
			}
		}
		return nearest;
	}
}