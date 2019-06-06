package com.paintball.drops;

import com.games.player.GamePlayer;
import com.paintball.Paintball;
import org.bukkit.Location;
import org.bukkit.Sound;

public class PaintballDropGrenade extends PaintballDrop {

	public PaintballDropGrenade(Paintball game,Location location){
		super(PaintballDropType.GRENADE,game,location);
	}

	@Override
	public void activate(GamePlayer gPlayer){
		for(GamePlayer gPlayer2 : this.getGame().getTeams().getPlayerTeam(gPlayer).getPlayers()){
			gPlayer2.getPlayer().getWorld().playSound(gPlayer2.getPlayer().getLocation(),Sound.ENTITY_ITEM_PICKUP,1f,1f);
			this.getGame().getUser(gPlayer2).addGrenades(2);
			this.getGame().setPlayerWeapons(gPlayer2,false);
		}
	}

	@Override
	public void clear(){
		super.clear();
	}
}