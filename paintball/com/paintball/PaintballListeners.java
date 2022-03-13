package com.paintball;

import com.games.Games;
import com.games.events.*;
import com.games.game.GameState;
import com.games.game.GameStats.GameStatsType;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.RandomUtil;
import realcraft.bukkit.utils.Title;
import com.paintball.PaintballTeam.PaintballTeamType;
import com.paintball.specials.PaintballSpecial;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import realcraft.bukkit.RealCraft;
import realcraft.bukkit.coins.Coins;
import realcraft.bukkit.users.Users;
import realcraft.bukkit.utils.MaterialUtil;
import realcraft.bukkit.utils.Particles;

public class PaintballListeners implements Listener {

	private Paintball game;

	public PaintballListeners(Paintball game){
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this,Games.getInstance());
	}

	public Paintball getGame(){
		return game;
	}

	@EventHandler
	public void GamePlayerJoinEvent(GamePlayerJoinEvent event){
		if(game.getState().isLobby()) game.loadLobbyInventory(event.getPlayer());
		else game.getScoreboard().updateForPlayer(event.getPlayer());
	}

	@EventHandler
	public void GamePlayerLeaveEvent(GamePlayerLeaveEvent event){
		if(game.getTeams().getPlayerTeam(event.getPlayer()) != null){
			game.getTeams().getPlayerTeam(event.getPlayer()).removePlayer(event.getPlayer());
		}
		if(event.getPlayer().getSettings().getInt("kills") > 0) game.getStats().addScore(event.getPlayer(),GameStatsType.KILLS,event.getPlayer().getSettings().getInt("kills"));
		if(event.getPlayer().getSettings().getInt("deaths") > 0) game.getStats().addScore(event.getPlayer(),GameStatsType.DEATHS,event.getPlayer().getSettings().getInt("deaths"));
	}

	@EventHandler
	public void GamePlayerStateChangeEvent(GamePlayerStateChangeEvent event){
		if(game.getState().isGame()) game.getScoreboard().updateForPlayer(event.getPlayer());
	}

	@EventHandler
	public void GameStartEvent(GameStartEvent event){
		game.getTeams().autoBalancingTeams();
		for(GamePlayer gPlayer : game.getPlayers()){
			gPlayer.resetPlayer();
			gPlayer.getSettings().setInt("kills",0);
			gPlayer.getSettings().setInt("deaths",0);
			gPlayer.getPlayer().setGameMode(GameMode.ADVENTURE);
			PaintballTeam team = game.getTeams().getPlayerTeam(gPlayer);
			team.setPlayerInventory(gPlayer);
			game.getUser(gPlayer).reset();
			game.setPlayerWeapons(gPlayer,true);
			gPlayer.getPlayer().teleport(team.getSpawnLocation());
			game.getScoreboard().updateForPlayer(gPlayer);
		}
		for(PaintballSpecial special : game.getArena().getSpecials()){
			special.setEnabled(true);
		}
	}

	@EventHandler
	public void GameEndEvent(GameEndEvent event){
		game.getTeams().resetTeams();
		game.getDrops().clear();
		for(GamePlayer gPlayer : game.getPlayers()){
			game.getScoreboard().updateForPlayer(gPlayer);
			game.loadLobbyInventory(gPlayer);
		}
		for(PaintballSpecial special : game.getArena().getSpecials()){
			special.setEnabled(false);
		}
	}

	@EventHandler
	public void GameTimeoutEvent(GameTimeoutEvent event){
		game.setState(GameState.ENDING);
	}

	@EventHandler
	public void GameStateChangeEvent(GameStateChangeEvent event){
		if(game.getState() == GameState.ENDING){
			PaintballTeam winner = game.getTeams().getWinnerTeam();
			game.sendMessage(winner.getType().getChatColor()+winner.getType().toName()+" §fvyhrali tuto hru");
			for(GamePlayer gPlayer : game.getPlayers()){
				gPlayer.getPlayer().getInventory().clear();
				if(gPlayer.getState() != GamePlayerState.SPECTATOR && game.getTeams().getPlayerTeam(gPlayer).getType() == winner.getType()){
					if(game.getGameTime() < game.getGameTimeDefault()-60){
						int kdreward = (game.getConfig().getInt("reward.kill",0)*gPlayer.getSettings().getInt("kills"))-(game.getConfig().getInt("reward.death",0)*gPlayer.getSettings().getInt("deaths"));
						if(kdreward < 0) kdreward = 0;
						final int reward = Users.getUser(gPlayer.getPlayer()).giveCoins(
							(game.getConfig().getInt("reward.base",0))+kdreward
						);

						game.getStats().addScore(gPlayer,GameStatsType.WINS,1);
						if(gPlayer.getSettings().getInt("kills") > 0) game.getStats().addScore(gPlayer,GameStatsType.KILLS,gPlayer.getSettings().getInt("kills"));
						if(gPlayer.getSettings().getInt("deaths") > 0) game.getStats().addScore(gPlayer,GameStatsType.DEATHS,gPlayer.getSettings().getInt("deaths"));

						Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
							public void run(){
								Coins.runCoinsEffect(gPlayer.getPlayer(),"§a§lVitezstvi!",reward);
							}
						},10*20);
					}
					Title.showTitle(gPlayer.getPlayer(),"§a§lVitezstvi!",0.5,8,0.5);
					Title.showSubTitle(gPlayer.getPlayer(),"§fTvuj tym vyhral tuto hru",0.5,8,0.5);
				} else {
					if(gPlayer.getSettings().getInt("kills") > 0) game.getStats().addScore(gPlayer,GameStatsType.KILLS,gPlayer.getSettings().getInt("kills"));
					Title.showTitle(gPlayer.getPlayer(),"§c§lProhra",0.5,8,0.5);
					Title.showSubTitle(gPlayer.getPlayer(),winner.getType().getChatColor()+winner.getType().toName()+" §fvyhrali tuto hru",0.5,8,0.5);
				}
				gPlayer.getSettings().setInt("kills",0);
				gPlayer.getSettings().setInt("deaths",0);
			}
		}
	}

	@EventHandler
	public void GameCycleEvent(GameCycleEvent event){
		if(game.getState() == GameState.INGAME){
			if(game.getTeams().getActiveTeams().size() < 2 && !RealCraft.isTestServer()) game.setState(GameState.ENDING);
			for(GamePlayer gPlayer : game.getPlayers()){
				if(gPlayer.getState() == GamePlayerState.SPECTATOR) continue;
				PaintballUser user = game.getUser(gPlayer);
				user.addPistols(1);
				game.setPlayerWeapons(gPlayer);
			}
			if(game.getGameTime()%15 == 0){
				game.getDrops().addDrop();
			}
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
			Title.showActionTitle(player,ChatColor.GRAY+"Hrac "+ChatColor.AQUA+killer.getName()+ChatColor.GRAY+" te zabil");

			game.getTeams().getPlayerTeam(game.getGamePlayer(killer)).addKill();
		}
	}

	@EventHandler
	public void PlayerRespawnEvent(PlayerRespawnEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		event.setRespawnLocation(game.getTeams().getPlayerTeam(gPlayer).getSpawnLocation());
		gPlayer.resetPlayer();
		game.getTeams().getPlayerTeam(gPlayer).setPlayerInventory(gPlayer);
		game.getUser(gPlayer).resetPistols();
		game.setPlayerWeapons(gPlayer,true);
	}

	@EventHandler(ignoreCancelled=true)
	public void EntityDamageEvent(EntityDamageEvent event){
		if(event.getEntity() instanceof Player){
			if(event.getCause() == DamageCause.FALL){
				event.setCancelled(true);
			}
			else if(event.getCause() == DamageCause.LAVA || event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK){
				event.getEntity().setFireTicks(0);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event){
		if(event.getDamager() instanceof Firework){
			event.setCancelled(true);
		}
		else if(event.getEntity() instanceof Player && event.getDamager() instanceof Snowball){
			Player player = (Player)event.getEntity();
			Player attacker = (Player)((Snowball)event.getDamager()).getShooter();
			if(game.getTeams().getPlayerTeam(game.getGamePlayer(player)) != game.getTeams().getPlayerTeam(game.getGamePlayer(attacker))){
				event.setCancelled(true);
				player.damage(100,attacker);
			} else {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void ProjectileLaunchEvent(ProjectileLaunchEvent event){
		if(event.getEntity() instanceof Snowball){
			if(event.getEntity().getShooter() instanceof Player){
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void ProjectileHitEvent(ProjectileHitEvent event){
		if(event.getEntity() instanceof Snowball){
			final Snowball snowball = (Snowball)event.getEntity();
			if(snowball.getShooter() instanceof Player){
				Player player = (Player)snowball.getShooter();
				GamePlayer gPlayer = game.getGamePlayer(player);
				PaintballTeam team = game.getTeams().getPlayerTeam(gPlayer);
				if(team != null){
					if(team.getType() == PaintballTeamType.RED){
						for(int i=0;i<2;i++) Particles.REDSTONE.display(new Particle.DustOptions(Color.RED,1f),new Vector(0,0,0),0f,snowball.getLocation().add(RandomUtil.getRandomDouble(-0.2,0.2),RandomUtil.getRandomDouble(-0.2,0.2),RandomUtil.getRandomDouble(-0.2,0.2)),64);
					}
					else if(team.getType() == PaintballTeamType.BLUE){
						for(int i=0;i<2;i++) Particles.REDSTONE.display(new Particle.DustOptions(Color.BLUE,1f),new Vector(0,0,0),0f,snowball.getLocation().add(RandomUtil.getRandomDouble(-0.2,0.2),RandomUtil.getRandomDouble(-0.2,0.2),RandomUtil.getRandomDouble(-0.2,0.2)),64);
					}
				}
				snowball.getWorld().playSound(snowball.getLocation(),Sound.BLOCK_STONE_PLACE,1f,2f);
				snowball.remove();
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGH,ignoreCancelled=true)
	public void PlayerInteractEvent(PlayerInteractEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		if(gPlayer.getState() == GamePlayerState.SPECTATOR) return;
		if(game.getState().isGame()){
			ItemStack itemStack = gPlayer.getPlayer().getInventory().getItemInMainHand();
			if(itemStack.getType() == Material.SNOWBALL){
				if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
					event.setCancelled(true);
					Snowball snowball = (Snowball) gPlayer.getPlayer().getWorld().spawnEntity(gPlayer.getPlayer().getEyeLocation(),EntityType.SNOWBALL);
			        snowball.setShooter(gPlayer.getPlayer());
			        snowball.setVelocity(gPlayer.getPlayer().getLocation().getDirection().multiply(1.5));
			        game.getUser(gPlayer).addPistols(-1);
			        game.setPlayerWeapons(gPlayer,false);
				}
			}
		}
	}

	@EventHandler
	public void PlayerInteractEvent2(PlayerInteractEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		if(gPlayer.getState() == GamePlayerState.SPECTATOR) return;
		if(game.getState().isLobby()){
			ItemStack itemStack = gPlayer.getPlayer().getInventory().getItemInMainHand();
			if(MaterialUtil.isWool(itemStack.getType())){
				if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
					event.setCancelled(true);
					if(MaterialUtil.getDyeColor(itemStack.getType()) == DyeColor.RED){
						if(game.getTeams().getPlayerTeam(gPlayer) != game.getTeams().getTeam(PaintballTeamType.RED)){
							if(!game.getTeams().isTeamFull(PaintballTeamType.RED)){
								game.getTeams().setPlayerTeam(gPlayer,game.getTeams().getTeam(PaintballTeamType.RED));
								gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,1);
								game.loadLobbyInventories();
							} else {
								gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ITEM_BREAK,1f,1f);
								Title.showActionTitle(gPlayer.getPlayer(),"§c\u2716 §fTento tym je jiz plny §c\u2716",3*20);
							}
						}
					} else {
						if(game.getTeams().getPlayerTeam(gPlayer) != game.getTeams().getTeam(PaintballTeamType.BLUE)){
							if(!game.getTeams().isTeamFull(PaintballTeamType.BLUE)){
								game.getTeams().setPlayerTeam(gPlayer,game.getTeams().getTeam(PaintballTeamType.BLUE));
								gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,1);
								game.loadLobbyInventories();
							} else {
								gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ITEM_BREAK,1f,1f);
								Title.showActionTitle(gPlayer.getPlayer(),"§c\u2716 §fTento tym je jiz plny §c\u2716",3*20);
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void InventoryClickEvent(InventoryClickEvent event){
		event.setCancelled(true);
	}
}