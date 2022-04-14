package com.games.game;

import com.games.Games;
import com.games.events.GameEndEvent;
import com.games.utils.SkinUtil;
import com.games.utils.SkinUtil.Skin;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import realcraft.bukkit.utils.ItemUtil;
import realcraft.bukkit.wrappers.HologramsApi;

import java.util.List;
import java.util.Map;

public abstract class GamePodium implements Listener {

	private Game game;
	private GamePodiumType type;

	private GamePodiumStand[] stands;

	public GamePodium(Game game,GamePodiumType type){
		this.game = game;
		this.type = type;
		this.getStands();
		Bukkit.getPluginManager().registerEvents(this,Games.getInstance());
		this.update();
	}

	public Game getGame(){
		return game;
	}

	public GamePodiumType getType(){
		return type;
	}

	@SuppressWarnings("unchecked")
	public GamePodiumStand[] getStands(){
		if(stands == null){
			int index = 0;
			stands = new GamePodiumStand[0];
			List<Map<String, Object>> tempLocations = (List<Map<String, Object>>) Games.getInstance().getConfig().get("podiums."+type.toString()+".locations");
			if (game.getConfig().isSet("podiums."+type.toString()+".locations")) {
				tempLocations = (List<Map<String, Object>>) game.getConfig().get("podiums."+type.toString()+".locations");
			}
			if(tempLocations != null && !tempLocations.isEmpty()){
				stands = new GamePodiumStand[tempLocations.size()];
				for(Map<String, Object> location : tempLocations){
					double x = Double.valueOf(location.get("x").toString());
					double y = Double.valueOf(location.get("y").toString());
					double z = Double.valueOf(location.get("z").toString());
					float yaw = Float.valueOf(location.get("yaw").toString());
					float pitch = Float.valueOf(location.get("pitch").toString());
					World world = Bukkit.getWorld(location.get("world").toString());
					stands[index] = new GamePodiumStand(GamePodiumStandType.values()[index],new Location(world,x,y,z,yaw,pitch));
					index ++;
				}
			}
		}
		return stands;
	}

	public abstract void update();

	@EventHandler
	public void ChunkLoadEvent(ChunkLoadEvent event){
		for(GamePodiumStand stand : this.getStands()){
			if(stand.isInChunk(event.getChunk())){
				stand.spawn();
			}
		}
	}

	@EventHandler
	public void ChunkUnloadEvent(ChunkUnloadEvent event){
		for(GamePodiumStand stand : this.getStands()){
			if(stand.isInChunk(event.getChunk())){
				stand.remove();
			}
		}
	}

	@EventHandler
	public void PlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent event){
		for(GamePodiumStand stand : this.getStands()){
			if(stand.getStand() != null && stand.getStand().equals(event.getRightClicked())){
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event){
		if(event.getEntity().getType() == EntityType.ARMOR_STAND){
			for(GamePodiumStand stand : this.getStands()){
				if(stand.getStand() != null && stand.getStand().equals(event.getEntity())){
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void GameEndEvent(GameEndEvent event){
		this.update();
	}

	public class GamePodiumStand {

		private GamePodiumStandType type;
		private String name;
		private Location location;
		private ArmorStand stand;
		private HologramsApi.Hologram hologram;
		private ItemStack head = new ItemStack(Material.PLAYER_HEAD);

		public GamePodiumStand(GamePodiumStandType type,Location location){
			this.type = type;
			this.location = location;
			this.hologram = HologramsApi.createHologram(location.clone().add(0.0,2.0,0.0));
			this.spawn();
		}

		public ArmorStand getStand(){
			return stand;
		}

		public void setData(String name,String value){
			hologram.clearLines();
			hologram.insertTextLine(0,"§f"+name);
			hologram.insertTextLine(1,"§6"+value);
			if(this.name == null || !this.name.equalsIgnoreCase(name)){
				this.name = name;
				Bukkit.getScheduler().runTaskAsynchronously(Games.getInstance(),new Runnable(){
					@Override
					public void run(){
						Skin skin = new Skin("steve","","","");
						String skinName = SkinUtil.getPlayerSkin(name);
						if(skinName != null) skin = SkinUtil.getSkin(skinName);
						if(skin == null) skin = new SkinUtil.DefaultSkin();
						final Skin skinFinal = skin;
						Bukkit.getScheduler().runTask(Games.getInstance(),new Runnable(){
							@Override
							public void run(){
								head = ItemUtil.getHead(skinFinal.getValue());
								if(stand != null) stand.getEquipment().setHelmet(head);
							}
						});
					}
				});
			}
		}

		public boolean isInChunk(Chunk chunk){
			return (location.getBlockX() >> 4 == chunk.getX() && location.getBlockZ() >> 4 == chunk.getZ());
		}

		private void spawn(){
			this.remove();
			stand = (ArmorStand)location.getWorld().spawnEntity(location,EntityType.ARMOR_STAND);
			stand.setBasePlate(false);
			stand.setArms(true);
			stand.setSmall(true);
			stand.getEquipment().setHelmet(head);
			if(type == GamePodiumStandType.FIRST){
				stand.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
				stand.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
				stand.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
			}
			else if(type == GamePodiumStandType.SECOND){
				stand.getEquipment().setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
				stand.getEquipment().setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
				stand.getEquipment().setBoots(new ItemStack(Material.GOLDEN_BOOTS));
			}
			else if(type == GamePodiumStandType.THIRD){
				stand.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
				stand.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
				stand.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
			}
			else if(type == GamePodiumStandType.FOURTH){
				stand.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
				stand.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
				stand.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
			}
			else if(type == GamePodiumStandType.FIFTH){
				stand.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
				stand.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
				stand.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
			}
			if(GamePodium.this.getType() == GamePodiumType.LEFT) stand.setHeadPose(new EulerAngle(0,Math.toRadians(-10),0));
			else if(GamePodium.this.getType() == GamePodiumType.RIGHT) stand.setHeadPose(new EulerAngle(0,Math.toRadians(10),0));
			for(Entity entity : stand.getNearbyEntities(0.2,0.2,0.2)){
				if(entity.getType() == EntityType.ARMOR_STAND){
					entity.remove();
				}
			}
		}

		private void remove(){
			if(stand != null && !stand.isDead()){
				stand.remove();
				stand = null;
			}
		}
	}

	public enum GamePodiumStandType {
		FIRST, SECOND, THIRD, FOURTH, FIFTH;
	}

	public enum GamePodiumType {
		LEFT, RIGHT, CENTER;

		public String toString(){
			return this.name().toLowerCase();
		}

		public int getId(){
			switch(this){
				case LEFT: return 1;
				case RIGHT: return 2;
				case CENTER: return 3;
			}
			return 0;
		}
	}
}