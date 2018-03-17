package com.blockparty.pickups;

import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.blockparty.BlockParty;
import com.games.player.GamePlayer;

public class BlockPartyPickupBlindness extends BlockPartyPickup {

	public BlockPartyPickupBlindness(BlockParty game){
		super(BlockPartyPickupType.BLINDNESS,game);
	}

	@Override
	public void activate(GamePlayer gPlayer){
		for(GamePlayer gPlayer2 : this.getGame().getGamePlayers()){
			gPlayer2.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,10*20,1),true);
			gPlayer2.getPlayer().playSound(gPlayer2.getPlayer().getLocation(),Sound.ENTITY_WITCH_DRINK,1f,0.5f);
		}
	}

	@Override
	public void clear(){
		for(GamePlayer gPlayer2 : this.getGame().getPlayers()){
			gPlayer2.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
		}
	}

	@Override
	public void run(){
	}
}