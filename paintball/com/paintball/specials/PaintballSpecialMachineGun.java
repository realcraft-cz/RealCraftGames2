package com.paintball.specials;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

import com.games.Games;
import com.games.game.GameState;
import com.games.player.GamePlayer;
import com.paintball.Paintball;

import realcraft.bukkit.RealCraft;
import realcraft.bukkit.utils.LocationUtil;
import realcraft.bukkit.utils.MaterialUtil;
import realcraft.bukkit.utils.RandomUtil;

public class PaintballSpecialMachineGun extends PaintballSpecial implements Runnable {

	private Location location;
	private GamePlayer gPlayer;
	private ArmorStand stand;
	private BukkitTask task;

	private int damage = 0;
	private boolean active = true;

	public PaintballSpecialMachineGun(Paintball game,Location location){
		super(PaintballSpecialType.MACHINEGUN,game);
		this.location = location;
	}

	private void activate(GamePlayer gPlayer){
		if(this.gPlayer == null && this.getGame().getState() == GameState.INGAME){
			this.gPlayer = gPlayer;
			stand = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0.0,-0.4,0.0),EntityType.ARMOR_STAND);
			stand.setSmall(true);
			stand.setGravity(false);
			stand.setBasePlate(false);
			stand.setVisible(false);
			stand.addPassenger(gPlayer.getPlayer());
			gPlayer.getPlayer().getInventory().setHeldItemSlot(0);
			if(task == null) task = Bukkit.getScheduler().runTaskTimer(Games.getInstance(),this,2,2);
		}
	}

	private void deactivate(){
		if(stand != null) stand.remove();
		if(gPlayer != null && this.getGame().getState().isGame()){
			Location location2 = location.clone().getBlock().getRelative(LocationUtil.yawToFace(location.getYaw())).getLocation().add(0.5,0.0,0.5);
			location2.setYaw(location.getYaw());
			Player player = gPlayer.getPlayer();
			Bukkit.getScheduler().runTask(RealCraft.getInstance(),new Runnable(){
				public void run(){
					player.teleport(location2);
				}
			});
			gPlayer.getPlayer().setExp(0);
		}
		stand = null;
		gPlayer = null;
	}

	@Override
	public void run(){
		damage -= 1;
		if(damage <= 0){
			damage = 0;
			if(!active){
				active = true;
			}
		}
		if(active && damage >= 100){
			damage = 100;
			active = false;
			location.getWorld().playSound(location,Sound.ENTITY_ITEM_BREAK,1f,1f);
			location.getWorld().playSound(location,Sound.BLOCK_FIRE_EXTINGUISH,1f,1f);
			location.getWorld().playSound(location,Sound.BLOCK_FIRE_EXTINGUISH,1f,0.5f);
		}
		if(gPlayer != null) gPlayer.getPlayer().setExp(damage*0.01f);
	}

	@Override
	public void clear(){
		if(task != null) task.cancel();
		task = null;
		damage = 0;
		active = true;
		this.deactivate();
	}

	@EventHandler(ignoreCancelled=true)
	public void PlayerInteractEvent(PlayerInteractEvent event){
		GamePlayer gPlayer = this.getGame().getGamePlayer(event.getPlayer());
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null &&
			(MaterialUtil.isStairs(event.getClickedBlock().getType()) || MaterialUtil.isButton(event.getClickedBlock().getType()) )){
			if(event.getClickedBlock().getLocation().distanceSquared(location) < 4){
				event.setCancelled(true);
				this.activate(gPlayer);
				return;
			}
		}
		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
			if(this.gPlayer == gPlayer){
				ItemStack itemStack = gPlayer.getPlayer().getInventory().getItemInMainHand();
				if(itemStack.getType() == Material.SNOWBALL){
					event.setCancelled(true);
					if(active){
						for(int i : new int[]{0,3}){
							Bukkit.getScheduler().runTaskLater(RealCraft.getInstance(),new Runnable(){
								public void run(){
									Snowball snowball = (Snowball) event.getPlayer().getWorld().spawnEntity(event.getPlayer().getEyeLocation(),EntityType.SNOWBALL);
							        snowball.setShooter(event.getPlayer());
							        snowball.setVelocity(event.getPlayer().getLocation().getDirection().multiply(1.8).add(new Vector(RandomUtil.getRandomDouble(-0.1,0.1),RandomUtil.getRandomDouble(-0.1,0.1),RandomUtil.getRandomDouble(-0.1,0.1))));
							        event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(),Sound.ENTITY_SNOWBALL_THROW,0.8f,0.5f);
							        if(active) damage += 3;
								}
							},i);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void EntityDismountEvent(EntityDismountEvent event){
		if(event.getEntity().getType() == EntityType.PLAYER && event.getDismounted().getType() == EntityType.ARMOR_STAND){
			if(stand != null && stand.getEntityId() == event.getDismounted().getEntityId()){
				this.deactivate();
			}
		}
	}
}