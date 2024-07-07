package com.paintball.specials;

import com.games.Games;
import com.games.game.GameState;
import com.games.player.GamePlayer;
import com.games.utils.RandomUtil;
import com.paintball.Paintball;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import realcraft.bukkit.utils.Particles;

import java.util.ArrayList;

public class PaintballSpecialGrenade extends PaintballSpecial {

	public PaintballSpecialGrenade(Paintball game){
		super(PaintballSpecialType.GRENADE,game);
	}

	private void activate(GamePlayer gPlayer){
		if(this.getGame().getState() == GameState.INGAME){
			Item grenade = gPlayer.getPlayer().getWorld().dropItem(gPlayer.getPlayer().getEyeLocation(),new ItemStack(Material.EGG));
			grenade.setPickupDelay(Integer.MAX_VALUE);
			grenade.setVelocity(gPlayer.getPlayer().getLocation().getDirection().multiply(1.5));
			gPlayer.getPlayer().getWorld().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_SILVERFISH_AMBIENT,1,2);
			this.getGame().getUser(gPlayer).addGrenades(-1);
			this.getGame().setPlayerWeapons(gPlayer,false);
			BukkitRunnable runnable = new BukkitRunnable(){
				private boolean landed = false;
				@Override
				public void run(){
					if(grenade.isOnGround() && !landed){
						landed = true;
						grenade.setVelocity(new Vector(0,0.5,0));
						Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
							public void run(){
								for(int i=0;i<3;i++){
									for(Vector velocity : PaintballSpecialGrenade.getDirections()){
										Snowball snowball = (Snowball) gPlayer.getPlayer().getWorld().spawnEntity(grenade.getLocation(),EntityType.SNOWBALL);
								        snowball.setShooter(gPlayer.getPlayer());
								        velocity = velocity.clone();
								        velocity = velocity.normalize().multiply(1.0);
								        velocity.setX(velocity.getX()+RandomUtil.getRandomDouble(-0.5,0.5));
								        velocity.setY(velocity.getY()+RandomUtil.getRandomDouble(-0.5,0.5));
								        velocity.setZ(velocity.getZ()+RandomUtil.getRandomDouble(-0.5,0.5));
								        snowball.setVelocity(velocity);
					                }
								}
								Particles.EXPLOSION.display(0,0,0,0,1,grenade.getLocation(),128);
								grenade.getWorld().playSound(grenade.getLocation(),Sound.ENTITY_GENERIC_EXPLODE,1f,2f);
								grenade.remove();
							}
						},10);
					}
					else if(grenade == null || grenade.isDead() || grenade.getTicksLived() > 10*20){
						if(grenade != null) grenade.remove();
						this.cancel();
					}
					else Particles.SNOW_SHOVEL.display(0.1f,0.1f,0.1f,0,4,grenade.getLocation().add(0,0.2,0),64);
				}
			};
			runnable.runTaskTimer(Games.getInstance(),1,1);
		}
	}

	@Override
	public void clear(){
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void PlayerInteractEvent(PlayerInteractEvent event){
		GamePlayer gPlayer = this.getGame().getGamePlayer(event.getPlayer());
		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
			ItemStack itemStack = gPlayer.getPlayer().getInventory().getItemInMainHand();
			if(itemStack.getType() == Material.EGG){
				event.setCancelled(true);
				this.activate(gPlayer);
			}
		}
	}

	private static ArrayList<Vector> directions = new ArrayList<Vector>();
	static {
		directions.add(new Vector(1, 0, 0));
		directions.add(new Vector(0, 1, 0));
		directions.add(new Vector(0, 0, 1));
		directions.add(new Vector(1, 1, 0));
        directions.add(new Vector(1, 0, 1));
        directions.add(new Vector(0, 1, 1));
        directions.add(new Vector(1, 1, 1));
        directions.add(new Vector(-1, 0, 0));
        directions.add(new Vector(0, 0, -1));
        directions.add(new Vector(-1, 0, -1));
        directions.add(new Vector(1, 0, -1));
        directions.add(new Vector(0, 1, -1));
        directions.add(new Vector(-1, 1, 0));
        directions.add(new Vector(-1, 0, 1));
        directions.add(new Vector(1, 1, -1));
        directions.add(new Vector(-1, 1, 1));
        directions.add(new Vector(-1, 1, -1));
        directions.add(new Vector(-1, -1, 1));
        directions.add(new Vector(-1, -1, -1));
        directions.add(new Vector(1, -1, -1));
        directions.add(new Vector(1, -1, 1));
        directions.add(new Vector(0, -1, 1));
        directions.add(new Vector(0, -1, -1));
        directions.add(new Vector(1, -1, 0));
        directions.add(new Vector(-1, -1, 0));
        directions.add(new Vector(0, -1, 0));
	}

	public static ArrayList<Vector> getDirections(){
		return directions;
	}
}