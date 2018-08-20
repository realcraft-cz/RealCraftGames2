package com.dominate.skills;

import com.dominate.Dominate;
import com.dominate.DominateUser;
import com.games.Games;
import com.games.game.GameState;
import com.games.player.GamePlayerState;
import com.games.utils.Glow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import realcraft.bukkit.utils.Particles;

import java.util.HashMap;
import java.util.List;

public class DominateSkillArrowBlindness extends DominateSkill {

	private HashMap<Integer,Entity> entities = new HashMap<Integer,Entity>();
	private boolean selected = true;

	public DominateSkillArrowBlindness(Dominate game,DominateUser dPlayer){
		super(DominateSkillType.ARROW_BLINDNESS,game,dPlayer);
	}

	@Override
	public void activate(Entity entity){
		entities.put(entity.getEntityId(),entity);
		selected = false;
		Bukkit.getScheduler().runTask(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				DominateSkillArrowBlindness.this.updateInventory();
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
			if(entity instanceof TippedArrow && ((TippedArrow)entity).getBasePotionData().getType() == PotionType.SPEED){
				if(!this.trigger(entity)) event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void ProjectileHitEvent(ProjectileHitEvent event){
		if(entities.containsKey(event.getEntity().getEntityId())){
			Projectile entity = event.getEntity();
			if(!this.getGame().getTeams().isLocationInSpawn(entity.getLocation())){
				entity.getWorld().playSound(entity.getLocation(),Sound.ENTITY_SPLASH_POTION_BREAK,1f,1f);
				Particles.FIREWORKS_SPARK.display(1.0f,1.0f,1.0f,0.2f,64,entity.getLocation().add(0,0.5,0),64);
				List<Entity> entities = entity.getNearbyEntities(5.0,5.0,5.0);
				for(Entity victim : entities){
	                if(!(victim instanceof Player)) continue;
	                if(this.getGame().getGamePlayer((Player)victim).getState() == GamePlayerState.SPECTATOR) continue;
	                if(this.getGame().getTeams().getPlayerTeam(this.getGame().getGamePlayer((Player)victim)) == this.getGame().getTeams().getPlayerTeam(this.getGamePlayer())) continue;
	                ((Player)victim).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,8*20,1),true);
	                ((Player)victim).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,5*20,1),true);
	                ((Player)victim).addPotionEffect(new PotionEffect(PotionEffectType.SLOW,4*20,2),true);
	                ((Player)victim).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,5*20,1),true);
				}
			}
			Bukkit.getScheduler().runTask(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					entity.remove();
					DominateSkillArrowBlindness.this.entities.remove(entity.getEntityId());
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
							player.playSound(player.getLocation(),Sound.BLOCK_NOTE_BLOCK_HAT,1f,1f);
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
			meta.addEnchant(Glow.getGlow(),1,true);
			item.setItemMeta(meta);
			this.getPlayer().getInventory().setItem(1,item);
		} else {
			if(this.getPlayer().getInventory().getItem(2) != null){
				int amount = this.getPlayer().getInventory().getItem(2).getAmount();
				this.getPlayer().getInventory().setItem(2,new ItemStack(Material.ARROW,amount));
			}
			item = this.getPlayer().getInventory().getItem(1);
			meta = item.getItemMeta();
			meta.removeEnchant(Glow.getGlow());
			item.setItemMeta(meta);
			this.getPlayer().getInventory().setItem(1,item);
		}
	}
}