package com.bedwars;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.bedwars.BedWarsResource.BedWarsResourceType;
import com.games.player.GamePlayer;
import com.games.utils.Glow;
import com.games.utils.ItemUtil;

public class BedWarsShop {

	private BedWars game;

	private HashMap<BedWarsCategoryType,BedWarsCategory> categories = new HashMap<BedWarsCategoryType,BedWarsCategory>();
	private HashMap<GamePlayer,BedWarsCategoryType> playerMenuType = new HashMap<GamePlayer,BedWarsCategoryType>();

	public BedWarsShop(BedWars game){
		this.game = game;
		categories.put(BedWarsCategoryType.ARMOR,new BedWarsCategory(BedWarsCategoryType.ARMOR));
		categories.put(BedWarsCategoryType.SWORDS,new BedWarsCategory(BedWarsCategoryType.SWORDS));
		categories.put(BedWarsCategoryType.BOWS,new BedWarsCategory(BedWarsCategoryType.BOWS));
		categories.put(BedWarsCategoryType.FOOD,new BedWarsCategory(BedWarsCategoryType.FOOD));
		categories.put(BedWarsCategoryType.TOOLS,new BedWarsCategory(BedWarsCategoryType.TOOLS));
		categories.put(BedWarsCategoryType.BLOCKS,new BedWarsCategory(BedWarsCategoryType.BLOCKS));
		categories.put(BedWarsCategoryType.SPECIAL,new BedWarsCategory(BedWarsCategoryType.SPECIAL));
	}

	public BedWars getGame(){
		return game;
	}

	public void open(GamePlayer gPlayer){
		Inventory menu = Bukkit.createInventory(null,2*9,"Obchod");
		ItemStack[] items = BedWarsCategoryType.getItems();
		int i=0;
		for(ItemStack item : items){
			menu.setItem(i++,item);
		}
		playerMenuType.remove(gPlayer);
		gPlayer.getPlayer().openInventory(menu);
	}

	public void openCategory(GamePlayer gPlayer,BedWarsCategoryType type,int index){
		Inventory menu = Bukkit.createInventory(null,2*9,"Obchod");
		ItemStack[] items = BedWarsCategoryType.getItems();
		int i=0;
		for(ItemStack item : items){
			if(i == index){
				item = item.clone();
				ItemMeta meta = item.getItemMeta();
				meta.addEnchant(new Glow(255),10,true);
				item.setItemMeta(meta);
			}
			menu.setItem(i++,item);
		}
		ArrayList<BedWarsCategoryItem> catItems = categories.get(type).getItems();
		i=9;
		for(BedWarsCategoryItem item : catItems){
			ItemStack itemStack = item.getItemStack();
			if(itemStack.getType() == Material.LEATHER_HELMET || itemStack.getType() == Material.LEATHER_CHESTPLATE || itemStack.getType() == Material.LEATHER_LEGGINGS || itemStack.getType() == Material.LEATHER_BOOTS){
				itemStack = item.getItemStack().clone();
				LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
		        meta.setColor(game.getTeams().getPlayerTeam(gPlayer).getType().getColor());
		        itemStack.setItemMeta(meta);
			}
			menu.setItem(i++,itemStack);
		}
		playerMenuType.put(gPlayer,type);
		gPlayer.getPlayer().openInventory(menu);
	}

	public void close(GamePlayer gPlayer){
		playerMenuType.remove(gPlayer);
		gPlayer.getPlayer().closeInventory();
	}

	public void onPlayerClick(InventoryClickEvent event){
		Player player = (Player) event.getWhoClicked();
		GamePlayer gPlayer = game.getGamePlayer(player);
		ItemStack item = event.getCurrentItem();
		if(event.getClickedInventory() != null){
			if(event.getClickedInventory().getType() == InventoryType.CHEST) event.setCancelled(true);
			else if(event.getClickedInventory().getType() == InventoryType.PLAYER && event.isShiftClick()) event.setCancelled(true);
		}
		if(item != null && item.getType() != Material.AIR){
			if(event.getRawSlot() < 9){
				player.playSound(player.getLocation(),Sound.UI_BUTTON_CLICK,1f,1f);
				BedWarsCategoryType type = BedWarsCategoryType.getTypeByItemStack(item);
				this.openCategory(gPlayer,type,event.getRawSlot());
			} else {
				BedWarsCategoryType type = playerMenuType.get(gPlayer);
				if(type != null){
					BedWarsCategoryItem shopItem = categories.get(type).getItemByIndex(event.getRawSlot()-9);
					if(shopItem != null){
						if(event.isShiftClick()) shopItem.buyAll(gPlayer);
						else shopItem.buyOne(gPlayer);
					}
				}
			}
		}
	}

