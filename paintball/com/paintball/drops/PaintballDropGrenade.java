package com.paintball.drops;

import org.bukkit.Location;
import org.bukkit.Sound;

import com.games.player.GamePlayer;
import com.paintball.Paintball;

public class PaintballDropGrenade extends PaintballDrop {

	public PaintballDropGrenade(Paintball game,Location location){
		super(PaintballDropType.GRENADE,game,location);
	}

	@Override
	public void activate(GamePlayer gPlayer){
		gPlayer.getPlayer().getWorld().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ITEM_PICKUP,1f,1f);
		this.getGame().getUser(gPlayer).addGrenades(2);
		this.getGame().setPlayerWeapons(gPlayer,false);
	}

	@Override
	public void clear(){
		super.clear();
	}
}