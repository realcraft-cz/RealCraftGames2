package com.blockparty.pickups;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.blockparty.BlockParty;
import com.games.player.GamePlayer;

public class BlockPartyPickupJump extends BlockPartyPickup {

	private GamePlayer gPlayer;

	public BlockPartyPickupJump(BlockParty game){
		super(BlockPartyPickupType.JUMP,game);
	}

	@Override
	public void activate(GamePlayer gPlayer){
		this.gPlayer = gPlayer;
		gPlayer.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP,10*20,2),true);
	}

	@Override
	public void clear(){
		if(gPlayer != null) gPlayer.getPlayer().removePotionEffect(PotionEffectType.JUMP);
	}

	@Override
	public void run(){
	}
}