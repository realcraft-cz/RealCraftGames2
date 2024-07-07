package com.dominate.skills;

import com.dominate.Dominate;
import com.dominate.DominateUser;
import com.dominate.DominateUtils;
import com.games.Games;
import com.games.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionType;
import realcraft.bukkit.utils.Particles;

import java.util.HashMap;
import java.util.List;

public class DominateSkillArrowFire extends DominateSkill {

	private HashMap<Integer,Entity> entities = new HashMap<Integer,Entity>();
	private boolean selected = true;

	public DominateSkillArrowFire(Dominate game,DominateUser dPlayer){
		super(DominateSkillType.ARROW_FIRE,game,dPlayer);
	}

	@Override
	public void activate(Entity entity){
		entity.setFireTicks(10*20);
		entities.put(entity.getEntityId(),entity);
		selected = false;
		Bukkit.getScheduler().runTask(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				DominateSkillArrowFire.this.updateInventory();
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
			if(entity.getType() == EntityType.ARROW && ((Arrow)entity).getBasePotionData().getType() == PotionType.FIRE_RESISTANCE){
				if(!this.trigger(entity)) event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void ProjectileHitEvent(ProjectileHitEvent event){
		if(entities.containsKey(event.getEntity().getEntityId())){
			Projectile entity = event.getEntity();
			if(!this.getGame().getTeams().isLocationInSpawn(entity.getLocation())) this.getDominateUser().getSkill(DominateSkillType.FIRE).activate(entity);
			List<Block> blocks = DominateUtils.getNearbyBlocks(entity.getLocation(),2);
			for(Block block : blocks){
				if(block.getType() == Material.ICE){
					block.setType(Material.AIR);
					entity.getWorld().playSound(block.getLocation(),Sound.BLOCK_FIRE_EXTINGUISH,1f,1f);
					Particles.CLOUD.display(0.2f,0.1f,0.2f,0,4,block.getLocation().add(0.5,0.5,0.5),64);
				}
			}
			Bukkit.getScheduler().runTask(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					entity.remove();
					DominateSkillArrowFire.this.entities.remove(entity.getEntityId());
				}
			});
		}
	}

	@EventHandler
	public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event){
		if(entities.containsKey(event.getDamager().getEntityId())){
			if(this.getGame().getTeams().getPlayerTeam(this.getGame().getGamePlayer((Player)event.getEntity())) != this.getGame().getTeams().getPlayerTeam(this.getGamePlayer())){
				((Player)event.getEntity()).damage(event.getDamage(),event.getDamager());
			} else {
				((Player)event.getEntity()).setFireTicks(0);
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
			meta.addEnchant(Enchantment.LURE,1,true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			item.setItemMeta(meta);
			this.getPlayer().getInventory().setItem(1,item);
		} else {
			if(this.getPlayer().getInventory().getItem(2) != null){
				int amount = this.getPlayer().getInventory().getItem(2).getAmount();
				this.getPlayer().getInventory().setItem(2,new ItemStack(Material.ARROW,amount));
			}
			item = this.getPlayer().getInventory().getItem(1);
			meta = item.getItemMeta();
			meta.removeEnchant(Enchantment.LURE);
			item.setItemMeta(meta);
			this.getPlayer().getInventory().setItem(1,item);
		}
	}
}