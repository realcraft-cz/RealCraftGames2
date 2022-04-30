package com.blockparty.pickups;

import com.blockparty.BlockParty;
import com.games.player.GamePlayer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BlockPartyPickupLevitation extends BlockPartyPickup {

	public BlockPartyPickupLevitation(BlockParty game){
		super(BlockPartyPickupType.LEVITATION,game);
	}

	@Override
	public void activate(GamePlayer gPlayer){
		for(GamePlayer gPlayer2 : this.getGame().getGamePlayers()){
			gPlayer2.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 8 * 20, 1));
		}
	}

	@Override
	public void clear(){
		for(GamePlayer gPlayer2 : this.getGame().getPlayers()){
			gPlayer2.getPlayer().removePotionEffect(PotionEffectType.LEVITATION);
		}
	}

	@Override
	public void run(){
	}
}
