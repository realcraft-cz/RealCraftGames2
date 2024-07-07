package com.ragemode;

import com.games.Games;
import com.games.events.*;
import com.games.game.GameState;
import com.games.game.GameStats.GameStatsType;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import realcraft.bukkit.utils.Title;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import realcraft.bukkit.RealCraft;
import realcraft.bukkit.coins.Coins;
import realcraft.bukkit.users.Users;
import realcraft.bukkit.utils.Particles;

import java.util.ArrayList;
import java.util.List;

public class RageModeListeners implements Listener {

	private RageMode game;

	public RageModeListeners(RageMode game){
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this,Games.getInstance());
	}

	public RageMode getGame(){
		return game;
	}

	@EventHandler
	public void GamePlayerJoinEvent(GamePlayerJoinEvent event){
		if(game.getState().isGame()) game.getScoreboard().updateForPlayer(event.getPlayer());
	}

	@EventHandler
	public void GamePlayerLeaveEvent(GamePlayerLeaveEvent event){
		if(event.getPlayer().getSettings().getInt("kills") > 0) game.getStats().addScore(event.getPlayer(),GameStatsType.KILLS,event.getPlayer().getSettings().getInt("kills"));
		if(event.getPlayer().getSettings().getInt("deaths") > 0) game.getStats().addScore(event.getPlayer(),GameStatsType.DEATHS,event.getPlayer().getSettings().getInt("deaths"));
	}

	@EventHandler
	public void GamePlayerStateChangeEvent(GamePlayerStateChangeEvent event){
		if(game.getState().isGame()) game.getScoreboard().updateForPlayer(event.getPlayer());
	}

	@EventHandler
	public void GameStartEvent(GameStartEvent event){
		for(GamePlayer gPlayer : game.getPlayers()){
			gPlayer.resetPlayer();
			gPlayer.getSettings().setInt("kills",0);
			gPlayer.getSettings().setInt("deaths",0);
			gPlayer.getPlayer().teleport(game.getArena().getRandomSpawn());
			gPlayer.getPlayer().setGameMode(GameMode.ADVENTURE);
			game.loadInventory(gPlayer);
			game.getScoreboard().updateForPlayer(gPlayer);
		}
	}

	@EventHandler
	public void GameEndEvent(GameEndEvent event){
		for(GamePlayer gPlayer : game.getPlayers()){
			game.getScoreboard().updateForPlayer(gPlayer);
		}
	}

	@EventHandler
	public void GameTimeoutEvent(GameTimeoutEvent event){
		game.setState(GameState.ENDING);
	}

	@EventHandler
	public void GameStateChangeEvent(GameStateChangeEvent event){
		if(game.getState() == GameState.ENDING){
			GamePlayer winner = game.getWinner();
			for(GamePlayer gPlayer : game.getPlayers()){
				gPlayer.getPlayer().getInventory().clear();
				if(gPlayer == winner){
					int kdreward = (game.getConfig().getInt("reward.kill",0)*gPlayer.getSettings().getInt("kills"))-(game.getConfig().getInt("reward.death",0)*gPlayer.getSettings().getInt("deaths"));
					if(kdreward < 0) kdreward = 0;
					final int reward = Users.getUser(gPlayer.getPlayer()).giveCoins(
						(game.getConfig().getInt("reward.base",0))+
						kdreward+
						(game.getConfig().getInt("reward.player",0)*game.getStartPlayers())
					);

					game.sendMessage("§b"+gPlayer.getPlayer().getName()+" §fvyhral a ziskava §a+"+reward+" coins");
					game.getStats().addScore(gPlayer,GameStatsType.WINS,1);

					Title.showTitle(gPlayer.getPlayer(),"§a§lVitezstvi!",0.5,8,0.5);
					Title.showSubTitle(gPlayer.getPlayer(),"§fVyhral jsi tuto hru",0.5,8,0.5);
					Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
						public void run(){
							Coins.runCoinsEffect(gPlayer.getPlayer(),"§a§lVitezstvi!",reward);
						}
					},10*20);
				} else {
					Title.showTitle(gPlayer.getPlayer(),"§c§lProhra",0.5,8,0.5);
					Title.showSubTitle(gPlayer.getPlayer(),"§b"+winner.getPlayer().getName()+" §fvyhral tuto hru",0.5,8,0.5);
				}
				if(gPlayer.getSettings().getInt("kills") > 0) game.getStats().addScore(gPlayer,GameStatsType.KILLS,gPlayer.getSettings().getInt("kills"));
				if(gPlayer.getSettings().getInt("deaths") > 0) game.getStats().addScore(gPlayer,GameStatsType.DEATHS,gPlayer.getSettings().getInt("deaths"));
				gPlayer.getSettings().setInt("kills",0);
				gPlayer.getSettings().setInt("deaths",0);
			}
		}
	}

	@EventHandler
	public void GameCycleEvent(GameCycleEvent event){
		if(game.getState() == GameState.INGAME){
			if(game.getPlayersCount() < 2 && !RealCraft.isTestServer()) game.setState(GameState.ENDING);
		}
		game.getScoreboard().update();
	}

	@EventHandler(ignoreCancelled=true)
	public void PlayerDeathEvent(PlayerDeathEvent event){
		event.setDeathMessage(null);
		Player player = event.getEntity();
		Player killer = player.getKiller();

		player.getInventory().clear();
		game.getGamePlayer(event.getEntity()).getSettings().addInt("deaths",1);

		if(killer != null && killer != player){
			game.getGamePlayer(killer).getSettings().addInt("kills",1);
			killer.playSound(killer.getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1f,1f);

			Title.showActionTitle(killer,ChatColor.GRAY+"Zabil jsi hrace "+ChatColor.AQUA+player.getName());
			Title.showActionTitle(player,ChatColor.DARK_RED+"Hrac "+ChatColor.AQUA+killer.getName()+ChatColor.DARK_RED+" te zabil");

			if(game.getGamePlayer(killer).getSettings().getInt("kills") >= RageMode.WINSCORE){
				game.setState(GameState.ENDING);
			}
		}
	}

	@EventHandler
	public void PlayerRespawnEvent(PlayerRespawnEvent event){
		event.setRespawnLocation(game.getArena().getRandomSpawn());
		game.getGamePlayer(event.getPlayer()).resetPlayer();
		game.loadInventory(game.getGamePlayer(event.getPlayer()));
	}

	@EventHandler(ignoreCancelled=true)
	public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event){
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Arrow){
			Arrow arrow = (Arrow)event.getDamager();
			if(arrow.getShooter() instanceof Player){
				Player player = (Player)event.getEntity();
				Player killer = (Player)arrow.getShooter();
				if(player != killer){
					event.setDamage(100);
				}
			}
		}
		else if(event.getEntity() instanceof Player && event.getDamager() instanceof Snowball){
			Player player = (Player)event.getEntity();
			player.setGlowing(true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,60,1),true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,40,1),true);
			Bukkit.getScheduler().scheduleSyncDelayedTask(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					player.setGlowing(false);
				}
            },50);
		}
		else if(event.getEntity() instanceof Player && event.getDamager() instanceof Player){
			Player player = (Player)event.getEntity();
			Player killer = (Player)event.getDamager();
			if(event.getCause() == DamageCause.ENTITY_ATTACK && killer.getInventory().getItemInMainHand().getType() == Material.IRON_AXE){
				player.damage(100);
			}
		}
	}

	@EventHandler
	public void PlayerInteractEvent(PlayerInteractEvent event){
		Player player = event.getPlayer();
		if(game.getState().isLobby() || game.getGamePlayer(event.getPlayer()).getState() == GamePlayerState.SPECTATOR) return;
		ItemStack itemStack = player.getInventory().getItemInMainHand();
		if(itemStack.getType() == Material.IRON_AXE){
			if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
				GamePlayer gamePlayer = game.getGamePlayer(player);
				if(gamePlayer != null){
					final Item item = player.getWorld().dropItem(player.getEyeLocation(),itemStack);
					item.setPickupDelay(1);
					item.setCustomName(player.getName());
					item.setCustomNameVisible(false);
					item.setVelocity(player.getEyeLocation().getDirection().multiply(1.4));
					player.getInventory().setItemInMainHand(null);
					Bukkit.getScheduler().scheduleSyncDelayedTask(Games.getInstance(),new Runnable(){
						@Override
						public void run(){
							item.remove();
						}
					},40);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
    public void EntityPickupItemEvent(EntityPickupItemEvent event){
		if(event.getEntity() instanceof Player){
			Player player = (Player)event.getEntity();
			Item item = event.getItem();
			if(item.getItemStack().getType() == Material.IRON_AXE && item.getTicksLived() <= 40){
				event.setCancelled(true);
				if(!item.getVelocity().equals(new Vector(0,0,0))){
					if(player.getName().equalsIgnoreCase(item.getCustomName()) == false){
						player.playSound(player.getLocation(),Sound.ENTITY_PLAYER_HURT,1,1);
						player.damage(100,Bukkit.getPlayer(item.getCustomName()));
					}
				}
			}
			else event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void ProjectileHitEvent(ProjectileHitEvent event){
		if(event.getEntity() instanceof Arrow){
			Arrow arrow = (Arrow) event.getEntity();
			if(arrow.getShooter() instanceof Player){
				Player player = (Player) arrow.getShooter();
				List<Entity> entities = arrow.getNearbyEntities(4.0,4.0,4.0);
				for(Entity entity : entities){
                    if(!(entity instanceof Player)) continue;
                    if(game.getGamePlayer((Player)entity).getState() == GamePlayerState.SPECTATOR) continue;
                    if(((Player)entity).getName().equalsIgnoreCase(player.getName()) == false){
                    	((Player)entity).damage(0,player);
                    }
				}
				final Location location = arrow.getLocation().clone();
				Bukkit.getScheduler().scheduleSyncDelayedTask(Games.getInstance(),new Runnable(){
					@Override
					public void run(){
						arrow.remove();
						location.getWorld().createExplosion(arrow.getLocation().getX(),arrow.getLocation().getY(),arrow.getLocation().getZ(),1.9f,false,false);
					}
				},0);
				location.getWorld().playEffect(location,Effect.SMOKE,0);
				location.getWorld().playEffect(location,Effect.SMOKE,2);
				location.getWorld().playEffect(location,Effect.SMOKE,4);
				location.getWorld().playEffect(location,Effect.SMOKE,6);
				location.getWorld().playEffect(location,Effect.SMOKE,8);
				location.add(0,1,0);
				location.getWorld().playEffect(location,Effect.SMOKE,1);
				location.getWorld().playEffect(location,Effect.SMOKE,3);
				location.getWorld().playEffect(location,Effect.SMOKE,5);
				location.getWorld().playEffect(location,Effect.SMOKE,7);
				location.add(0,-1,0);
				Particles.LAVA.display(0f,0f,0f,0f,8,location,64);
			}
		}
		else if(event.getEntity() instanceof Egg){
			Egg grenade = (Egg) event.getEntity();
			if(grenade.getShooter() instanceof Player){
				Player player = (Player) grenade.getShooter();
				Location location = grenade.getLocation();
				grenade.remove();
				location.getWorld().createExplosion(location,0.0f,false);
				Particles.FIREWORKS_SPARK.display(0f,0f,0f,0f,16,location,64);
				List<Entity> entities = grenade.getNearbyEntities(2.0,2.0,2.0);
                for(final Entity entity : entities){
                    if(!(entity instanceof Player)) continue;
                    if(game.getGamePlayer((Player)entity).getState() == GamePlayerState.SPECTATOR) continue;
                    ((Player)entity).setGlowing(true);
                    ((Player)entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,60,1),true);
                    ((Player)entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,40,1),true);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Games.getInstance(),new Runnable(){
						@Override
						public void run(){
							((Player)entity).setGlowing(false);
						}
                    },50);
                }

                for(Vector v : this.getDirections()){
                    Snowball snowball = (Snowball)location.getWorld().spawnEntity(location,EntityType.SNOWBALL);
                    snowball.setShooter(player);
                    Vector v2 = v.clone();
                    snowball.setVelocity(v2.normalize().multiply(0.6));
                }
                for(int i=0;i<2;i++){
	                for(Vector v : this.getDirections()){
	                    Snowball snowball = (Snowball)location.getWorld().spawnEntity(location,EntityType.SNOWBALL);
	                    snowball.setShooter(player);
	                    Vector v2 = v.clone();
	                    v2.setX(v.getX() + Math.random() - Math.random());
	                    v2.setY(v.getY() + Math.random() - Math.random());
	                    v2.setZ(v.getZ() + Math.random() - Math.random());
	                    snowball.setVelocity(v2.normalize().multiply(0.6));
	                }
                }
			}
		}
	}

	public ArrayList<Vector> getDirections(){
		ArrayList<Vector> vectors = new ArrayList<Vector>();
		vectors.add(new Vector(1, 0, 0));
		vectors.add(new Vector(0, 1, 0));
		vectors.add(new Vector(0, 0, 1));
		vectors.add(new Vector(1, 1, 0));
        vectors.add(new Vector(1, 0, 1));
        vectors.add(new Vector(0, 1, 1));
        vectors.add(new Vector(1, 1, 1));
        vectors.add(new Vector(-1, 0, 0));
        vectors.add(new Vector(0, 0, -1));
        vectors.add(new Vector(-1, 0, -1));
        vectors.add(new Vector(1, 0, -1));
        vectors.add(new Vector(0, 1, -1));
        vectors.add(new Vector(-1, 1, 0));
        vectors.add(new Vector(-1, 0, 1));
        vectors.add(new Vector(1, 1, -1));
        vectors.add(new Vector(-1, 1, 1));
        vectors.add(new Vector(-1, 1, -1));
        vectors.add(new Vector(-1, -1, 1));
        vectors.add(new Vector(-1, -1, -1));
        vectors.add(new Vector(1, -1, -1));
        vectors.add(new Vector(1, -1, 1));
        vectors.add(new Vector(0, -1, 1));
        vectors.add(new Vector(0, -1, -1));
        vectors.add(new Vector(1, -1, 0));
        vectors.add(new Vector(-1, -1, 0));
        vectors.add(new Vector(0, -1, 0));
		return vectors;
	}
}