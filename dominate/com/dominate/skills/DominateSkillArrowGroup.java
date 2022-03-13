package com.dominate.skills;

import com.dominate.Dominate;
import com.dominate.DominateUser;
import com.dominate.DominateUtils;
import com.games.Games;
import com.games.game.GameState;
import realcraft.bukkit.utils.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;

public class DominateSkillArrowGroup extends DominateSkill {

	private HashMap<Integer,Entity> entities = new HashMap<Integer,Entity>();
	private int force;
	private long started;

	public DominateSkillArrowGroup(Dominate game,DominateUser dPlayer){
		super(DominateSkillType.ARROW_GROUP,game,dPlayer);
	}

	@Override
	public void activate(Entity entity){
		entities.put(entity.getEntityId(),entity);
		Player player = this.getPlayer();
		for(int i=0;i<(int)Math.floor(force*(6/15f));i++){
			Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					player.getWorld().playSound(player.getLocation(),Sound.ENTITY_ARROW_SHOOT,1f,1f);
					Arrow arrow = player.launchProjectile(Arrow.class);
					Vector vector = arrow.getVelocity();
	                vector.setX(vector.getX() + DominateUtils.getRandomDouble(-0.05,0.05));
	                vector.setY(vector.getY() + DominateUtils.getRandomDouble(-0.05,0.05));
	                vector.setZ(vector.getZ() + DominateUtils.getRandomDouble(-0.05,0.05));
	                arrow.setVelocity(vector);
	                entities.put(arrow.getEntityId(),arrow);
				}
			},i);
		}
		force = 0;
		this.setRunning(false);
		Title.sendActionBar(this.getPlayer(),"");
	}

	@Override
	public void clear(){
		for(Entity entity : entities.values()) entity.remove();
		entities.clear();
	}

	@Override
	public void run(){
		if(!entities.isEmpty()){
			ArrayList<Entity> toremove = new ArrayList<Entity>();
			for(Entity entity : entities.values()){
				if(entity.getTicksLived() >= 10*20 || entity.getLocation().getBlock().isLiquid()){
					toremove.add(entity);
				}
			}
			for(Entity entity : toremove){
				entity.remove();
				entities.remove(entity.getEntityId());
			}
		}
		if(this.isRunning()){
			if(started+800 < System.currentTimeMillis()){
				if(force < 15){
					force += 1;
					this.getPlayer().playSound(this.getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_HARP,1,1+(force*(1.0f/15f)));
				}
				String message = "";
				for(int i=0;i<15;i++){
					if(force > i){
						if(force < 6) message += "§e";
						else if(force < 11) message += "§6";
						else message += "§c";
					}
					else message += "§7";
					message += BLOCK_CHAR;
				}
				Title.sendActionBar(this.getPlayer(),message);
			}
		}
	}

	@Override
	public void recharged(){
	}

	@EventHandler
	public void ProjectileLaunchEvent(ProjectileLaunchEvent event){
		Projectile entity = event.getEntity();
		if(entity.getShooter() instanceof Player && ((Player)entity.getShooter()).equals(this.getPlayer())){
			if(entity instanceof Arrow){
				this.activate(entity);
			}
		}
	}

	@EventHandler
	public void PlayerInteractEvent(PlayerInteractEvent event){
		if(event.getPlayer().equals(this.getPlayer()) && this.getGame().getState() == GameState.INGAME){
			Player player = event.getPlayer();
			Action action = event.getAction();
			ItemStack item = player.getInventory().getItemInMainHand();
			if(item != null && item.getType() == Material.BOW && player.getInventory().contains(Material.ARROW)){
				if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK){
					started = System.currentTimeMillis();
					this.setRunning(true);
				}
			}
		}
	}

	@EventHandler
	public void PlayerItemHeldEvent(PlayerItemHeldEvent event){
		if(event.getPlayer().equals(this.getPlayer())){
			force = 0;
			this.setRunning(false);
			Title.sendActionBar(this.getPlayer(),"");
		}
	}

	@Override
	public void updateInventory(){
	}
}