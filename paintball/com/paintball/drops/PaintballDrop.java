package com.paintball.drops;

import com.games.Games;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.FireworkUtil;
import com.games.utils.RandomUtil;
import com.paintball.Paintball;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import realcraft.bukkit.utils.ItemUtil;
import realcraft.bukkit.utils.Particles;

public abstract class PaintballDrop implements Runnable, Listener {

	private static final String DROP_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGM2YmFjZDM2ZWQ2MGY1MzMxMzhlNzU5YzQyNTk0NjIyMmI3OGVkYTZiNjE2MjE2ZjZkY2MwOGU5MGQzM2UifX19";
	private static final ItemStack HEAD = ItemUtil.getHead(DROP_TEXTURE);

	private static final int LIVETIME_TICKS = 35*20;

	private PaintballDropType type;
	private Paintball game;
	private Location location;

	private ArmorStand stand;

	public PaintballDrop(PaintballDropType type,Paintball game,Location location){
		this.type = type;
		this.game = game;
		this.location = location;
	}

	public Paintball getGame(){
		return game;
	}

	public PaintballDropType getType(){
		return type;
	}

	public Location getLocation(){
		return location;
	}

	public void drop(){
		stand = (ArmorStand) location.getWorld().spawnEntity(this.getTopLocation(),EntityType.ARMOR_STAND);
		stand.setSmall(false);
		stand.setGravity(false);
		stand.setBasePlate(false);
		stand.setVisible(false);
		stand.setHelmet(HEAD);
		stand.setTicksLived(1);
		Bukkit.getPluginManager().registerEvents(this,Games.getInstance());
		this.run();
	}

	public void clear(){
		if(stand != null) stand.remove();
		game.getDrops().removeDrop(this);
		stand = null;
		location = null;
		HandlerList.unregisterAll(this);
	}

	@Override
	public void run(){
		if(stand != null){
			Location location = stand.getLocation();
			Particles.SPELL_WITCH.display(0.3f,0.3f,0.3f,0.5f,2,stand.getEyeLocation(),64);
			if(location.getY() > this.location.getY()-1){
				Particles.BLOCK_CRACK.display(Bukkit.createBlockData(Material.EMERALD_ORE),0.3f,0.5f,0.3f,0.0f,4,stand.getEyeLocation(),64);
				if(stand.getTicksLived()%3 == 0) location.getWorld().playSound(location,Sound.ENTITY_CHICKEN_EGG,1f,1f);
				if(stand.getTicksLived()%5 == 0) Particles.EXPLOSION_LARGE.display(0,0,0,0,1,stand.getEyeLocation(),128);
			}
			location.setYaw(location.getYaw()+10);
			if(location.getY() > this.location.getY()-1) location.add(0,-0.1,0);
			stand.teleport(location);

			if(stand.getTicksLived() <= LIVETIME_TICKS) Bukkit.getScheduler().runTaskLater(Games.getInstance(),this,1);
			else {
				Particles.EXPLOSION_LARGE.display(0,0,0,0,1,stand.getEyeLocation(),64);
				this.clear();
			}
		}
	}

	@EventHandler
	public void PlayerMoveEvent(PlayerMoveEvent event){
		if(stand != null){
			GamePlayer gPlayer = this.getGame().getGamePlayer(event.getPlayer());
			if(gPlayer.getState() != GamePlayerState.SPECTATOR){
				if(gPlayer.getPlayer().getLocation().distanceSquared(stand.getEyeLocation().clone().add(0,-1,0)) < 3){
					game.sendMessage(game.getTeams().getPlayerTeam(gPlayer).getType().getChatColor()+event.getPlayer().getName()+" "+this.getType().getMessage());
					FireworkUtil.spawnFirework(stand.getEyeLocation(),FireworkEffect.Type.BALL,Color.LIME,false,false);
					Particles.EXPLOSION_LARGE.display(0,0,0,0,1,stand.getEyeLocation(),64);
					Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
						@Override
						public void run(){
							gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1f,1f);
						}
					},5);
					Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
						@Override
						public void run(){
							PaintballDrop.this.activate(gPlayer);
						}
					},10);
					this.clear();
				}
			}
		}
	}

	@EventHandler
	public void PlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent event){
		if(stand != null && event.getRightClicked().equals(stand)){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event){
		if(event.getEntity().getType() == EntityType.ARMOR_STAND && event.getDamager() instanceof Player){
			if(stand != null && event.getEntity().equals(stand)){
				event.setCancelled(true);
			}
		}
	}

	public Location getTopLocation(){
		Location tmpLocation = location.clone();
		while(tmpLocation.getBlock().getType() == Material.AIR && tmpLocation.getBlockY()-32 < location.getBlockY()){
			tmpLocation.add(0,1,0);
		}
		tmpLocation.add(0,-1,0);
		return tmpLocation;
	}

	public abstract void activate(GamePlayer gPlayer);

	public enum PaintballDropType {
		GLOW, SPEED, GRENADE, AMMO;

		public String getMessage(){
			switch(this){
				case GLOW: return "§7aktivoval §b§lglowing";
				case SPEED: return "§7ziskal §d§lspeed boost";
				case GRENADE: return "§7ziskal §e§l2 granaty§7 pro svuj tym";
				case AMMO: return "§7ziskal §f§ldoplneni munice§7 pro svuj tym";
				default:break;
			}
			return null;
		}

		public static PaintballDropType getRandomType(){
			switch(RandomUtil.getRandomInteger(0,3)){
				case 0: return PaintballDropType.GLOW;
				case 1: return PaintballDropType.SPEED;
				case 2: return PaintballDropType.AMMO;
				case 3: return PaintballDropType.GRENADE;
				default: return PaintballDropType.AMMO;
			}
		}
	}
}