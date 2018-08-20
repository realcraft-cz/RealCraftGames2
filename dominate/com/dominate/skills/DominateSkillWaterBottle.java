package com.dominate.skills;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.dominate.Dominate;
import com.dominate.DominateUser;
import com.dominate.DominateUtils;
import com.games.game.GameState;

public class DominateSkillWaterBottle extends DominateSkill {

	public DominateSkillWaterBottle(Dominate game,DominateUser dPlayer){
		super(DominateSkillType.WATER_BOTTLE,game,dPlayer);
	}

	@Override
	public void activate(Entity none){
		this.getPlayer().getWorld().playSound(this.getPlayer().getLocation(),Sound.ITEM_BUCKET_FILL,1f,1f);
		DominateUtils.removeItems(this.getPlayer().getInventory(),DominateSkillType.WATER_BOTTLE.getMaterial(),1);
		this.getPlayer().setFireTicks(0);
	}

	@Override
	public void clear(){
	}

	@Override
	public void run(){
	}

	@Override
	public void recharged(){
	}

	@EventHandler
	public void PlayerInteractEvent(PlayerInteractEvent event){
		if(event.getPlayer().equals(this.getPlayer()) && this.getGame().getState() == GameState.INGAME){
			event.setCancelled(true);
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

	@Override
	public void updateInventory(){
	}
}