	public enum BedWarsCategoryType {
		ARMOR, SWORDS, BOWS, FOOD, TOOLS, BLOCKS, SPECIAL;

		private static ItemStack[] items;

		public String getName(){
			switch(this){
				case ARMOR: return "§bBrneni";
				case SWORDS: return "§4Mece";
				case BOWS: return "§aLuky";
				case FOOD: return "§9Jidlo";
				case TOOLS: return "§eNastroje";
				case BLOCKS: return "§fBloky";
				case SPECIAL: return "§6Specialni";
			}
			return null;
		}

		public Material getMaterial(){
			switch(this){
				case ARMOR: return Material.CHAINMAIL_CHESTPLATE;
				case SWORDS: return Material.GOLD_SWORD;
				case BOWS: return Material.BOW;
				case FOOD: return Material.APPLE;
				case TOOLS: return Material.STONE_PICKAXE;
				case BLOCKS: return Material.SANDSTONE;
				case SPECIAL: return Material.TNT;
			}
			return null;
		}

		public static ItemStack[] getItems(){
			if(items == null){
				ItemStack[] items = new ItemStack[9];
				int i=0;
				for(BedWarsCategoryType type : BedWarsCategoryType.values()){
					ItemStack item = new ItemStack(type.getMaterial(),1);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(type.getName());
					item.setItemMeta(meta);
					items[i++] = item;
				}
				BedWarsCategoryType.items = items;
			}
			return BedWarsCategoryType.items;
		}

