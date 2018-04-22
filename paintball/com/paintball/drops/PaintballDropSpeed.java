package com.paintball.drops;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.games.player.GamePlayer;
import com.paintball.Paintball;

public class PaintballDropSpeed extends PaintballDrop {

	public PaintballDropSpeed(Paintball game,Location location){
		super(PaintballDropType.SPEED,game,location);
	}

	@Override
	public void activate(GamePlayer gPlayer){
		gPlayer.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED,30*20,2),true);
		gPlayer.getPlayer().getWorld().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_BLAZE_AMBIENT,1f,1f);
	}

	@Override
	public void clear(){
		super.clear();
	}
}