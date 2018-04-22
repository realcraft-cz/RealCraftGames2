package com.paintball.drops;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.games.player.GamePlayer;
import com.paintball.Paintball;
import com.paintball.PaintballTeam.PaintballTeamType;

public class PaintballDropGlow extends PaintballDrop {

	public PaintballDropGlow(Paintball game,Location location){
		super(PaintballDropType.GLOW,game,location);
	}

	@Override
	public void activate(GamePlayer gPlayer){
		if(this.getGame().getTeams().getPlayerTeam(gPlayer).getType() == PaintballTeamType.RED){
			for(GamePlayer gPlayer2 : this.getGame().getTeams().getTeam(PaintballTeamType.BLUE).getPlayers()){
				gPlayer2.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,30*20,1),true);
			}
		} else {
			for(GamePlayer gPlayer2 : this.getGame().getTeams().getTeam(PaintballTeamType.RED).getPlayers()){
				gPlayer2.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,30*20,1),true);
			}
		}
		for(GamePlayer gPlayer2 : this.getGame().getGamePlayers()){
			gPlayer2.getPlayer().playSound(gPlayer2.getPlayer().getLocation(),Sound.ENTITY_WITCH_DRINK,1f,1f);
		}
	}

	@Override
	public void clear(){
		super.clear();
	}
}