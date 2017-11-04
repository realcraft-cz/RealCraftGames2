package com.dominate.skills;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import com.dominate.Dominate;
import com.dominate.DominateUser;
import com.dominate.DominateUtils;
import com.games.Games;

public class DominateSkillFrostWalk extends DominateSkill {

	public DominateSkillFrostWalk(Dominate game,DominateUser dPlayer){
		super(DominateSkillType.FROSTWALK,game,dPlayer);
	}

	@Override
	public void activate(Entity none){
		if(this.getPlayer().getVelocity().getY() > -0.6){
			Location location = this.getPlayer().getLocation().add(this.getPlayer().getLocation().getDirection().setY(0).normalize().multiply(1.0)).add(0,-1,0);
			ArrayList<Location> blocks = new ArrayList<Location>();
			blocks.addAll(DominateUtils.makeCylinder(location,Material.ICE,3));
			blocks.addAll(DominateUtils.makeCylinder(location.add(0,-1,0),Material.ICE,3));
			for(int i=0;i<blocks.size();i++){
				final int index = i;
				Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
					@Override
					public void run(){
						blocks.get(index).getBlock().setType(Material.WATER);
					}
				},20+i);
			}
		}
	}

	@Override
	public void clear(){
	}

	@Override
	public void run(){
	}

	@Override
	public void recharged(){
	}

	@EventHandler
	public void PlayerMoveEvent(PlayerMoveEvent event){
		if(event.getPlayer().equals(this.getPlayer())){
			this.trigger();
		}
	}

	@Override
	public void updateInventory(){
	}
}