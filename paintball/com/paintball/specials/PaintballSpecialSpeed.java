package com.paintball.specials;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.paintball.Paintball;

import realcraft.bukkit.utils.MaterialUtil;

public class PaintballSpecialSpeed extends PaintballSpecial {

	private int duration;
	private Location location;

	public PaintballSpecialSpeed(Paintball game,int duration,Location location){
		super(PaintballSpecialType.SPEED,game);
		this.duration = duration;
		this.location = location;
	}

	@Override
	public void clear(){
	}

	@EventHandler(ignoreCancelled=true)
	public void PlayerInteractEvent(PlayerInteractEvent event){
		if(event.getAction() == Action.PHYSICAL){
			Block block = event.getClickedBlock();
			if(block != null && MaterialUtil.isPressurePlate(block.getType())
				&& block.getLocation().getBlockX() == location.getBlockX() && block.getLocation().getBlockY() == location.getBlockY() && block.getLocation().getBlockZ() == location.getBlockZ()){
				event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED,duration,2));
			}
		}
	}
}