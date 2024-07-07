package com.dominate;

import com.comphenix.protocol.ProtocolLibrary;
import com.dominate.skills.DominateSkill.DominateSkillType;
import com.games.Games;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import realcraft.bukkit.utils.MaterialUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DominateKit implements Listener {

	private Dominate game;
	private DominateArena arena;
	private DominateKitType type;
	private Location location;
	private ArmorStand stand;

	private HashMap<GamePlayer,Boolean> spawned = new HashMap<GamePlayer,Boolean>();

	public DominateKit(Dominate game,DominateArena arena,DominateKitType type,Location location){
		this.game = game;
		this.arena = arena;
		this.type = type;
		this.location = location;
		Bukkit.getPluginManager().registerEvents(this,Games.getInstance());
	}

	public Dominate getGame(){
		return game;
	}

	public DominateArena getArena(){
		return arena;
	}

	public DominateKitType getType(){
		return type;
	}

	public Location getLocation(){
		return location;
	}

	public ArmorStand getStand(){
		return stand;
	}

	public void spawn(){
		this.getLocation().getChunk().load();
		this.clear();
		stand = (ArmorStand)this.getLocation().getWorld().spawnEntity(this.getLocation(),EntityType.ARMOR_STAND);
		stand.setBasePlate(false);
		stand.setArms(true);
		stand.setCustomName(this.getType().getColor()+this.getType().getName());
		stand.setCustomNameVisible(true);
		if(this.getType().getHelmet() != null) stand.setHelmet(this.getType().getHelmet());
		if(this.getType().getChestplate() != null) stand.setChestplate(this.getType().getChestplate());
		if(this.getType().getLeggings() != null) stand.setLeggings(this.getType().getLeggings());
		if(this.getType().getBoots() != null) stand.setBoots(this.getType().getBoots());
		if(this.getType().getMainWeapon() != null) stand.setItemInHand(this.getType().getMainWeapon());
		if(this.getType().getSecondaryWeapon() != null) stand.getEquipment().setItemInOffHand(this.getType().getSecondaryWeapon());
		for(Entity entity : stand.getNearbyEntities(0.5,0.5,0.5)){
			if(entity.getType() == EntityType.ARMOR_STAND){
				entity.remove();
			}
		}
	}

	public void clear(){
		if(stand != null){
			stand.remove();
			stand = null;
		}
	}

	public void spawnForPlayer(GamePlayer gPlayer){
		if(!spawned.containsKey(gPlayer) || spawned.get(gPlayer) == false){
			spawned.put(gPlayer,true);
			ProtocolLibrary.getProtocolManager().updateEntity(stand,Arrays.asList(gPlayer.getPlayer()));
		}
	}

	public void despawnForPlayer(GamePlayer gPlayer){
		spawned.put(gPlayer,false);
		//((CraftPlayer)gPlayer.getPlayer()).getHandle().b.a(new PacketPlayOutEntityDestroy(stand.getEntityId()));
	}

	public boolean isSpawnedForPlayer(GamePlayer gPlayer){
		return (spawned.containsKey(gPlayer) && spawned.get(gPlayer));
	}

	public void run(){
		if(stand != null){
			if(stand.isDead()) this.spawn();
		}
	}

	public void reset(){
		this.spawn();
	}

	public void click(GamePlayer gPlayer){
		game.getUser(gPlayer).setKit(this.getType());
		gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1f,1f);
		gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_HORSE_ARMOR,0.5f,1f);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.hasItem() && MaterialUtil.isWool(event.getItem().getType())){
			// code ...
		}
	}

	@EventHandler
	public void PlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent event){
		if(event.getPlayer().getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
		if(this.getStand() != null && event.getRightClicked().equals(this.getStand())){
			Player player = event.getPlayer();
			GamePlayer gPlayer = arena.getGame().getGamePlayer(player);
			if(gPlayer.getState() != GamePlayerState.SPECTATOR){
				this.click(gPlayer);
			}
		}
	}

	@EventHandler
	public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event){
		if(event.getEntity().getType() == EntityType.ARMOR_STAND){
			if(this.getStand() != null && event.getEntity().equals(this.getStand())){
				if(event.getDamager() instanceof Player){
					Player player = (Player) event.getDamager();
					GamePlayer gPlayer = arena.getGame().getGamePlayer(player);
					if(gPlayer.getState() != GamePlayerState.SPECTATOR){
						this.click(gPlayer);
					}
				}
				event.setCancelled(true);
			}
		}
	}

	public enum DominateKitType {
		ARCHER, FROST, PYROMAN, CONFUSER, BOOMER;

		public String getName(){
			switch(this){
				case ARCHER: return "Archer";
				case FROST: return "Frost";
				case PYROMAN: return "Pyroman";
				case CONFUSER: return "Confuser";
				case BOOMER: return "Boomer";
			}
			return null;
		}

		public ChatColor getColor(){
			switch(this){
				case ARCHER: return ChatColor.WHITE;
				case FROST: return ChatColor.AQUA;
				case PYROMAN: return ChatColor.GOLD;
				case CONFUSER: return ChatColor.GRAY;
				case BOOMER: return ChatColor.YELLOW;
			}
			return ChatColor.WHITE;
		}

		public static DominateKitType getByName(String name){
			if(ARCHER.toString().equalsIgnoreCase(name)) return ARCHER;
			if(FROST.toString().equalsIgnoreCase(name)) return FROST;
			if(PYROMAN.toString().equalsIgnoreCase(name)) return PYROMAN;
			if(CONFUSER.toString().equalsIgnoreCase(name)) return CONFUSER;
			if(BOOMER.toString().equalsIgnoreCase(name)) return BOOMER;
			return null;
		}

		public ArrayList<DominateSkillType> getSkills(){
			ArrayList<DominateSkillType> skills = new ArrayList<DominateSkillType>();
			switch(this){
				case ARCHER:{
					skills.add(DominateSkillType.ARROW_GROUP);
					skills.add(DominateSkillType.JUMP);
					skills.add(DominateSkillType.WEB);
					skills.add(DominateSkillType.SOUP);
					break;
				}
				case FROST:{
					skills.add(DominateSkillType.ICETRAP);
					skills.add(DominateSkillType.FROSTWALK);
					skills.add(DominateSkillType.WATER_BOTTLE);
					skills.add(DominateSkillType.SOUP);
					break;
				}
				case PYROMAN:{
					skills.add(DominateSkillType.ARROW_FIRE);
					skills.add(DominateSkillType.FIRE);
					skills.add(DominateSkillType.SOUP);
					break;
				}
				case BOOMER:{
					skills.add(DominateSkillType.ARROW_EXPLOSIVE);
					skills.add(DominateSkillType.SMASHDOWN);
					skills.add(DominateSkillType.SOUP);
					break;
				}
				case CONFUSER:{
					skills.add(DominateSkillType.ARROW_BLINDNESS);
					skills.add(DominateSkillType.RECALL);
					skills.add(DominateSkillType.WEB);
					skills.add(DominateSkillType.SOUP);
					break;
				}
				default:break;
			}
			return skills;
		}

		public ItemStack getHelmet(){
			ItemStack item = null;
			ItemMeta meta;
			LeatherArmorMeta letherMeta;
			if(this == ARCHER){
				item = new ItemStack(Material.CHAINMAIL_HELMET);
				meta = item.getItemMeta();
				meta.addEnchant(Enchantment.PROJECTILE_PROTECTION,1,false);
				item.setItemMeta(meta);
			}
			else if(this == FROST){
				item = new ItemStack(Material.LEATHER_HELMET,1);
				letherMeta = (LeatherArmorMeta) item.getItemMeta();
				letherMeta.setColor(Color.WHITE);
				letherMeta.addEnchant(Enchantment.PROTECTION,2,false);
		        item.setItemMeta(letherMeta);
			}
			else if(this == PYROMAN){
				item = new ItemStack(Material.LEATHER_HELMET,1);
				letherMeta = (LeatherArmorMeta) item.getItemMeta();
				letherMeta.setColor(Color.ORANGE);
				letherMeta.addEnchant(Enchantment.FIRE_PROTECTION,4,false);
		        item.setItemMeta(letherMeta);
			}
			else if(this == CONFUSER){
				item = new ItemStack(Material.LEATHER_HELMET,1);
				letherMeta = (LeatherArmorMeta) item.getItemMeta();
				letherMeta.setColor(Color.BLACK);
				letherMeta.addEnchant(Enchantment.PROTECTION,2,false);
		        item.setItemMeta(letherMeta);
			}
			else if(this == BOOMER){
				item = new ItemStack(Material.GOLDEN_HELMET);
				meta = item.getItemMeta();
				meta.addEnchant(Enchantment.PROTECTION,1,false);
				meta.addEnchant(Enchantment.UNBREAKING,3,false);
				item.setItemMeta(meta);
			}
			return item;
		}

		public ItemStack getChestplate(){
			ItemStack item = null;
			ItemMeta meta;
			LeatherArmorMeta letherMeta;
			if(this == ARCHER){
				item = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
				meta = item.getItemMeta();
				meta.addEnchant(Enchantment.PROJECTILE_PROTECTION,2,false);
				item.setItemMeta(meta);
			}
			else if(this == FROST){
				item = new ItemStack(Material.IRON_CHESTPLATE);
				meta = item.getItemMeta();
				meta.addEnchant(Enchantment.PROTECTION,2,false);
				item.setItemMeta(meta);
			}
			else if(this == PYROMAN){
				item = new ItemStack(Material.LEATHER_CHESTPLATE,1);
				letherMeta = (LeatherArmorMeta) item.getItemMeta();
				letherMeta.setColor(Color.ORANGE);
				letherMeta.addEnchant(Enchantment.PROTECTION,3,false);
		        item.setItemMeta(letherMeta);
			}
			else if(this == CONFUSER){
				item = new ItemStack(Material.LEATHER_CHESTPLATE,1);
				letherMeta = (LeatherArmorMeta) item.getItemMeta();
				letherMeta.setColor(Color.GRAY);
				letherMeta.addEnchant(Enchantment.PROTECTION,3,false);
		        item.setItemMeta(letherMeta);
			}
			else if(this == BOOMER){
				item = new ItemStack(Material.GOLDEN_CHESTPLATE);
				meta = item.getItemMeta();
				meta.addEnchant(Enchantment.PROTECTION,2,false);
				meta.addEnchant(Enchantment.UNBREAKING,3,false);
				item.setItemMeta(meta);
			}
			return item;
		}

		public ItemStack getLeggings(){
			ItemStack item = null;
			ItemMeta meta;
			LeatherArmorMeta letherMeta;
			if(this == ARCHER){
				item = new ItemStack(Material.CHAINMAIL_LEGGINGS);
				meta = item.getItemMeta();
				meta.addEnchant(Enchantment.PROJECTILE_PROTECTION,2,false);
				item.setItemMeta(meta);
			}
			else if(this == FROST){
				item = new ItemStack(Material.LEATHER_LEGGINGS,1);
				letherMeta = (LeatherArmorMeta) item.getItemMeta();
				letherMeta.setColor(Color.WHITE);
				letherMeta.addEnchant(Enchantment.FIRE_PROTECTION,3,false);
		        item.setItemMeta(letherMeta);
			}
			else if(this == PYROMAN){
				item = new ItemStack(Material.LEATHER_LEGGINGS,1);
				letherMeta = (LeatherArmorMeta) item.getItemMeta();
				letherMeta.setColor(Color.ORANGE);
				letherMeta.addEnchant(Enchantment.PROTECTION,1,false);
		        item.setItemMeta(letherMeta);
			}
			else if(this == CONFUSER){
				item = new ItemStack(Material.LEATHER_LEGGINGS,1);
				letherMeta = (LeatherArmorMeta) item.getItemMeta();
				letherMeta.setColor(Color.BLACK);
				letherMeta.addEnchant(Enchantment.FIRE_PROTECTION,1,false);
		        item.setItemMeta(letherMeta);
			}
			else if(this == BOOMER){
				item = new ItemStack(Material.GOLDEN_LEGGINGS);
				meta = item.getItemMeta();
				meta.addEnchant(Enchantment.FIRE_PROTECTION,1,false);
				meta.addEnchant(Enchantment.UNBREAKING,3,false);
				item.setItemMeta(meta);
			}
			return item;
		}

		public ItemStack getBoots(){
			ItemStack item = null;
			ItemMeta meta;
			LeatherArmorMeta letherMeta;
			if(this == ARCHER){
				item = new ItemStack(Material.CHAINMAIL_BOOTS);
				meta = item.getItemMeta();
				meta.addEnchant(Enchantment.PROJECTILE_PROTECTION,2,false);
				item.setItemMeta(meta);
			}
			else if(this == FROST){
				item = new ItemStack(Material.IRON_BOOTS,1);
		        meta = item.getItemMeta();
		        meta.addEnchant(Enchantment.PROTECTION,2,false);
		        item.setItemMeta(meta);
			}
			else if(this == PYROMAN){
				item = new ItemStack(Material.GOLDEN_BOOTS);
				meta = item.getItemMeta();
		        meta.addEnchant(Enchantment.FIRE_PROTECTION,4,false);
		        item.setItemMeta(meta);
			}
			else if(this == CONFUSER){
				item = new ItemStack(Material.LEATHER_BOOTS,1);
				letherMeta = (LeatherArmorMeta) item.getItemMeta();
				letherMeta.setColor(Color.GRAY);
				letherMeta.addEnchant(Enchantment.PROTECTION,2,false);
		        item.setItemMeta(letherMeta);
			}
			else if(this == BOOMER){
				item = new ItemStack(Material.GOLDEN_BOOTS);
				meta = item.getItemMeta();
				meta.addEnchant(Enchantment.PROTECTION,2,false);
				meta.addEnchant(Enchantment.UNBREAKING,3,false);
				item.setItemMeta(meta);
			}
			return item;
		}

		public ItemStack getMainWeapon(){
			ItemStack item = null;
			ItemMeta meta;
			if(this == ARCHER){
				item = new ItemStack(Material.WOODEN_SWORD);
				meta = item.getItemMeta();
				meta.addEnchant(Enchantment.SHARPNESS,1,false);
				meta.addEnchant(Enchantment.UNBREAKING,3,false);
				meta.addEnchant(Enchantment.KNOCKBACK,1,false);
				item.setItemMeta(meta);
			}
			else if(this == FROST){
				item = new ItemStack(Material.IRON_SWORD);
				meta = item.getItemMeta();
				meta.addEnchant(Enchantment.SHARPNESS,1,false);
				item.setItemMeta(meta);
			}
			else if(this == PYROMAN){
				item = new ItemStack(Material.IRON_SWORD);
				meta = item.getItemMeta();
				meta.addEnchant(Enchantment.FIRE_ASPECT,1,false);
				item.setItemMeta(meta);
			}
			else if(this == CONFUSER){
				item = this.setItemName(new ItemStack(Material.IRON_SWORD),"§f[PRAVY KLIK]: §e3 sekundy zpet");
			}
			else if(this == BOOMER){
				item = new ItemStack(Material.IRON_SWORD);
			}
			return item;
		}

		public ItemStack getSecondaryWeapon(){
			ItemStack item = null;
			ItemMeta meta;
			if(this == ARCHER){
				item = new ItemStack(Material.BOW);
				meta = item.getItemMeta();
				meta.addEnchant(Enchantment.SHARPNESS,2,false);
				meta.addEnchant(Enchantment.KNOCKBACK,1,false);
				item.setItemMeta(meta);
			}
			else if(this == FROST){
				item = new ItemStack(Material.ICE);
			}
			else if(this == PYROMAN){
				item = this.setItemName(new ItemStack(Material.BOW),"§f[LEVY KLIK]: §6Horici sip");
			}
			else if(this == CONFUSER){
				item = this.setItemName(new ItemStack(Material.BOW),"§f[LEVY KLIK]: §bOslepujici sip");
			}
			else if(this == BOOMER){
				item = this.setItemName(new ItemStack(Material.BOW),"§f[LEVY KLIK]: §7Explozivni sip");
			}
			return item;
		}

		public void setPlayerArmor(GamePlayer gPlayer){
			gPlayer.getPlayer().getInventory().setHelmet(this.getHelmet());
			gPlayer.getPlayer().getInventory().setChestplate(this.getChestplate());
			gPlayer.getPlayer().getInventory().setLeggings(this.getLeggings());
			gPlayer.getPlayer().getInventory().setBoots(this.getBoots());
		}

		public void setPlayerWeapons(GamePlayer gPlayer){
			gPlayer.getPlayer().getInventory().setItem(0,this.getMainWeapon());
			gPlayer.getPlayer().getInventory().setItem(1,this.getSecondaryWeapon());
			if(this == ARCHER){
				gPlayer.getPlayer().getInventory().setItem(2,new ItemStack(Material.ARROW,32));
				for(int i=0;i<5;i++) gPlayer.getPlayer().getInventory().setItem(i+3,new ItemStack(Material.MUSHROOM_STEW));
				gPlayer.getPlayer().getInventory().setItem(8,DominateSkillType.WEB.getItemStack());
			}
			else if(this == FROST){
				gPlayer.getPlayer().getInventory().setItem(1,DominateSkillType.ICETRAP.getItemStack());
				gPlayer.getPlayer().getInventory().setItem(8,DominateSkillType.WATER_BOTTLE.getItemStack());
				for(int i=0;i<5;i++) gPlayer.getPlayer().getInventory().setItem(i+2,new ItemStack(Material.MUSHROOM_STEW));
			}
			else if(this == PYROMAN){
				gPlayer.getPlayer().getInventory().setItem(2,new ItemStack(Material.ARROW,16));
				for(int i=0;i<5;i++) gPlayer.getPlayer().getInventory().setItem(i+3,new ItemStack(Material.MUSHROOM_STEW));
			}
			else if(this == CONFUSER){
				gPlayer.getPlayer().getInventory().setItem(2,new ItemStack(Material.ARROW,16));
				gPlayer.getPlayer().getInventory().setItem(8,DominateSkillType.WEB.getItemStack());
				for(int i=0;i<5;i++) gPlayer.getPlayer().getInventory().setItem(i+3,new ItemStack(Material.MUSHROOM_STEW));
			}
			else if(this == BOOMER){
				gPlayer.getPlayer().getInventory().setItem(2,new ItemStack(Material.ARROW,8));
				for(int i=0;i<5;i++) gPlayer.getPlayer().getInventory().setItem(i+3,new ItemStack(Material.MUSHROOM_STEW));
			}
		}

		public ItemStack setItemName(ItemStack item,String name){
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(name);
			item.setItemMeta(meta);
			return item;
		}
	}
}