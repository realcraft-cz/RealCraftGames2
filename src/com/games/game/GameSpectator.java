package com.games.game;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.games.Games;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;

public class GameSpectator implements Listener {

	private Game game;
	private ItemStack hotbarItem;
	private Inventory inventory;
	private HashMap<Integer,SpectatorMenuItem> items = new HashMap<Integer,SpectatorMenuItem>();
	private static final String INV_NAME = "Spectator";

	public GameSpectator(Game game){
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this,Games.getInstance());
	}

	public Game getGame(){
		return game;
	}

	public Inventory getInventory(){
		if(inventory == null){
			inventory = Bukkit.createInventory(null,4*9,INV_NAME);
		}
		return inventory;
	}

	public ItemStack getItem(){
		if(hotbarItem == null){
			hotbarItem = new ItemStack(Material.COMPASS);
			ItemMeta meta = hotbarItem.getItemMeta();
			meta.setDisplayName("§f§l"+INV_NAME);
			hotbarItem.setItemMeta(meta);
		}
		return hotbarItem;
	}

	public void update(){
		inventory.clear();
		items = game.getSpectatorMenuItems();
		for(SpectatorMenuItem item : items.values()){
			inventory.setItem(item.getIndex(),item.getItemStack());
		}
	}

	private void open(GamePlayer gPlayer){
		gPlayer.getPlayer().openInventory(this.getInventory());
		this.update();
	}

	@EventHandler(ignoreCancelled=false)
	public void PlayerInteractEvent(PlayerInteractEvent event){
		if(GameFlag.SPECTATOR == false) return;
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		if(gPlayer.getState() == GamePlayerState.SPECTATOR){
			if(gPlayer.getPlayer().getInventory().getItemInMainHand().getType() == Material.COMPASS && (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))){
				this.open(gPlayer);
			}
		}
	}

	@EventHandler(ignoreCancelled=false)
	public void InventoryClickEvent(InventoryClickEvent event){
		if(GameFlag.SPECTATOR == false) return;
		if(event.getWhoClicked() instanceof Player){
			GamePlayer gPlayer = game.getGamePlayer((Player)event.getWhoClicked());
			if(event.getInventory().getName().equalsIgnoreCase(INV_NAME)){
				if(items.containsKey(event.getRawSlot())){
					items.get(event.getRawSlot()).onPlayerClick(gPlayer);
				}
			}
		}
	}

	public static abstract class SpectatorMenuItem {

		private SpectatorMenuItemType type;
		private int index;
		private ItemStack item;

		public SpectatorMenuItem(SpectatorMenuItemType type,int index){
			this.type = type;
			this.index = index;
		}

		public int getIndex(){
			return index;
		}

		public SpectatorMenuItemType getType(){
			return type;
		}

		public void setItemStack(ItemStack item){
			this.item = item;
		}

		public ItemStack getItemStack(){
			return item;
		}

		public abstract void onPlayerClick(GamePlayer gPlayer);
	}

	public static class SpectatorMenuItemPlayer extends SpectatorMenuItem {

		private GamePlayer gPlayer;

		@SuppressWarnings("deprecation")
		public SpectatorMenuItemPlayer(int index,String name,GamePlayer gPlayer){
			super(SpectatorMenuItemType.PLAYER,index);
			this.gPlayer = gPlayer;
			ItemStack item = new ItemStack(Material.SKULL_ITEM,1,(short)3);
			SkullMeta meta = (SkullMeta) item.getItemMeta();
			meta.setDisplayName(name);
			meta.setOwner(gPlayer.getPlayer().getName());
			item.setItemMeta(meta);
			this.setItemStack(item);
		}

		public GamePlayer getPlayer(){
			return gPlayer;
		}

		public void onPlayerClick(GamePlayer gPlayer){
			gPlayer.getPlayer().closeInventory();
			if(this.getPlayer().getPlayer().isOnline() && this.getPlayer().getState() == GamePlayerState.DEFAULT){
				gPlayer.getPlayer().teleport(this.getPlayer().getPlayer().getLocation());
				gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.UI_BUTTON_CLICK,1f,1f);
			}
		}
	}

	public static class SpectatorMenuItemLocation extends SpectatorMenuItem {

		private Location location;

		public SpectatorMenuItemLocation(int index,ItemStack item,Location location){
			super(SpectatorMenuItemType.LOCATION,index);
			this.location = location;
			this.setItemStack(item);
		}

		public Location getLocation(){
			return location;
		}

		public void onPlayerClick(GamePlayer gPlayer){
			gPlayer.getPlayer().closeInventory();
			gPlayer.getPlayer().teleport(this.getLocation());
			gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.UI_BUTTON_CLICK,1f,1f);
		}
	}

	public enum SpectatorMenuItemType {
		PLAYER, LOCATION;
	}
}