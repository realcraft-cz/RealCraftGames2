package com.bedwars;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.games.Games;
import com.games.game.GameState;

public class BedWarsResource implements Runnable {

	private BedWars game;
	private BedWarsArena arena;
	private BedWarsResourceType type;
	private Location location;

	public BedWarsResource(BedWars game,BedWarsArena arena,BedWarsResourceType type,Location location){
		this.game = game;
		this.arena = arena;
		this.type = type;
		this.location = location;
		this.location.add(0.5,0.5,0.5);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Games.getInstance(),this,20,type.toRepeatDelay());
	}

	public BedWars getGame(){
		return game;
	}

	public BedWarsResourceType getType(){
		return type;
	}

	@Override
	public void run(){
		if(game.getState() == GameState.INGAME && game.getArena() == arena){
			Item item = location.getWorld().dropItemNaturally(location,type.toItemStack());
			item.setPickupDelay(0);
		}
	}

	public enum BedWarsResourceType {
		BRONZE, IRON, GOLD;

		public String toString(){
			return this.name().toLowerCase();
		}

		public static BedWarsResourceType getByName(String name){
			return BedWarsResourceType.valueOf(name.toUpperCase());
		}

		public String toItemName(){
			switch(this){
				case BRONZE: return "§4Bronze";
				case IRON: return "§7Iron";
				case GOLD: return "§6Gold";
			}
			return null;
		}

		public ItemStack toItemStack(){
			ItemStack itemStack = new ItemStack(this.toMaterial(),1);
			ItemMeta meta = itemStack.getItemMeta();
			meta.setDisplayName(this.toItemName());
			itemStack.setItemMeta(meta);
			return itemStack;
		}

		public Material toMaterial(){
			switch(this){
				case BRONZE: return Material.BRICK;
				case IRON: return Material.IRON_INGOT;
				case GOLD: return Material.GOLD_INGOT;
			}
			return null;
		}

		public int toRepeatDelay(){
			switch(this){
				case BRONZE: return 20;
				case IRON: return 200;
				case GOLD: return 400;
			}
			return 20;
		}
	}
}