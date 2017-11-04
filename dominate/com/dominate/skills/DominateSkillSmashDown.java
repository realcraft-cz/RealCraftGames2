package com.dominate.skills;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import com.anticheat.AntiCheat;
import com.dominate.Dominate;
import com.dominate.DominateUser;
import com.dominate.DominateUtils;
import com.games.Games;
import com.games.game.GameState;
import com.games.player.GamePlayerState;
import com.games.utils.Particles;
import com.games.utils.Particles.BlockData;

public class DominateSkillSmashDown extends DominateSkill {

	private HashMap<Integer,Entity> entities = new HashMap<Integer,Entity>();
	private long started;

	public DominateSkillSmashDown(Dominate game,DominateUser dPlayer){
		super(DominateSkillType.SMASHDOWN,game,dPlayer);
	}

	@Override
	public void activate(Entity none){
		this.getPlayer().getWorld().playSound(this.getPlayer().getLocation(),Sound.ENTITY_FIREWORK_LAUNCH,1f,1f);
		this.getPlayer().setVelocity(this.getPlayer().getVelocity().setY(2));
		this.getPlayer().setFlying(false);
		this.getPlayer().setAllowFlight(false);
		AntiCheat.exempt(this.getPlayer(),1000);
		started = System.currentTimeMillis();
		this.setRunning(true);
	}

	@Override
	public void clear(){
		for(Entity entity : entities.values()) entity.remove();
		entities.clear();
	}

	@Override
	public void run(){
		if(!this.isRunning()) return;
		if(started+500 > System.currentTimeMillis()){
			Particles.CLOUD.display(0.1f,0.1f,0.1f,0,4,this.getPlayer().getLocation(),64);
		}
		else if(started > 0){
			started = -1;
			this.getPlayer().setVelocity(this.getPlayer().getVelocity().setY(-2));
		}
		else if(started == -1 && this.getPlayer().getVelocity().getY() > -0.5 && (this.getPlayer().getLocation().getY() == this.getPlayer().getLocation().getBlockY() || this.getPlayer().getLocation().getY() == this.getPlayer().getLocation().getBlockY()+0.5)){
			started = -2;
			if(!this.getGame().getTeams().isLocationInSpawn(this.getPlayer().getLocation())){
				this.getPlayer().getWorld().playSound(this.getPlayer().getLocation(),Sound.ENTITY_ENDERDRAGON_FIREBALL_EXPLODE,2f,1f);
				Particles.EXPLOSION_LARGE.display(2.0f,1.0f,2.0f,0f,4,this.getPlayer().getLocation(),128);
				List<Entity> entities = this.getPlayer().getNearbyEntities(3.0,3.0,3.0);
				for(Entity victim : entities){
	                if(!(victim instanceof Player)) continue;
	                if(this.getGame().getGamePlayer((Player)victim).getState() == GamePlayerState.SPECTATOR) continue;
	                if(this.getGame().getTeams().getPlayerTeam(this.getGame().getGamePlayer((Player)victim)) == this.getGame().getTeams().getPlayerTeam(this.getGamePlayer())) continue;
	                double dX = this.getPlayer().getLocation().getX() - victim.getLocation().getX();
	                double dY = this.getPlayer().getLocation().getY() - victim.getLocation().getY();
	                double dZ = this.getPlayer().getLocation().getZ() - victim.getLocation().getZ();
	                double yaw = Math.atan2(dZ, dX);
	                double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;
	                double X = Math.sin(pitch) * Math.cos(yaw);
	                double Y = Math.sin(pitch) * Math.sin(yaw);
	                double Z = Math.cos(pitch);
	                double distance = this.getPlayer().getLocation().distance(victim.getLocation());
	                if(distance > 3) distance = 3;
	                double damage = 20-(distance*(20/3.0));
	                if(damage < 1) damage = 1;
	                victim.teleport(victim.getLocation().add(0,0.2,0));
	                Vector vector = new Vector(X,Z,Y);
	                victim.setVelocity(vector.multiply(0.5).setY(0.5));
	                ((Player)victim).damage(damage,this.getPlayer());
	                AntiCheat.exempt((Player)victim,1000);
				}
				List<Block> blocks = DominateUtils.getNearbyBlocks(this.getPlayer().getLocation(),2);
				for(Block block : blocks){
					if(block.getType().isSolid() && block.getRelative(BlockFace.UP).getType() == Material.AIR && DominateUtils.getRandomBoolean()){
						@SuppressWarnings("deprecation")
						FallingBlock fallblock = this.getPlayer().getWorld().spawnFallingBlock(block.getLocation().clone().add(0,1.1f,0),block.getType(),block.getData());
						double dX = this.getPlayer().getLocation().getX() - fallblock.getLocation().getX();
		                double dY = this.getPlayer().getLocation().getY() - fallblock.getLocation().getY();
		                double dZ = this.getPlayer().getLocation().getZ() - fallblock.getLocation().getZ();
		                double yaw = Math.atan2(dZ, dX);
		                double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;
		                double X = Math.sin(pitch) * Math.cos(yaw);
		                double Y = Math.sin(pitch) * Math.sin(yaw);
		                double Z = Math.cos(pitch);
		                Vector vector = new Vector(X+DominateUtils.getRandomDouble(-0.2,0.2),Z,Y+DominateUtils.getRandomDouble(-0.2,0.2));
		                fallblock.setVelocity(vector.multiply(0.4).setY(DominateUtils.getRandomDouble(0.4,0.8)));
		                fallblock.setDropItem(false);
		                this.entities.put(fallblock.getEntityId(),fallblock);
					}
					if(block.getType() == Material.ICE){
						block.setType(Material.AIR);
						block.getWorld().playSound(block.getLocation(),Sound.BLOCK_GLASS_BREAK,1f,1f);
						Particles.BLOCK_CRACK.display(new BlockData(Material.ICE,(byte)0),0.3f,0.2f,0.3f,0.0f,8,block.getLocation().add(0.5,0.5,0.5),64);
					}
				}
			}
			Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					DominateSkillSmashDown.this.setRunning(false);
				}
			},2);
		}
	}

	@Override
	public void recharged(){
	}

	@EventHandler
	public void EntityChangeBlockEvent(EntityChangeBlockEvent event){
		if(entities.containsKey(event.getEntity().getEntityId())){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void PlayerToggleFlightEvent(PlayerToggleFlightEvent event){
		if(event.getPlayer().equals(this.getPlayer())){
			this.trigger();
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void PlayerMoveEvent(PlayerMoveEvent event){
		if(event.getPlayer().equals(this.getPlayer())){
			if((this.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR || this.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getType() != Material.AIR) && this.getPlayer().getFallDistance() < 3){
				if(this.getPlayer().getAllowFlight() == false && !this.isRunning() && !this.isCooldown() && !this.hasLowPower()){
					Bukkit.getScheduler().runTask(Games.getInstance(),new Runnable(){
						@Override
						public void run(){
							event.getPlayer().setAllowFlight(true);
						}
					});
				}
			}
			else if(this.getPlayer().getAllowFlight() == true) this.getPlayer().setAllowFlight(false);

		}
	}

	@EventHandler
	public void EntityDamageEvent(EntityDamageEvent event){
		if(event.getEntity().equals(this.getPlayer()) && this.getGame().getState() == GameState.INGAME){
			if(event.getCause() == DamageCause.FALL && this.isRunning()){
				event.setCancelled(true);
			}
		}
	}

	@Override
	public void updateInventory(){
	}
}