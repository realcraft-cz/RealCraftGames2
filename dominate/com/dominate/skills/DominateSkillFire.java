package com.dominate.skills;

import com.dominate.Dominate;
import com.dominate.DominateUser;
import com.dominate.DominateUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.util.Vector;
import realcraft.bukkit.utils.Particles;

import java.util.ArrayList;
import java.util.HashMap;

public class DominateSkillFire extends DominateSkill {

	private HashMap<Integer,Entity> entities = new HashMap<Integer,Entity>();

	public DominateSkillFire(Dominate game,DominateUser dPlayer){
		super(DominateSkillType.FIRE,game,dPlayer);
	}

	@Override
	public void activate(Entity none){
		if(none == null){
			Item item = this.getPlayer().getWorld().dropItem(this.getPlayer().getLocation(),this.getType().getItemStack());
			item.setPickupDelay(0);
			item.setVelocity(new Vector(DominateUtils.getRandomDouble(-0.1,0.1),DominateUtils.getRandomDouble(0.2,0.3),DominateUtils.getRandomDouble(-0.1,0.1)));
			entities.put(item.getEntityId(),item);
			Particles.LAVA.display(0.2f,0.0f,0.2f,1,2,this.getPlayer().getLocation(),64);
			this.getPlayer().getWorld().playSound(this.getPlayer().getLocation(),Sound.BLOCK_FIRE_EXTINGUISH,1f,1f);
		} else {
			for(int i=0;i<16;i++){
				Item item = none.getWorld().dropItem(none.getLocation().clone().add(0,0.5,0),this.getType().getItemStack());
				item.setPickupDelay(0);
				item.setVelocity(new Vector(DominateUtils.getRandomDouble(-0.3,0.3),DominateUtils.getRandomDouble(0.1,0.2),DominateUtils.getRandomDouble(-0.3,0.3)));
				entities.put(item.getEntityId(),item);
			}
			Particles.LAVA.display(0.4f,0.2f,0.4f,1,16,none.getLocation(),64);
			none.getWorld().playSound(none.getLocation(),Sound.ENTITY_GENERIC_EXTINGUISH_FIRE,1f,1f);
		}
	}

	@Override
	public void clear(){
		for(Entity entity : entities.values()) entity.remove();
		entities.clear();
	}

	@Override
	public void run(){
		ArrayList<Entity> toremove = new ArrayList<Entity>();
		for(Entity entity : entities.values()){
			if(entity.getTicksLived() >= 10*20 || entity.getLocation().getBlock().isLiquid()){
				toremove.add(entity);
			}
			if(entity.getLocation().getBlock().getType() == Material.ICE){
				entity.getLocation().getBlock().setType(Material.AIR);
				Particles.CLOUD.display(0.2f,0.1f,0.2f,0,4,entity.getLocation(),64);
				entity.getWorld().playSound(entity.getLocation(),Sound.BLOCK_FIRE_EXTINGUISH,1f,1f);
			}
			if(entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.ICE){
				entity.getLocation().getBlock().getRelative(BlockFace.DOWN).setType(Material.AIR);
				Particles.CLOUD.display(0.2f,0.1f,0.2f,0,4,entity.getLocation(),64);
				entity.getWorld().playSound(entity.getLocation(),Sound.BLOCK_FIRE_EXTINGUISH,1f,1f);
			}
		}
		for(Entity entity : toremove){
			entity.remove();
			entities.remove(entity.getEntityId());
		}
	}

	@Override
	public void recharged(){
	}

	@EventHandler
	public void ItemMergeEvent(ItemMergeEvent event){
		if(entities.containsKey(event.getEntity().getEntityId())){
			event.setCancelled(true);
		}
	}

	@EventHandler
    public void EntityPickupItemEvent(EntityPickupItemEvent event){
		if(entities.containsKey(event.getItem().getEntityId())){
			Player player = (Player)event.getEntity();
			if(this.getGame().getTeams().getPlayerTeam(this.getGame().getGamePlayer(player)) != this.getGame().getTeams().getPlayerTeam(this.getGamePlayer())){
				if(!this.getGame().getTeams().isLocationInSpawn(event.getItem().getLocation())){
					player.setFireTicks(8*20);
					player.getWorld().playSound(player.getLocation(),Sound.ITEM_FLINTANDSTEEL_USE,1f,1f);
					event.getItem().remove();
					entities.remove(event.getItem().getEntityId());
				}
			}
			event.setCancelled(true);
		}
	}

	/*@EventHandler
	public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event){
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Player && this.getGame().getTeams().getPlayerTeam(this.getGame().getGamePlayer((Player)event.getEntity())) != this.getGame().getTeams().getPlayerTeam(this.getGamePlayer())){
			if(this.getGame().getTeams().getPlayerTeam(this.getGame().getGamePlayer((Player)event.getDamager())) != this.getGame().getTeams().getPlayerTeam(this.getGamePlayer())){
				this.trigger();
			}
		}
	}*/

	@Override
	public void updateInventory(){
	}
}