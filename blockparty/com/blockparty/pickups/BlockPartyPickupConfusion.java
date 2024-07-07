package com.blockparty.pickups;

import com.blockparty.BlockParty;
import com.games.player.GamePlayer;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BlockPartyPickupConfusion extends BlockPartyPickup {

	public BlockPartyPickupConfusion(BlockParty game){
		super(BlockPartyPickupType.CONFUSION,game);
	}

	@Override
	public void activate(GamePlayer gPlayer){
		for(GamePlayer gPlayer2 : this.getGame().getGamePlayers()){
			gPlayer2.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 10 * 20, 1));
			gPlayer2.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.POISON, 10 * 20, 1));
			gPlayer2.getPlayer().playSound(gPlayer2.getPlayer().getLocation(), Sound.ENTITY_WITCH_DRINK, 1f, 0.5f);
		}
	}

	@Override
	public void clear(){
		for(GamePlayer gPlayer2 : this.getGame().getPlayers()){
			gPlayer2.getPlayer().removePotionEffect(PotionEffectType.NAUSEA);
			gPlayer2.getPlayer().removePotionEffect(PotionEffectType.POISON);
		}
	}

	@Override
	public void run(){
	}
}
