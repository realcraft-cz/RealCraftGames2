package com.dominate.skills;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.dominate.Dominate;
import com.dominate.DominateUser;
import com.dominate.DominateUtils;
import com.games.Games;
import com.games.game.GameState;
import com.games.utils.Particles;
import com.games.utils.Particles.BlockData;

public class DominateSkillIceTrap extends DominateSkill {

	private HashMap<Integer,Entity> entities = new HashMap<Integer,Entity>();

	public DominateSkillIceTrap(Dominate game,DominateUser dPlayer){
		super(DominateSkillType.ICETRAP,game,dPlayer);
	}

	@Override
	public void activate(Entity none){
		Item item = this.getPlayer().getWorld().dropItem(this.getPlayer().getEyeLocation(),this.getType().getItemStack());
		item.setPickupDelay(9999);
        item.setVelocity(this.getPlayer().getEyeLocation().getDirection().multiply(1));
        entities.put(item.getEntityId(),item);
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
		for(Entity item : entities.values()){
			if(item.isDead() || item.getTicksLived() > 10*20 || this.getGame().getTeams().isLocationInSpawn(item.getLocation())){
				item.remove();
			}
			else if(item.isOnGround()){
				item.getWorld().playSound(item.getLocation(),Sound.BLOCK_GLASS_BREAK,1f,1f);
				item.getWorld().playSound(item.getLocation().clone().add(3,0,0),Sound.BLOCK_STONE_BREAK,1f,1f);
				item.getWorld().playSound(item.getLocation().clone().add(0,0,3),Sound.BLOCK_STONE_BREAK,1f,1f);
				item.getWorld().playSound(item.getLocation().clone().add(-3,0,0),Sound.BLOCK_STONE_BREAK,1f,1f);
				item.getWorld().playSound(item.getLocation().clone().add(0,0,-3),Sound.BLOCK_STONE_BREAK,1f,1f);
				ArrayList<Location> blocks = DominateUtils.makeSphere(item.getLocation(),this.getType().getMaterial(),3.25);
				if(item.getLocation().clone().add(0,3,0).getBlock().getType() == Material.ICE) item.getLocation().clone().add(0,3,0).getBlock().setType(Material.AIR);
				for(Location block : blocks){
					if(DominateUtils.getRandomBoolean()){
						Location location = new Location(block.getWorld(),block.getBlockX()+0.5,block.getBlockY()+0.5,block.getBlockZ()+0.5);
						Particles.BLOCK_CRACK.display(new BlockData(Material.ICE,(byte)0),0.3f,0.2f,0.3f,0.0f,8,location,64);
					}
				}
				Collections.sort(blocks,new Comparator<Location>(){
					@Override
					public int compare(Location point1,Location point2){
						int compare = Integer.compare(point1.getBlockY(),point2.getBlockY());
						if(compare > 0) return -1;
						else if(compare < 0) return 1;
						return 0;
					}
				});
				for(int i=0;i<blocks.size();i++){
					final int index = i;
					Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
						@Override
						public void run(){
							blocks.get(index).getBlock().setType(Material.AIR);
						}
					},80+i);
				}
				Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
					@Override
					public void run(){
						blocks.clear();
						DominateSkillIceTrap.this.setRunning(false);
					}
				},80+blocks.size());
				item.remove();
			} else {
				Particles.SNOW_SHOVEL.display(0.1f,0.1f,0.1f,0,4,item.getLocation().add(0,0.2,0),64);
			}
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
	public void BlockFadeEvent(BlockFadeEvent event){
		Block block = event.getBlock();
		if(block.getType() == Material.ICE){
			event.setCancelled(true);
		}
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