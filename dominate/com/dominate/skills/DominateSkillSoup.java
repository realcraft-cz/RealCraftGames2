package com.dominate.skills;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.dominate.Dominate;
import com.dominate.DominateUser;
import com.games.game.GameState;

public class DominateSkillSoup extends DominateSkill {

	public DominateSkillSoup(Dominate game,DominateUser dPlayer){
		super(DominateSkillType.SOUP,game,dPlayer);
	}

	@Override
	public void activate(Entity none){
		this.getPlayer().getInventory().setItem(this.getPlayer().getInventory().getHeldItemSlot(),null);
		this.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,5*20,1),true);
		this.getPlayer().getWorld().playSound(this.getPlayer().getLocation(),Sound.ENTITY_GENERIC_EAT,1,1);
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