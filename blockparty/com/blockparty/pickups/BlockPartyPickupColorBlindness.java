package com.blockparty.pickups;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;

import com.blockparty.BlockParty;
import com.games.Games;
import com.games.player.GamePlayer;

import realcraft.bukkit.utils.MaterialUtil;

public class BlockPartyPickupColorBlindness extends BlockPartyPickup {

	public BlockPartyPickupColorBlindness(BlockParty game){
		super(BlockPartyPickupType.COLORBLINDNESS,game);
	}

	@Override
	public void activate(GamePlayer gPlayer){
		Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				for(GamePlayer gPlayer2 : BlockPartyPickupColorBlindness.this.getGame().getGamePlayers()){
					gPlayer2.getPlayer().playSound(gPlayer2.getPlayer().getLocation(),Sound.ENTITY_WITCH_DRINK,1f,0.5f);
					for(int y=BlockPartyPickupColorBlindness.this.getGame().getArena().getRegion().getMinLocation().getBlockY();y<=BlockPartyPickupColorBlindness.this.getGame().getArena().getRegion().getMaxLocation().getBlockY();y++){
						for(int x=BlockPartyPickupColorBlindness.this.getGame().getArena().getRegion().getMinLocation().getBlockX();x<=BlockPartyPickupColorBlindness.this.getGame().getArena().getRegion().getMaxLocation().getBlockX();x++){
							for(int z=BlockPartyPickupColorBlindness.this.getGame().getArena().getRegion().getMinLocation().getBlockZ();z<=BlockPartyPickupColorBlindness.this.getGame().getArena().getRegion().getMaxLocation().getBlockZ();z++){
								if(MaterialUtil.isTerracotta(BlockPartyPickupColorBlindness.this.getGame().getArena().getWorld().getBlockAt(x,y,z).getType())){
									gPlayer2.getPlayer().sendBlockChange(new Location(BlockPartyPickupColorBlindness.this.getGame().getArena().getWorld(),x,y,z),Bukkit.createBlockData(Material.CYAN_TERRACOTTA));
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