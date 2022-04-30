package com.blockparty.pickups;

import com.blockparty.BlockParty;
import com.games.player.GamePlayer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class BlockPartyPickupPumpkin extends BlockPartyPickup {

	public BlockPartyPickupPumpkin(BlockParty game){
		super(BlockPartyPickupType.PUMPKIN,game);
	}

	@Override
	public void activate(GamePlayer gPlayer){
		ItemStack item = new ItemStack(Material.CARVED_PUMPKIN);
		for(GamePlayer gPlayer2 : this.getGame().getGamePlayers()){
			gPlayer2.getPlayer().getInventory().setItem(EquipmentSlot.HEAD, item);
			gPlayer2.getPlayer().playSound(gPlayer2.getPlayer().getLocation(),Sound.ENTITY_WITCH_DRINK,1f,1f);
		}
	}

	@Override
	public void clear(){
		for(GamePlayer gPlayer2 : this.getGame().getPlayers()){
			gPlayer2.getPlayer().getInventory().setItem(EquipmentSlot.HEAD, null);
		}
	}

	@Override
	public void run(){
	}
}