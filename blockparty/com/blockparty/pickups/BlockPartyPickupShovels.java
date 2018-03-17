package com.blockparty.pickups;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.blockparty.BlockParty;
import com.games.player.GamePlayer;
import com.games.utils.Particles;

import realcraft.bukkit.RealCraft;

public class BlockPartyPickupShovels extends BlockPartyPickup {

	private static final int COUNT = 10;
	private ArrayList<Item> items = new ArrayList<Item>();

	public BlockPartyPickupShovels(BlockParty game){
		super(BlockPartyPickupType.SHOVELS,game);
	}

	@Override
	public void activate(GamePlayer gPlayer){
		Particles.FIREWORKS_SPARK.display(0.3f,0.3f,0.3f,0.2f,32,this.getLocation().clone().add(0.5,0.5,0.5),64);
		Random random = new Random();
		ItemStack itemStack = new ItemStack(Material.GOLD_SPADE,1);
		ItemMeta meta = itemStack.getItemMeta();
		for(int i=0;i<COUNT;i++){
			Bukkit.getScheduler().runTaskLater(RealCraft.getInstance(),new Runnable(){
				@Override
				public void run(){
					BlockPartyPickupShovels.this.getLocation().getWorld().playSound(BlockPartyPickupShovels.this.getLocation(),Sound.ENTITY_ITEM_PICKUP,1f,1f);
					meta.setDisplayName(UUID.randomUUID().toString());
			    	itemStack.setItemMeta(meta);
					Item item = BlockPartyPickupShovels.this.getLocation().getWorld().dropItem(BlockPartyPickupShovels.this.getLocation(),itemStack);
					item.setPickupDelay(20);
					item.setVelocity(new Vector(random.nextDouble()-0.5,random.nextDouble()/4,random.nextDouble()-0.5).add(new Vector(0,0.4,0)));
					items.add(item);
				}
			},i*2);
		}
	}

	@Override
	public void clear(){
		for(GamePlayer gPlayer2 : this.getGame().getPlayers()){
			gPlayer2.getPlayer().getInventory().remove(Material.GOLD_SPADE);
		}
		for(Item item : items) item.remove();
		items.clear();
	}

	@Override
	public void run(){
	}
}