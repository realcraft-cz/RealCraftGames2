package com.dominate;

import com.games.Games;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import realcraft.bukkit.utils.FireworkUtil;
import realcraft.bukkit.utils.Particles;
import realcraft.bukkit.utils.Title;

public class DominateEmerald implements Listener {

	public static final int POINTS = 50;
	public static final int COOLDOWN = 90;

	private Dominate game;
	private DominateArena arena;
	private Location location;
	private int cooldown = 0;
	private Item item;

	public DominateEmerald(Dominate game,DominateArena arena,Location location){
		this.game = game;
		this.arena = arena;
		this.location = location;
		Bukkit.getPluginManager().registerEvents(this,Games.getInstance());
	}

	public Dominate getGame(){
		return game;
	}

	public DominateArena getArena(){
		return arena;
	}

	public Location getLocation(){
		return location;
	}

	public Item getItem(){
		return item;
	}

	public void spawn(){
		this.getLocation().getChunk().load();
		this.remove();
		item = this.getLocation().getWorld().dropItem(this.getLocation(),new ItemStack(Material.EMERALD));
		item.setVelocity(new Vector(0,0,0));
		item.teleport(this.getLocation());
		for(Entity entity : item.getNearbyEntities(2.0,2.0,2.0)){
			if(entity.getType() == EntityType.ITEM){
				if(((Item)entity).getItemStack().getType() == Material.EMERALD){
					entity.remove();
				}
			}
		}
	}

	public void remove(){
		if(item != null){
			item.remove();
			item = null;
		}
	}

	public void run(){
		if(cooldown > 0){
			cooldown --;
			if(cooldown == 0){
				this.spawn();
			}
		}
		else if(item != null){
			if(item.isDead()) this.spawn();
			else Particles.VILLAGER_HAPPY.display(1f,0.5f,1f,0f,10,this.getLocation().clone().add(0,1.0,0),64.0);
		}
	}

	public void reset(){
		cooldown = 0;
		this.spawn();
	}

	public void pickup(GamePlayer gPlayer){
		cooldown = COOLDOWN;
		game.getTeams().getPlayerTeam(gPlayer).pickupEmerald();
		FireworkUtil.spawnFirework(this.getLocation().clone().add(0,0.5,0),FireworkEffect.Type.BALL,Color.LIME,false,false);
		Title.showTitle(gPlayer.getPlayer()," ",0.5,2,0.5);
		Title.showSubTitle(gPlayer.getPlayer(),"�a+"+POINTS+" bodu",0.5,2,0.5);
		this.remove();
		Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,1);
			}
		},5);
	}

	@EventHandler
    public void EntityPickupItemEvent(EntityPickupItemEvent event){
		if(event.getEntity() instanceof Player){
			Item item = event.getItem();
			if(this.getItem() != null && item.getItemStack().getType() == Material.EMERALD){
				GamePlayer gPlayer = arena.getGame().getGamePlayer((Player)event.getEntity());
				if(gPlayer.getState() != GamePlayerState.SPECTATOR && item.getEntityId() == this.getItem().getEntityId()){
					this.pickup(gPlayer);
				}
				event.setCancelled(true);
			}
		}
	}
}