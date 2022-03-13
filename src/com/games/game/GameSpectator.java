package com.games.game;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.games.Games;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.SkinUtil;
import com.games.utils.SkinUtil.Skin;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
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
import realcraft.bukkit.utils.ItemUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class GameSpectator implements Listener {

	private Game game;
	private ArrayList<SpectatorHotbarItem> hotbarItems;
	private Inventory inventory;
	private HashMap<Integer,SpectatorMenuItem> items = new HashMap<Integer,SpectatorMenuItem>();
	private static final String INV_NAME = "Spectator";

	public GameSpectator(Game game){
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this,Games.getInstance());
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Games.getInstance(),PacketType.Play.Client.USE_ENTITY,PacketType.Play.Server.PLAYER_INFO){

			@Override
			public void onPacketReceiving(PacketEvent event){
				if(event.getPacketType() == PacketType.Play.Client.USE_ENTITY){
					GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
					if(gPlayer != null && gPlayer.getState() == GamePlayerState.SPECTATOR){
						event.setCancelled(true);
					}
				}
			}

			@Override
			public void onPacketSending(PacketEvent event){
				if(event.getPacketType() == PacketType.Play.Server.PLAYER_INFO){
					try {
						UUID uuid = event.getPacket().getPlayerInfoDataLists().read(0).get(0).getProfile().getUUID();
						Player player = Bukkit.getPlayer(uuid);
						if(player != null && player.isOnline() && event.getPlayer().getUniqueId() != uuid){
							GamePlayer gPlayer = game.getGamePlayer(player);
							if(gPlayer != null && gPlayer.getState() == GamePlayerState.SPECTATOR && !gPlayer.isLeaving()){
								PacketPlayOutPlayerInfo packet = (PacketPlayOutPlayerInfo) event.getPacket().getHandle();
								PacketPlayOutPlayerInfo.EnumPlayerInfoAction action = (PacketPlayOutPlayerInfo.EnumPlayerInfoAction) realcraft.bukkit.utils.ReflectionUtils.getField(packet.getClass(),true,"a").get(packet);
								if(action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e){
									event.setCancelled(true);
								}
							}
						}
					} catch (Exception e){
						e.printStackTrace();
					}
				}
			}
		});
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

	public ArrayList<SpectatorHotbarItem> getItems(){
		if(hotbarItems == null){
			hotbarItems = new ArrayList<SpectatorHotbarItem>();
			if(GameFlag.SPECTATOR) hotbarItems.add(new SpectatorHotbarItem(0,"§f§l"+INV_NAME,Material.COMPASS));
			hotbarItems.add(new SpectatorHotbarItem(8,"§e§lOpustit hru",Material.SLIME_BALL));
		}
		return hotbarItems;
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
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		if(gPlayer.getState() == GamePlayerState.SPECTATOR){
			if(gPlayer.getPlayer().getInventory().getItemInMainHand().getType() == Material.COMPASS && (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))){
				this.open(gPlayer);
				event.setCancelled(true);
			}
			else if(gPlayer.getPlayer().getInventory().getItemInMainHand().getType() == Material.SLIME_BALL && (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))){
				game.leavePlayer(gPlayer);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled=false)
	public void InventoryClickEvent(InventoryClickEvent event){
		if(event.getWhoClicked() instanceof Player){
			GamePlayer gPlayer = game.getGamePlayer((Player)event.getWhoClicked());
			if(event.getView().getTitle().equalsIgnoreCase(INV_NAME)){
				if(items.containsKey(event.getRawSlot())){
					items.get(event.getRawSlot()).onPlayerClick(gPlayer);
					event.setCancelled(true);
				}
			}
		}
	}

	public static class SpectatorHotbarItem {

		private int index;
		private ItemStack item;

		public SpectatorHotbarItem(int index,String name,Material type){
			this.index = index;
			item = new ItemStack(type);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(name);
			item.setItemMeta(meta);
		}

		public int getIndex(){
			return index;
		}

		public ItemStack getItemStack(){
			return item;
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

		public SpectatorMenuItemPlayer(int index,String name,GamePlayer gPlayer){
			super(SpectatorMenuItemType.PLAYER,index);
			this.gPlayer = gPlayer;
			Skin skin = new Skin("steve","","","");
			String skinName = SkinUtil.getPlayerSkin(gPlayer.getPlayer().getName());
			if(skinName != null){
				skin = SkinUtil.getSkin(skinName);
				if(skin == null) skin = new Skin("steve","","","");
			}
			ItemStack item = ItemUtil.getHead(skin.getValue());
			SkullMeta meta = (SkullMeta) item.getItemMeta();
			meta.setDisplayName(name);
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