package com.games.game;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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

import com.games.Games;
import com.games.events.GameEndEvent;
import com.games.utils.ItemUtil;
import com.games.utils.SkinUtil;
import com.games.utils.SkinUtil.Skin;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

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
			stands = new GamePodiumStand[3];
			List<Map<String, Object>> tempLocations = (List<Map<String, Object>>) Games.getInstance().getConfig().get("podiums."+type.toString()+".locations");
			if(tempLocations != null && !tempLocations.isEmpty()){
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
		private Hologram hologram;
		private ItemStack head = new ItemStack(Material.SKULL_ITEM);

		public GamePodiumStand(GamePodiumStandType type,Location location){
			this.type = type;
			this.location = location;
			this.hologram = HologramsAPI.createHologram(Games.getInstance(),location.clone().add(0.0,2.0,0.0));
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
						String skinName = SkinUtil.getPlayerSkin(name);
						Skin skin = SkinUtil.getSkin(skinName);
						Bukkit.getScheduler().runTask(Games.getInstance(),new Runnable(){
							@Override
							public void run(){
								head = ItemUtil.getHead(skin.getValue());
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
				stand.getEquipment().setChestplate(new ItemStack(Material.GOLD_CHESTPLATE));
				stand.getEquipment().setLeggings(new ItemStack(Material.GOLD_LEGGINGS));
				stand.getEquipment().setBoots(new ItemStack(Material.GOLD_BOOTS));
			}
			else if(type == GamePodiumStandType.THIRD){
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
		FIRST, SECOND, THIRD;
	}

	public enum GamePodiumType {
		LEFT, RIGHT;

		public String toString(){
			return this.name().toLowerCase();
		}

		public int getId(){
			switch(this){
				case LEFT: return 1;
				case RIGHT: return 2;
			}
			return 0;
		}
	}
}