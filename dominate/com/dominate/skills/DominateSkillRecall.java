package com.dominate.skills;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.dominate.Dominate;
import com.dominate.DominateUser;
import com.games.game.GameState;
import com.games.utils.Particles;

public class DominateSkillRecall extends DominateSkill {

	private LinkedList<PlayerPosition> lastPositions = new LinkedList<PlayerPosition>();
	private int maxPositions = 3*20;

	public DominateSkillRecall(Dominate game,DominateUser dPlayer){
		super(DominateSkillType.RECALL,game,dPlayer);
	}

	@Override
	public void activate(Entity none){
		Particles.FIREWORKS_SPARK.display(0.2f,0.2f,0.2f,0.1f,16,this.getPlayer().getLocation().clone().add(0,1,0),64);
		this.getPlayer().getWorld().playSound(this.getPlayer().getLocation(),Sound.ENTITY_WITCH_DRINK,1f,1f);
		this.getPlayer().getWorld().playSound(this.getPlayer().getLocation(),Sound.ENTITY_BAT_HURT,0.5f,1f);
		boolean first = false;
		for(PlayerPosition position : lastPositions){
			if(!first){
				first = true;
				this.getPlayer().teleport(position.getLocation());
				this.getPlayer().setVelocity(position.getVelocity());
				this.getPlayer().getWorld().playSound(position.getLocation(),Sound.ENTITY_WITCH_DRINK,1f,1f);
				this.getPlayer().getWorld().playSound(position.getLocation(),Sound.ENTITY_BAT_HURT,0.5f,1f);
			}
			Particles.SPELL_WITCH.display(0.4f,0.4f,0.4f,0f,3,position.getLocation(),64);
		}
	}

	@Override
	public void clear(){
		lastPositions = new LinkedList<PlayerPosition>();
	}

	@Override
	public void run(){
		lastPositions.add(new PlayerPosition(this.getPlayer().getLocation(),this.getPlayer().getVelocity()));
		if(lastPositions.size() > maxPositions) lastPositions.poll();
	}

	@Override
	public void recharged(){
	}

	@EventHandler
	public void PlayerInteractEvent(PlayerInteractEvent event){
		if(event.getPlayer().equals(this.getPlayer()) && this.getGame().getState() == GameState.INGAME){
			Player player = event.getPlayer();
			Action action = event.getAction();
			ItemStack item = player.getInventory().getItemInMainHand();
			if(item != null && item.getType() == this.getType().getMaterial()){
				if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK){
					this.trigger();
				}
			}
		}
	}

	@EventHandler
	public void PlayerDeathEvent(PlayerDeathEvent event){
		if(event.getEntity().equals(this.getPlayer())){
			this.clear();
		}
	}

	@Override
	public void updateInventory(){
	}

	public class PlayerPosition {
		private Location location;
		private Vector velocity;

		public PlayerPosition(Location location,Vector velocity){
			this.location = location;
			this.velocity = velocity;
		}

		public Location getLocation(){
			return location;
		}

		public Vector getVelocity(){
			return velocity;
		}
	}
}