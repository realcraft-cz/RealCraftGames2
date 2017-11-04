package com.dominate.skills;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.anticheat.AntiCheat;
import com.dominate.Dominate;
import com.dominate.DominateUser;
import com.dominate.DominateUtils;
import com.games.Games;
import com.games.game.GameState;
import com.games.player.GamePlayerState;
import com.games.utils.Glow;
import com.games.utils.Particles;
import com.games.utils.Particles.BlockData;

public class DominateSkillArrowExplosive extends DominateSkill {

	private HashMap<Integer,Entity> entities = new HashMap<Integer,Entity>();
	private boolean selected = true;

	public DominateSkillArrowExplosive(Dominate game,DominateUser dPlayer){
		super(DominateSkillType.ARROW_EXPLOSIVE,game,dPlayer);
	}

	@Override
	public void activate(Entity entity){
		entities.put(entity.getEntityId(),entity);
		selected = false;
		BukkitRunnable runnable = new BukkitRunnable(){
			@Override
			public void run(){
				if(!entity.isDead()) Particles.CLOUD.display(0.1f,0.1f,0.1f,0,3,entity.getLocation().add(0,0.2,0),64);
				else this.cancel();
			}
		};
		runnable.runTaskTimerAsynchronously(Games.getInstance(),1,1);
		Bukkit.getScheduler().runTask(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				DominateSkillArrowExplosive.this.updateInventory();
			}
		});
	}

	@Override
	public void clear(){
		for(Entity entity : entities.values()) entity.remove();
		entities.clear();
		selected = true;
	}

	@Override
	public void run(){
	}

	@Override
	public void recharged(){
	}

	@EventHandler
	public void ProjectileLaunchEvent(ProjectileLaunchEvent event){
		Projectile entity = event.getEntity();
		if(entity.getShooter() instanceof Player && ((Player)entity.getShooter()).equals(this.getPlayer())){
			if(entity instanceof TippedArrow && ((TippedArrow)entity).getBasePotionData().getType() == PotionType.WEAKNESS){
				if(!this.trigger(entity)) event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void ProjectileHitEvent(ProjectileHitEvent event){
		if(entities.containsKey(event.getEntity().getEntityId())){
			Projectile entity = event.getEntity();
			if(!this.getGame().getTeams().isLocationInSpawn(entity.getLocation())){
				Particles.EXPLOSION_HUGE.display(0,0,0,0,1,entity.getLocation(),64);
				entity.getWorld().playSound(entity.getLocation(),Sound.ENTITY_GENERIC_EXPLODE,2f,1f);
				List<Entity> entities = entity.getNearbyEntities(5.0,5.0,5.0);
				for(Entity victim : entities){
	                if(!(victim instanceof Player)) continue;
	                if(this.getGame().getGamePlayer((Player)victim).getState() == GamePlayerState.SPECTATOR) continue;
	                if(this.getGame().getTeams().getPlayerTeam(this.getGame().getGamePlayer((Player)victim)) == this.getGame().getTeams().getPlayerTeam(this.getGamePlayer())) continue;
	                double dX = entity.getLocation().getX() - victim.getLocation().getX();
	                double dY = entity.getLocation().getY() - victim.getLocation().getY();
	                double dZ = entity.getLocation().getZ() - victim.getLocation().getZ();
	                double yaw = Math.atan2(dZ, dX);
	                double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;
	                double X = Math.sin(pitch) * Math.cos(yaw);
	                double Y = Math.sin(pitch) * Math.sin(yaw);
	                double Z = Math.cos(pitch);
	                double distance = entity.getLocation().distance(victim.getLocation());
	                if(distance > 5) distance = 5;
	                double direction = 3-(distance*(3/5.0));
	                if(direction < 1) direction = 1;
	                double height = 2-(distance*(2/5.0));
	                if(height < 0.5) height = 0.5;
	                victim.teleport(victim.getLocation().add(0,0.2,0));
	                Vector vector = new Vector(X,Z,Y);
	                victim.setVelocity(vector.multiply(direction).setY(height));
	                AntiCheat.exempt((Player)victim,2000);
				}
			}
			List<Block> blocks = DominateUtils.getNearbyBlocks(entity.getLocation(),2);
			for(Block block : blocks){
				if(block.getType() == Material.ICE){
					block.setType(Material.AIR);
					block.getWorld().playSound(block.getLocation(),Sound.BLOCK_GLASS_BREAK,1f,1f);
					Particles.BLOCK_CRACK.display(new BlockData(Material.ICE,(byte)0),0.3f,0.2f,0.3f,0.0f,8,block.getLocation().add(0.5,0.5,0.5),64);
				}
			}
			Bukkit.getScheduler().runTask(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					entity.remove();
					DominateSkillArrowExplosive.this.entities.remove(entity.getEntityId());
				}
			});
		}
	}

	@EventHandler
	public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event){
		if(entities.containsKey(event.getDamager().getEntityId())){
			if(this.getGame().getTeams().getPlayerTeam(this.getGame().getGamePlayer((Player)event.getEntity())) != this.getGame().getTeams().getPlayerTeam(this.getGamePlayer())){
				((Player)event.getEntity()).damage(event.getDamage(),event.getDamager());
			}
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void PlayerInteractEvent(PlayerInteractEvent event){
		if(event.getPlayer().equals(this.getPlayer()) && this.getGame().getState() == GameState.INGAME){
			Player player = event.getPlayer();
			Action action = event.getAction();
			ItemStack item = player.getInventory().getItemInMainHand();
			if(item != null && (item.getType() == Material.BOW || item.getType() == Material.ARROW || item.getType() == Material.TIPPED_ARROW)){
				if(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK){
					if(!this.isCooldown()){
						if(!selected){
							selected = !selected;
							player.playSound(player.getLocation(),Sound.BLOCK_NOTE_HAT,1f,1f);
							this.updateInventory();
						}
					} else {
						player.playSound(player.getLocation(),Sound.ENTITY_ITEM_BREAK,1f,1f);
					}
				}
			}
		}
	}

	public void updateInventory(){
		ItemStack item;
		ItemMeta meta;
		if(selected){
			if(this.getPlayer().getInventory().getItem(2) != null){
				int amount = this.getPlayer().getInventory().getItem(2).getAmount();
				item = this.getType().getItemStack();
				item.setAmount(amount);
				this.getPlayer().getInventory().setItem(2,item);
			}

			item = this.getPlayer().getInventory().getItem(1);
			meta = item.getItemMeta();
			meta.addEnchant(new Glow(255),1,true);
			item.setItemMeta(meta);
			this.getPlayer().getInventory().setItem(1,item);
		} else {
			if(this.getPlayer().getInventory().getItem(2) != null){
				int amount = this.getPlayer().getInventory().getItem(2).getAmount();
				this.getPlayer().getInventory().setItem(2,new ItemStack(Material.ARROW,amount));
			}
			item = this.getPlayer().getInventory().getItem(1);
			meta = item.getItemMeta();
			meta.removeEnchant(new Glow(255));
			item.setItemMeta(meta);
			this.getPlayer().getInventory().setItem(1,item);
		}
	}
}