package com.dominate.skills;

import com.dominate.Dominate;
import com.dominate.DominateUser;
import com.games.Games;
import com.games.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import realcraft.bukkit.anticheat.AntiCheat;
import realcraft.bukkit.utils.Particles;

public class DominateSkillJump extends DominateSkill {

	private long started;

	public DominateSkillJump(Dominate game,DominateUser dPlayer){
		super(DominateSkillType.JUMP,game,dPlayer);
	}

	@Override
	public void activate(Entity none){
		this.getPlayer().setVelocity(this.getPlayer().getLocation().getDirection().multiply(1.2).setY(0.8));
		this.getPlayer().getWorld().playSound(this.getPlayer().getLocation(),Sound.ENTITY_BAT_TAKEOFF,0.5f,1f);
		this.getPlayer().setFlying(false);
		this.getPlayer().setAllowFlight(false);
		AntiCheat.exempt(this.getPlayer(),1000);
		started = System.currentTimeMillis();
		this.setRunning(true);
		Particles.SPELL_WITCH.display(0.4f,0.4f,0.4f,0f,3,this.getPlayer().getLocation(),64);
		for(int i=0;i<10;i++){
			Bukkit.getScheduler().runTaskLaterAsynchronously(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					Particles.SPELL_WITCH.display(0.4f,0.4f,0.4f,0f,3,DominateSkillJump.this.getPlayer().getLocation(),64);
				}
			},i);
		}
	}

	@Override
	public void clear(){
	}

	@Override
	public void run(){
		if(!this.isRunning()) return;
		if(started+500 < System.currentTimeMillis() && this.getPlayer().isOnGround()){
			Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					DominateSkillJump.this.setRunning(false);
				}
			},2);
		}
	}

	@Override
	public void recharged(){
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
					Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
						@Override
						public void run(){
							event.getPlayer().setAllowFlight(true);
						}
					},2);
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
				DominateSkillJump.this.setRunning(false);
			}
		}
	}

	@Override
	public void updateInventory(){
	}
}