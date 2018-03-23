package com.blockparty.pickups;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;

import com.blockparty.BlockParty;
import com.games.Games;
import com.games.player.GamePlayer;

public class BlockPartyPickupColorBlindness extends BlockPartyPickup {

	public BlockPartyPickupColorBlindness(BlockParty game){
		super(BlockPartyPickupType.COLORBLINDNESS,game);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void activate(GamePlayer gPlayer){
		Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				for(GamePlayer gPlayer2 : BlockPartyPickupColorBlindness.this.getGame().getGamePlayers()){
					gPlayer2.getPlayer().playSound(gPlayer2.getPlayer().getLocation(),Sound.ENTITY_WITCH_DRINK,1f,0.5f);
					for(int y=BlockPartyPickupColorBlindness.this.getGame().getArena().getLocMin().getBlockY();y<=BlockPartyPickupColorBlindness.this.getGame().getArena().getLocMax().getBlockY();y++){
						for(int x=BlockPartyPickupColorBlindness.this.getGame().getArena().getLocMin().getBlockX();x<=BlockPartyPickupColorBlindness.this.getGame().getArena().getLocMax().getBlockX();x++){
							for(int z=BlockPartyPickupColorBlindness.this.getGame().getArena().getLocMin().getBlockZ();z<=BlockPartyPickupColorBlindness.this.getGame().getArena().getLocMax().getBlockZ();z++){
								if(BlockPartyPickupColorBlindness.this.getGame().getArena().getWorld().getBlockAt(x,y,z).getType() == Material.STAINED_CLAY){
									gPlayer2.getPlayer().sendBlockChange(new Location(BlockPartyPickupColorBlindness.this.getGame().getArena().getWorld(),x,y,z),Material.STAINED_CLAY,(byte)9);
								}
							}
						}
					}
				}
			}
		},10);
	}

	@Override
	public void clear(){
	}

	@Override
	public void run(){
	}
}