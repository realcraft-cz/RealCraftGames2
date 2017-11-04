package com.dominate.skills;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.dominate.Dominate;
import com.dominate.DominateUser;
import com.dominate.DominateUtils;
import com.games.Games;
import com.games.game.GameState;
import com.games.utils.LocationUtil;
import com.games.utils.RandomUtil;

public class DominateSkillWeb extends DominateSkill {

	private HashMap<Integer,Entity> entities = new HashMap<Integer,Entity>();
	private HashMap<Integer,BlockFace> faces = new HashMap<Integer,BlockFace>();
	private ArrayList<Location> blocks = new ArrayList<Location>();

	public DominateSkillWeb(Dominate game,DominateUser dPlayer){
		super(DominateSkillType.WEB,game,dPlayer);
	}

	@Override
	public void activate(Entity none){
		DominateUtils.removeItems(this.getPlayer().getInventory(),DominateSkillType.WEB.getMaterial(),1);
		Item item = this.getPlayer().getWorld().dropItem(this.getPlayer().getEyeLocation(),this.getType().getItemStack());
		item.setPickupDelay(9999);
        item.setVelocity(this.getPlayer().getEyeLocation().getDirection().multiply(1));
        entities.put(item.getEntityId(),item);
        faces.put(item.getEntityId(),LocationUtil.yawToFace(this.getPlayer().getLocation().getYaw(),true));
	}

	@Override
	public void clear(){
		for(Entity entity : entities.values()) entity.remove();
		for(Location block : blocks) block.getBlock().setType(Material.AIR);
		entities.clear();
		blocks.clear();
	}

	@Override
	public void run(){
		ArrayList<Entity> toremove = new ArrayList<Entity>();
		for(Entity item : entities.values()){
			if(item.isDead() || item.getTicksLived() > 10*20 || this.getGame().getTeams().isLocationInSpawn(item.getLocation())){
				toremove.add(item);
			}
			else if(item.isOnGround()){
				Location location = item.getLocation();
				BlockFace face = faces.get(item.getEntityId());
				this.runWebBlock(location.getBlock());
				this.runWebBlock(location.getBlock().getRelative(LocationUtil.yawToFace(LocationUtil.faceToYaw(face,true)+90,true)));
				this.runWebBlock(location.getBlock().getRelative(LocationUtil.yawToFace(LocationUtil.faceToYaw(face,true)-90,true)));
				this.runWebBlock(location.getBlock().getRelative(BlockFace.UP));
				this.runWebBlock(location.getBlock().getRelative(LocationUtil.yawToFace(LocationUtil.faceToYaw(face,true)+90,true)).getRelative(BlockFace.UP));
				this.runWebBlock(location.getBlock().getRelative(LocationUtil.yawToFace(LocationUtil.faceToYaw(face,true)-90,true)).getRelative(BlockFace.UP));
				toremove.add(item);
			}
		}
		for(Entity entity : toremove){
			entity.remove();
			entities.remove(entity.getEntityId());
			faces.remove(entity.getEntityId());
		}
	}

	@Override
	public void recharged(){
	}

	private void runWebBlock(Block block){
		if(block.getType() == Material.AIR){
			block.setType(Material.WEB);
			Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					block.setType(Material.AIR);
					blocks.remove(block.getLocation());
				}
			},RandomUtil.getRandomInteger(140,180));
		}
	}

	/*private static int id = 0;
	private void runWebBreakEffect(Block block,int stage){
		id ++;
		PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation(this.getPlayer().getEntityId(),new BlockPosition(block.getLocation().getBlockX(),block.getLocation().getBlockY(),block.getLocation().getBlockZ()),stage);
		for(GamePlayer gPlayer : this.getGame().getPlayers()){
			((CraftPlayer)gPlayer.getPlayer()).getHandle().playerConnection.sendPacket(packet);
		}
	}*/

	@EventHandler
	public void ItemMergeEvent(ItemMergeEvent event){
		if(entities.containsKey(event.getEntity().getEntityId())){
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