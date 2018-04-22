package com.paintball.drops;

import org.bukkit.Location;
import org.bukkit.Sound;

import com.games.player.GamePlayer;
import com.paintball.Paintball;

public class PaintballDropAmmo extends PaintballDrop {

	public PaintballDropAmmo(Paintball game,Location location){
		super(PaintballDropType.AMMO,game,location);
	}

	@Override
	public void activate(GamePlayer gPlayer){
		gPlayer.getPlayer().getWorld().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ITEM_PICKUP,1f,1f);
		this.getGame().getUser(gPlayer).addPistols(64);
		this.getGame().setPlayerWeapons(gPlayer,false);
	}

	@Override
	public void clear(){
		super.clear();
	}
}