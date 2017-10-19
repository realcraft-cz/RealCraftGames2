package com.paintball;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.games.Games;
import com.games.events.GameCycleEvent;
import com.games.events.GameEndEvent;
import com.games.events.GamePlayerJoinEvent;
import com.games.events.GameStartEvent;
import com.games.events.GameStateChangeEvent;
import com.games.events.GameTimeoutEvent;
import com.games.game.GameState;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.Particles;
import com.games.utils.RandomUtil;
import com.games.utils.Title;
import com.realcraft.nicks.NickManager;

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
		game.loadLobbyInventory(event.getPlayer());
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
			team.setPlayerTeamInventory(gPlayer);
			team.setPlayerNickColor(gPlayer);
			game.setPlayerWeapons(gPlayer,true);
			gPlayer.getPlayer().teleport(team.getSpawnLocation());
			game.getScoreboard().updateForPlayer(gPlayer);
		}
	}

	@EventHandler
	public void GameEndEvent(GameEndEvent event){
		game.getTeams().resetTeams();
		for(GamePlayer gPlayer : game.getPlayers()){
			game.loadLobbyInventory(gPlayer);
			NickManager.clearPlayerNick(gPlayer.getPlayer());
		}
	}

	@EventHandler
	public void GameTimeoutEvent(GameTimeoutEvent event){
		game.setState(GameState.ENDING);
	}

	@EventHandler
	public void GameStateChangeEvent(GameStateChangeEvent event){
		if(game.getState() == GameState.ENDING){
		}
	}

	@EventHandler
	public void GameCycleEvent(GameCycleEvent event){
		if(game.getState() == GameState.INGAME){
			if(game.getPlayersCount() < 1) game.setState(GameState.ENDING);
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
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void PlayerRespawnEvent(PlayerRespawnEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		event.setRespawnLocation(game.getTeams().getPlayerTeam(gPlayer).getSpawnLocation());
		gPlayer.resetPlayer();
		game.getTeams().getPlayerTeam(gPlayer).setPlayerTeamInventory(gPlayer);
		game.setPlayerWeapons(gPlayer,true);
	}

	@EventHandler(ignoreCancelled=true)
	public void EntityDamageEvent(EntityDamageEvent event){
		if(event.getEntity() instanceof Player){
			if(event.getCause() == EntityDamageEvent.DamageCause.FALL){
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
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Snowball){
			Player player = (Player)event.getEntity();
			Player attacker = (Player)((Snowball)event.getDamager()).getShooter();
			if(game.getTeams().getPlayerTeam(game.getGamePlayer(player)) != game.getTeams().getPlayerTeam(game.getGamePlayer(attacker))){
				player.damage(100,attacker);
			} else {
				event.setCancelled(true);
			}
		}
		else if(event.getEntity() instanceof Player && event.getDamager() instanceof Player){
			event.setCancelled(true);
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
						for(int i=0;i<2;i++) Particles.REDSTONE.display(new Particles.OrdinaryColor(255,0,0),snowball.getLocation().add(RandomUtil.getRandomDouble(-0.2,0.2),RandomUtil.getRandomDouble(-0.2,0.2),RandomUtil.getRandomDouble(-0.2,0.2)),64);
					}
					else if(team.getType() == PaintballTeamType.BLUE){
						for(int i=0;i<2;i++) Particles.REDSTONE.display(new Particles.OrdinaryColor(0,0,255),snowball.getLocation().add(RandomUtil.getRandomDouble(-0.2,0.2),RandomUtil.getRandomDouble(-0.2,0.2),RandomUtil.getRandomDouble(-0.2,0.2)),64);
					}
				}
				snowball.getWorld().playSound(snowball.getLocation(),Sound.BLOCK_STONE_PLACE,1f,2f);
				snowball.remove();
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void PlayerInteractEvent(PlayerInteractEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		if(game.getGamePlayer(event.getPlayer()).getState() == GamePlayerState.SPECTATOR) return;
		if(game.getState().isLobby()){
			ItemStack itemStack = gPlayer.getPlayer().getInventory().getItemInMainHand();
			if(itemStack.getType() == Material.WOOL){
				if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
					event.setCancelled(true);
					if(itemStack.getDurability() == DyeColor.RED.getWoolData()){
						if(game.getTeams().getPlayerTeam(gPlayer) != game.getTeams().getTeam(PaintballTeamType.RED)){
							if(!game.getTeams().isTeamFull(PaintballTeamType.RED)){
								game.getTeams().setPlayerTeam(gPlayer,game.getTeams().getTeam(PaintballTeamType.RED));
								gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,1);
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
							} else {
								gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ITEM_BREAK,1f,1f);
								Title.showActionTitle(gPlayer.getPlayer(),"§c\u2716 §fTento tym je jiz plny §c\u2716",3*20);
							}
						}
					}
					game.loadLobbyInventory(gPlayer);
				}
			}
		} else {
			ItemStack itemStack = gPlayer.getPlayer().getInventory().getItemInMainHand();
			if(itemStack.getType() == Material.SNOW_BALL){
				if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
					event.setCancelled(true);
					Snowball snowball = (Snowball) gPlayer.getPlayer().getWorld().spawnEntity(gPlayer.getPlayer().getEyeLocation(),EntityType.SNOWBALL);
			        snowball.setShooter(gPlayer.getPlayer());
			        snowball.setVelocity(gPlayer.getPlayer().getLocation().getDirection().multiply(1.5));
			        game.getPlayer(gPlayer).addPistols(-1);
			        game.setPlayerWeapons(gPlayer,false,true);
				}
			}
		}
	}

	@EventHandler
	public void InventoryClickEvent(InventoryClickEvent event){
		event.setCancelled(true);
	}
}