		public static BedWarsCategoryType getTypeByItemStack(ItemStack itemStack){
			for(BedWarsCategoryType type : BedWarsCategoryType.values()){
				if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(type.getName())) return type;
			}
			return null;
		}
	}

	public class BedWarsCategory {

		private BedWarsCategoryType type;
		private ArrayList<BedWarsCategoryItem> items;

		public BedWarsCategory(BedWarsCategoryType type){
			this.type = type;
		}

		public BedWarsCategoryType getType(){
			return type;
		}

		public ItemStack getItemStack(){
			ItemStack itemStack = new ItemStack(this.getType().getMaterial(),1);
			ItemMeta meta = itemStack.getItemMeta();
			meta.setDisplayName(this.getType().getName());
			itemStack.setItemMeta(meta);
			return itemStack;
		}

		public BedWarsCategoryType getTypeByItemStack(ItemStack itemStack){
			for(BedWarsCategoryType type : BedWarsCategoryType.values()){
				if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(type.getName())) return type;
			}
			return null;
		}

		public BedWarsCategoryItem getItemByIndex(int index){
			if(items.size() > index) return items.get(index);
			return null;
		}

		@SuppressWarnings("deprecation")
		public ArrayList<BedWarsCategoryItem> getItems(){
			if(items != null) return items;
			ArrayList<BedWarsCategoryItem> shopItems = new ArrayList<BedWarsCategoryItem>();
			ItemStack itemStack;
			ItemMeta meta;
			switch(this.getType()){
				case ARMOR:{
					itemStack = new ItemStack(Material.LEATHER_HELMET,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,1,false);
					meta.addEnchant(Enchantment.DURABILITY,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.BRONZE,1));

					itemStack = new ItemStack(Material.LEATHER_LEGGINGS,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,1,false);
					meta.addEnchant(Enchantment.DURABILITY,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.BRONZE,1));

					itemStack = new ItemStack(Material.LEATHER_BOOTS,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,1,false);
					meta.addEnchant(Enchantment.DURABILITY,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.BRONZE,1));

					itemStack = new ItemStack(Material.CHAINMAIL_CHESTPLATE,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,1,false);
					meta.addEnchant(Enchantment.DURABILITY,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.IRON,2));

					itemStack = new ItemStack(Material.CHAINMAIL_CHESTPLATE,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,2,false);
					meta.addEnchant(Enchantment.DURABILITY,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.IRON,4));

					itemStack = new ItemStack(Material.CHAINMAIL_CHESTPLATE,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,3,false);
					meta.addEnchant(Enchantment.DURABILITY,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.IRON,7));

					itemStack = new ItemStack(Material.SHIELD,1);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.IRON,7));
					break;
				}
				case SWORDS:{
					itemStack = new ItemStack(Material.STICK,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.KNOCKBACK,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.BRONZE,8));

					itemStack = new ItemStack(Material.GOLD_SWORD,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.DAMAGE_ALL,1,false);
					meta.addEnchant(Enchantment.DURABILITY,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.IRON,1));

					itemStack = new ItemStack(Material.GOLD_SWORD,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.DAMAGE_ALL,2,false);
					meta.addEnchant(Enchantment.DURABILITY,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.IRON,3));

					itemStack = new ItemStack(Material.GOLD_SWORD,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.DAMAGE_ALL,3,false);
					meta.addEnchant(Enchantment.DURABILITY,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.IRON,7));

					itemStack = new ItemStack(Material.IRON_SWORD,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.KNOCKBACK,1,false);
					meta.addEnchant(Enchantment.DAMAGE_ALL,2,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.GOLD,8));
					break;
				}
				case BOWS:{
					itemStack = new ItemStack(Material.BOW,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.ARROW_INFINITE,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.GOLD,5));

					itemStack = new ItemStack(Material.BOW,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.ARROW_INFINITE,1,false);
					meta.addEnchant(Enchantment.ARROW_DAMAGE,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.GOLD,8));

					itemStack = new ItemStack(Material.BOW,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.ARROW_INFINITE,1,false);
					meta.addEnchant(Enchantment.ARROW_DAMAGE,2,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.GOLD,14));

					itemStack = new ItemStack(Material.BOW,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.ARROW_INFINITE,1,false);
					meta.addEnchant(Enchantment.ARROW_FIRE,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.GOLD,18));

					itemStack = new ItemStack(Material.BOW,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.ARROW_INFINITE,1,false);
					meta.addEnchant(Enchantment.ARROW_FIRE,1,false);
					meta.addEnchant(Enchantment.ARROW_KNOCKBACK,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.GOLD,22));

					itemStack = new ItemStack(Material.ARROW,1);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.GOLD,1));

					itemStack = new ItemStack(Material.SPECTRAL_ARROW,1);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.GOLD,3));
					break;
				}
				case FOOD:{
					itemStack = new ItemStack(Material.APPLE,1);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.BRONZE,1));

					itemStack = new ItemStack(Material.GRILLED_PORK,2);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.BRONZE,4));

					itemStack = new ItemStack(Material.CAKE,1);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.IRON,1));

					itemStack = new ItemStack(Material.GOLDEN_APPLE,1);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.GOLD,2));
					break;
				}
				case TOOLS:{
					itemStack = new ItemStack(Material.WOOD_PICKAXE,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.DIG_SPEED,1,false);
					meta.addEnchant(Enchantment.DURABILITY,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.BRONZE,5));

					itemStack = new ItemStack(Material.STONE_PICKAXE,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.DIG_SPEED,1,false);
					meta.addEnchant(Enchantment.DURABILITY,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.IRON,3));

					itemStack = new ItemStack(Material.STONE_PICKAXE,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.DIG_SPEED,2,false);
					meta.addEnchant(Enchantment.DURABILITY,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.IRON,6));

					itemStack = new ItemStack(Material.IRON_PICKAXE,1);
					meta = itemStack.getItemMeta();
					meta.addEnchant(Enchantment.DIG_SPEED,3,false);
					meta.addEnchant(Enchantment.DURABILITY,1,false);
					itemStack.setItemMeta(meta);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.GOLD,6));
					break;
				}
				case BLOCKS:{
					itemStack = new ItemStack(Material.SANDSTONE,2);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.BRONZE,1));

					itemStack = new ItemStack(Material.ENDER_STONE,1);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.BRONZE,8));

					itemStack = new ItemStack(Material.IRON_BLOCK,1);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.IRON,3));

					itemStack = new ItemStack(Material.GLOWSTONE,4);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.BRONZE,15));

					itemStack = new ItemStack(Material.GLASS,1);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.BRONZE,4));

					itemStack = new ItemStack(Material.LADDER,1);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.BRONZE,1));
					break;
				}
				case SPECIAL:{
					itemStack = new ItemStack(Material.WEB,1);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.BRONZE,16));

					itemStack = new ItemStack(Material.TNT,1);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.IRON,3));

					itemStack = new ItemStack(Material.FLINT_AND_STEEL,1);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.GOLD,2));

					itemStack = new ItemStack(Material.MONSTER_EGG,1,(short)0,(byte)91);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.BRONZE,64));

					itemStack = new ItemStack(Material.CHEST,1);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.IRON,3));

					itemStack = new ItemStack(Material.ENDER_CHEST,1);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.GOLD,1));

					itemStack = new ItemStack(Material.ENDER_PEARL,1);
					shopItems.add(new BedWarsCategoryItem(itemStack,BedWarsResourceType.GOLD,6));
					break;
				}
			}
			items = shopItems;
			return shopItems;
		}
	}

	public class BedWarsCategoryItem {

		private ItemStack itemStack;
		private ItemStack itemStackToBuy;
		private BedWarsResourceType resource;
		private int price;

		public BedWarsCategoryItem(ItemStack itemStack,BedWarsResourceType resource,int price){
			this.itemStack = itemStack;
			this.itemStackToBuy = itemStack.clone();
			this.resource = resource;
			this.price = price;
			ItemMeta meta = this.itemStack.getItemMeta();
			ArrayList<String> lore = new ArrayList<String>();
			lore.add("§f"+this.price+" "+this.resource.toItemName());
			meta.setLore(lore);
			this.itemStack.setItemMeta(meta);
		}

		public ItemStack getItemStack(){
			return itemStack;
		}

		public ItemStack getItemStackToBuy(){
			return itemStackToBuy;
		}

		public BedWarsResourceType getResource(){
			return resource;
		}

		public int getPrice(){
			return price;
		}

		public boolean hasEnoughResources(GamePlayer gPlayer){
			return gPlayer.getPlayer().getInventory().contains(resource.toMaterial(),price);
		}

		public boolean buyOne(GamePlayer gPlayer){
			return this.buyOne(gPlayer,true);
		}

		public boolean buyOne(GamePlayer gPlayer,boolean sound){
			if(this.hasEnoughResources(gPlayer)){
				if(sound) gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ITEM_PICKUP,1f,1f);
				ItemUtil.removeItems(gPlayer.getPlayer().getInventory(),resource.toItemStack(),price);
				ItemStack item = this.getItemStackToBuy().clone();
				if(item.getType() == Material.LEATHER_HELMET || item.getType() == Material.LEATHER_CHESTPLATE || item.getType() == Material.LEATHER_LEGGINGS || item.getType() == Material.LEATHER_BOOTS){
					item = item.clone();
					LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
			        meta.setColor(game.getTeams().getPlayerTeam(gPlayer).getType().getColor());
					item.setItemMeta(meta);
				}
				gPlayer.getPlayer().getInventory().addItem(item);
				return true;
			}
			else if(sound) gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ITEM_BREAK,1f,1f);
			return false;
		}

		public void buyAll(GamePlayer gPlayer){
			int bought = 0;
			boolean cancel = false;
			while(this.buyOne(gPlayer,false) && !cancel){
				bought += this.getItemStackToBuy().getAmount();
				cancel = ((bought + this.getItemStackToBuy().getAmount()) > 64);
			}
			if(bought > 0) gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ITEM_PICKUP,1f,1f);
			else gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ITEM_BREAK,1f,1f);
		}
	}
}