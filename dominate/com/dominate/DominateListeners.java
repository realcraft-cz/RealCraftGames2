package com.dominate;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.dominate.DominateTeam.DominateTeamType;
import com.games.Games;
import com.games.events.GameCycleEvent;
import com.games.events.GameEndEvent;
import com.games.events.GamePlayerJoinEvent;
import com.games.events.GamePlayerLeaveEvent;
import com.games.events.GamePlayerStateChangeEvent;
import com.games.events.GameStartEvent;
import com.games.events.GameStateChangeEvent;
import com.games.events.GameTimeoutEvent;
import com.games.game.GameState;
import com.games.game.GameStats.GameStatsType;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.Title;

import realcraft.bukkit.playermanazer.PlayerManazer;

public class DominateListeners implements Listener {

	private Dominate game;

	public DominateListeners(Dominate game){
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this,Games.getInstance());
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Games.getInstance(),ListenerPriority.HIGH,PacketType.Play.Server.SPAWN_ENTITY){
			@Override
			public void onPacketSending(PacketEvent event){
				if(game.getState().isGame() && event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY){
					Entity entity = event.getPacket().getEntityModifier(event).read(0);
					if(entity != null && entity.getType() == EntityType.ARMOR_STAND){
						for(DominateKit kit : game.getArena().getKits()){
							if(kit.getStand() != null && kit.getStand().getEntityId() == entity.getEntityId()){
								GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
								if(!kit.isSpawnedForPlayer(gPlayer)) event.setCancelled(true);
								break;
							}
						}
					}
				}
			}
		});
	}

	public Dominate getGame(){
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
		game.getUser(event.getPlayer()).clear();
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
		game.getArena().reset();
		for(GamePlayer gPlayer : game.getPlayers()){
			gPlayer.resetPlayer();
			gPlayer.getSettings().setInt("kills",0);
			gPlayer.getSettings().setInt("deaths",0);
			gPlayer.getPlayer().setGameMode(GameMode.SURVIVAL);
			DominateTeam team = game.getTeams().getPlayerTeam(gPlayer);
			team.setPlayerInventory(gPlayer);
			gPlayer.getPlayer().teleport(team.getSpawnLocation());
			game.getScoreboard().updateForPlayer(gPlayer);
			game.getUser(gPlayer).respawn();
		}
	}

	@EventHandler
	public void GameEndEvent(GameEndEvent event){
		game.getTeams().resetTeams();
		game.getArena().reset();
		for(GamePlayer gPlayer : game.getPlayers()){
			game.getScoreboard().updateForPlayer(gPlayer);
			game.loadLobbyInventory(gPlayer);
			game.getUser(gPlayer).clear();
		}
	}

	@EventHandler
	public void GameTimeoutEvent(GameTimeoutEvent event){
		game.setState(GameState.ENDING);
	}

	@EventHandler
	public void GameStateChangeEvent(GameStateChangeEvent event){
		if(game.getState() == GameState.ENDING){
			DominateTeam winner = game.getTeams().getWinnerTeam();
			if(winner != null){
				game.sendMessage(winner.getType().getChatColor()+winner.getType().toName()+" §fvyhrali tuto hru");
				for(GamePlayer gPlayer : game.getPlayers()){
					gPlayer.getPlayer().getInventory().clear();
					if(gPlayer.getState() != GamePlayerState.SPECTATOR && game.getTeams().getPlayerTeam(gPlayer).getType() == winner.getType()){
						if(winner.getPoints() > DominateTeams.WIN_SCORE/2){
							int kdreward = (game.getConfig().getInt("reward.kill",0)*gPlayer.getSettings().getInt("kills"))-(game.getConfig().getInt("reward.death",0)*gPlayer.getSettings().getInt("deaths"));
							if(kdreward < 0) kdreward = 0;
							final int reward = PlayerManazer.getPlayerInfo(gPlayer.getPlayer()).giveCoins(
								(game.getConfig().getInt("reward.base",0))+kdreward
							);

							game.getStats().addScore(gPlayer,GameStatsType.WINS,1);
							if(gPlayer.getSettings().getInt("kills") > 0) game.getStats().addScore(gPlayer,GameStatsType.KILLS,gPlayer.getSettings().getInt("kills"));
							if(gPlayer.getSettings().getInt("deaths") > 0) game.getStats().addScore(gPlayer,GameStatsType.DEATHS,gPlayer.getSettings().getInt("deaths"));

							Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
								public void run(){
									PlayerManazer.getPlayerInfo(gPlayer.getPlayer()).runCoinsEffect("§a§lVitezstvi!",reward);
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
	}

	@EventHandler
	public void GameCycleEvent(GameCycleEvent event){
		if(game.getState() == GameState.INGAME){
			if(game.getTeams().getActiveTeams().size() < 2 || game.getTeams().getTeam(DominateTeamType.RED).getPoints() >= DominateTeams.WIN_SCORE || game.getTeams().getTeam(DominateTeamType.BLUE).getPoints() >= DominateTeams.WIN_SCORE) game.setState(GameState.ENDING);
			for(DominateEmerald emerald : game.getArena().getEmeralds()){
				emerald.run();
			}
			for(DominateKit kit : game.getArena().getKits()){
				kit.run();
			}
			for(GamePlayer gPlayer : game.getPlayers()){
				for(DominateKit kit : game.getArena().getKits()){
					if(kit.getStand() == null) continue;
					if(gPlayer.getPlayer().getLocation().distanceSquared(kit.getStand().getLocation()) > 16*16) kit.despawnForPlayer(gPlayer);
					else kit.spawnForPlayer(gPlayer);
				}
			}
		}
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
			game.sendMessage("§c\u271E §b"+killer.getName()+" §7zabil hrace §b"+player.getName());
		}
		else game.sendMessage("§c\u271E §b"+player.getName()+" §7zemrel");
	}

	@EventHandler
	public void PlayerRespawnEvent(PlayerRespawnEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		event.setRespawnLocation(game.getTeams().getPlayerTeam(gPlayer).getSpawnLocation());
		gPlayer.resetPlayer();
		game.getUser(gPlayer).respawn();
	}

	@EventHandler(ignoreCancelled=true)
	public void EntityDamageEvent(EntityDamageEvent event){
		if(event.getEntity() instanceof Player && event.getCause() == DamageCause.FALL && game.getUser(game.getGamePlayer((Player)event.getEntity())).getLastInSpawn()+2000 > System.currentTimeMillis()){
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event){
		if(event.getDamager() instanceof Firework){
			event.setCancelled(true);
		}
		else if(event.getEntity() instanceof Player){
			DominateTeam team = game.getTeams().getPlayerTeam(game.getGamePlayer((Player)event.getEntity()));
			if(team != null && team.isLocationInSpawn(event.getEntity().getLocation())){
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void EntityRegainHealthEvent(EntityRegainHealthEvent event){
		if(event.getRegainReason() == RegainReason.SATIATED){
			event.setCancelled(true);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
    public void PlayerPickupArrowEvent(PlayerPickupArrowEvent event){
		event.setCancelled(true);
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
						if(game.getTeams().getPlayerTeam(gPlayer) != game.getTeams().getTeam(DominateTeamType.RED)){
							if(!game.getTeams().isTeamFull(DominateTeamType.RED)){
								game.getTeams().setPlayerTeam(gPlayer,game.getTeams().getTeam(DominateTeamType.RED));
								gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,1);
								game.loadLobbyInventories();
							} else {
								gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ITEM_BREAK,1f,1f);
								Title.showActionTitle(gPlayer.getPlayer(),"§c\u2716 §fTento tym je jiz plny §c\u2716",3*20);
							}
						}
					} else {
						if(game.getTeams().getPlayerTeam(gPlayer) != game.getTeams().getTeam(DominateTeamType.BLUE)){
							if(!game.getTeams().isTeamFull(DominateTeamType.BLUE)){
								game.getTeams().setPlayerTeam(gPlayer,game.getTeams().getTeam(DominateTeamType.BLUE));
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
		if(game.getState().isLobby()) event.setCancelled(true);
	}

	@EventHandler
	public void ChunkUnloadEvent(ChunkUnloadEvent event){
		if(game.getState().isLobby()) return;
		Location location = new Location(event.getWorld(),event.getChunk().getX()*16,game.getArena().getMinLocation().getY(),event.getChunk().getZ()*16);
		if(game.getArena().isLocationInArena(location)){
			event.setCancelled(true);
		}
	}
}