package com.bedwars.specials;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Stray;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.bedwars.BedWars;
import com.bedwars.BedWarsTeam;
import com.games.game.GameState;
import com.games.player.GamePlayer;

public class BedWarsSpecialStray extends BedWarsSpecialTeam {

	private Entity stray;

	public BedWarsSpecialStray(BedWars game,BedWarsTeam team){
		super(BedWarsSpecialType.STRAY,game,team);
	}
	@Override
	public void activate(GamePlayer gPlayer){
		stray = gPlayer.getPlayer().getWorld().spawnEntity(gPlayer.getPlayer().getLocation(),EntityType.STRAY);
    	((Stray)stray).setRemoveWhenFarAway(false);
    	((Stray)stray).getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
    	((Stray)stray).getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(128);
		ItemStack item = new ItemStack(Material.LEATHER_HELMET,1);
		LeatherArmorMeta meta = (LeatherArmorMeta)item.getItemMeta();
        meta.setColor(this.getTeam().getType().getColor());
        item.setItemMeta(meta);
        ((Stray)stray).getEquipment().setHelmet(item);
		this.runTaskTimer(1,20);
	}

	@Override
	public void clear(){
		stray.remove();
	}

	@Override
	public void run(){
		GamePlayer gPlayer = this.getNearestEnemy(stray.getLocation());
		if(gPlayer != null && !stray.isDead()){
			((Stray)stray).setTarget(gPlayer.getPlayer());
		}
		if(this.getGame().getState() != GameState.INGAME || stray.isDead()){
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