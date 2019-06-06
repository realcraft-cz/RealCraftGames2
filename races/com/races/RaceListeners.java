package com.races;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.games.Games;
import com.games.events.*;
import com.games.game.GameState;
import com.games.game.GameStats.GameStatsType;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.FireworkUtil;
import com.games.utils.Title;
import com.races.RaceCheckpoint.RaceCheckpointType;
import com.races.arenas.RaceArena.RaceBarrier;
import net.minecraft.server.v1_13_R2.PacketPlayInSteerVehicle;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import realcraft.bukkit.RealCraft;
import realcraft.bukkit.coins.Coins;
import realcraft.bukkit.users.Users;
import realcraft.bukkit.utils.RandomUtil;

import java.util.ArrayList;
import java.util.Collections;

public class RaceListeners implements Listener {

	private Races game;

	public RaceListeners(Races game){
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this,Games.getInstance());
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(RealCraft.getInstance(),PacketType.Play.Client.STEER_VEHICLE){
			@Override
			public void onPacketReceiving(PacketEvent event){
				if(event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE){
					PacketPlayInSteerVehicle packet = (PacketPlayInSteerVehicle) event.getPacket().getHandle();
					GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
					if(packet.d() && gPlayer.getState() != GamePlayerState.SPECTATOR) event.setCancelled(true);
					if(Math.abs(packet.b()) > 0.0001 || Math.abs(packet.c()) > 0.0001) game.getUser(game.getGamePlayer(event.getPlayer())).setLastKeyAction();
				}
			}
		});
	}

	public Races getGame(){
		return game;
	}

	@EventHandler
	public void GamePlayerJoinEvent(GamePlayerJoinEvent event){
		game.getScoreboard().updateForPlayer(event.getPlayer());
	}

	@EventHandler
	public void GamePlayerLeaveEvent(GamePlayerLeaveEvent event){
		game.getUser(event.getPlayer()).clear();
	}

	@EventHandler
	public void GamePlayerStateChangeEvent(GamePlayerStateChangeEvent event){
		if(game.getState().isGame()) game.getScoreboard().updateForPlayer(event.getPlayer());
	}

	@EventHandler
	public void GameStartEvent(GameStartEvent event){
		game.setCountdown(Races.COUNTDOWN);
		game.clearWinners();
		ArrayList<GamePlayer> players = new ArrayList<GamePlayer>(game.getPlayers());
		Collections.shuffle(players);
		int index = 0;
		for(GamePlayer gPlayer : players){
			gPlayer.resetPlayer();
			game.getUser(gPlayer).clear();
			gPlayer.getPlayer().setGameMode(GameMode.ADVENTURE);
			game.getScoreboard().updateForPlayer(gPlayer);
			if(index < game.getArena().getSpawns().size()){
				game.getUser(gPlayer).setSpawn(game.getArena().getSpawns().get(index++));
				gPlayer.getPlayer().teleport(game.getUser(gPlayer).getSpawn());
				Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
					@Override
					public void run(){
						gPlayer.getPlayer().teleport(game.getUser(gPlayer).getSpawn());
						game.getUser(gPlayer).respawn();
					}
				},5);
			} else {
				gPlayer.setState(GamePlayerState.SPECTATOR);
				Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
					@Override
					public void run(){
						gPlayer.teleportToSpectatorLocation();
						gPlayer.toggleSpectator();
					}
				},5);
			}
		}
	}

	@EventHandler
	public void GameEndEvent(GameEndEvent event){
		game.clearWinners();
		for(GamePlayer gPlayer : game.getPlayers()){
			game.getScoreboard().updateForPlayer(gPlayer);
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
			for(GamePlayer gPlayer : game.getPlayers()){
				gPlayer.getPlayer().getInventory().clear();
				if(game.getWinner(gPlayer) != null && game.getWinner(gPlayer).getPosition() <= 3){
					game.getStats().addScore(gPlayer,GameStatsType.WINS,1);
				}
			}
		}
	}

	@EventHandler
	public void GameCycleEvent(GameCycleEvent event){
		if(game.getState() == GameState.INGAME){
			if(game.getPlayersCount() < 1 && game.getGameTime() > 5 && !RealCraft.isTestServer()) game.setGameTime(5);
			if(game.getCountdown() > 0){
				game.resetGameTime();
				game.setCountdown(game.getCountdown()-1);
				if(game.getCountdown() > 0 && game.getCountdown() <= 5){
					for(GamePlayer gPlayer : game.getPlayers()){
						Title.showTitle(gPlayer.getPlayer(),Races.NUMBERS[game.getCountdown()-1],0,1.2,0);
						gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_BELL,1f,1f);
						gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_FLUTE,1f,1f);
					}
				}
				else if(game.getCountdown() == 0){
					game.setCountdown(-1);
					for(GamePlayer gPlayer : game.getPlayers()){
						game.getUser(gPlayer).setLastKeyAction();
						Title.showTitle(gPlayer.getPlayer(),"§a§lGO",0,1.2,0.2);
						if(game.getArena().getRounds() > 1) Title.showSubTitle(gPlayer.getPlayer(),"§f§l1/"+game.getArena().getRounds(),0.2,2,0.2);
						gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_BELL,1f,2f);
						gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_FLUTE,1f,2f);
					}
					RaceBarrier barrier = game.getArena().getBarrier();
					for(Block block : barrier.getBlocks()){
						block.setType(Material.AIR);
					}
				}
			}
			else if(game.getGameTimeDefault()-game.getGameTime() <= game.getArena().getRaceType().getMaxStartInactivity()+1){
				for(GamePlayer gPlayer : game.getPlayers()){
					if(gPlayer.getState() != GamePlayerState.SPECTATOR && game.getUser(gPlayer).getLastKeyAction()+(game.getArena().getRaceType().getMaxStartInactivity()*1000) < System.currentTimeMillis()){
						gPlayer.setState(GamePlayerState.SPECTATOR);
						gPlayer.getPlayer().leaveVehicle();
						game.getUser(gPlayer).exitVehicle();
						gPlayer.toggleSpectator();
						game.sendMessage("§7"+gPlayer.getPlayer().getName()+" diskvalifikovan za neaktivitu");
					}
				}
			}
			else if(game.getGameTimeDefault()-game.getGameTime() > game.getArena().getRaceType().getMaxStartInactivity()+1){
				for(GamePlayer gPlayer : game.getPlayers()){
					if(gPlayer.getState() != GamePlayerState.SPECTATOR && game.getUser(gPlayer).getLastKeyAction()+(game.getArena().getRaceType().getMaxGameInactivity()*1000) < System.currentTimeMillis()){
						gPlayer.setState(GamePlayerState.SPECTATOR);
						gPlayer.getPlayer().leaveVehicle();
						game.getUser(gPlayer).exitVehicle();
						gPlayer.toggleSpectator();
						game.sendMessage("§7"+gPlayer.getPlayer().getName()+" diskvalifikovan za neaktivitu");
					}
				}
			}
		}
		game.getScoreboard().update();
	}

	@EventHandler
	public void PlayerRespawnEvent(PlayerRespawnEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		gPlayer.resetPlayer();
		game.getUser(gPlayer).respawn();
	}

	@EventHandler
	public void PlayerMoveEvent(PlayerMoveEvent event){
		if(game.getState().isGame()){
			GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
			if(gPlayer.getState() != GamePlayerState.SPECTATOR){
				Location location = event.getPlayer().getLocation();
				for(RaceCheckpoint checkpoint : game.getArena().getCheckpoints()){
					if(checkpoint.getType() == RaceCheckpointType.CHECKPOINT){
						if(((game.getUser(gPlayer).getLastCheckpoint() == null && checkpoint.getIndex() == 0) || (game.getUser(gPlayer).getLastCheckpoint() != null && game.getUser(gPlayer).getLastCheckpoint().getIndex() == checkpoint.getIndex()-1)) && checkpoint.isLocationInside(location)){
							game.getUser(gPlayer).setLastCheckpoint(checkpoint);
							this.PlayerEnterCheckpoint(gPlayer,checkpoint);
						}
					}
					else if(checkpoint.getType() == RaceCheckpointType.FINISH){
						if(game.getUser(gPlayer).getLastCheckpoint() != null && game.getUser(gPlayer).getLastCheckpoint().getIndex() == checkpoint.getIndex()-1 && checkpoint.isLocationInside(location)){
							game.getUser(gPlayer).setLastCheckpoint(checkpoint);
							this.PlayerEnterCheckpoint(gPlayer,checkpoint);
						}
					}
				}
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void EntityDamageEvent(EntityDamageEvent event){
		if(event.getEntity() instanceof Player){
			event.setCancelled(true);
			if(event.getCause() == DamageCause.LAVA || event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK){
				event.getEntity().setFireTicks(0);
			}
		}
	}

	@EventHandler
	public void InventoryClickEvent(InventoryClickEvent event){
		event.setCancelled(true);
	}

	@EventHandler
	public void VehicleEnterEvent(VehicleEnterEvent event){
		if(event.getVehicle().getPassengers().size() != 0) event.setCancelled(true);
	}

	@EventHandler
	public void VehicleExitEvent(VehicleExitEvent event){
		event.setCancelled(true);
	}

	@EventHandler
	public void VehicleDamageEvent(VehicleDamageEvent event){
		event.setCancelled(true);
	}

	@EventHandler
	public void VehicleEntityCollisionEvent(VehicleEntityCollisionEvent event){
		if(event.getVehicle().getType() == EntityType.BOAT && event.getEntity().getType() == EntityType.PLAYER){
			if(!event.getEntity().getVehicle().equals(event.getVehicle())){
				event.setCancelled(true);
			}
		}
	}

	public void PlayerEnterCheckpoint(GamePlayer gPlayer,RaceCheckpoint checkpoint){
		if(checkpoint.getType() == RaceCheckpointType.FINISH){
			if(game.getUser(gPlayer).getRounds() == game.getArena().getRounds()){
				game.addWinner(gPlayer);
				if(game.getGameTime() > 60) game.setGameTime(60);
				int position = game.getWinner(gPlayer).getPosition();
				if(position <= 3){
					final int reward = Users.getUser(gPlayer.getPlayer()).giveCoins(
						(game.getConfig().getInt("reward.base."+position,0))+
						(game.getConfig().getInt("reward.player",0)*game.getStartPlayers())
					);
					Title.showTitle(gPlayer.getPlayer(),"§a§lFinish!",0.5,8,0.5);
					Title.showSubTitle(gPlayer.getPlayer(),"§fDojel jsi na "+position+". miste",0.5,8,0.5);
					Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
						@Override
						public void run(){
							Coins.runCoinsEffect(gPlayer.getPlayer(),"§a§lFinish!",reward);
						}
					},60);
					game.sendMessage("§b"+gPlayer.getPlayer().getName()+" §fdojel na "+position+". miste a ziskava §a+"+reward+" coins");
					if(position == 1){
						for(int i=0;i<10;i++){
							Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
								@Override
								public void run(){
									Location location = checkpoint.getCenterLocation().clone();
									location.add(RandomUtil.getRandomInteger(-5,5),RandomUtil.getRandomInteger(0,5),RandomUtil.getRandomInteger(-5,5));
									FireworkUtil.spawnFirework(location,null,false);
									location.getWorld().playSound(location,Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,1f,1f);
								}
							},i*10);
						}
					}
				} else {
					Title.showSubTitle(gPlayer.getPlayer(),"§fDojel jsi na "+position+". miste",0.5,8,0.5);
					game.sendMessage("§b"+gPlayer.getPlayer().getName()+" §fdojel na "+position+". miste");
				}
				if(game.getGameTime() >= 5){
					Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
						@Override
						public void run(){
							if(gPlayer.getPlayer().isOnline()){
								gPlayer.setState(GamePlayerState.SPECTATOR);
								gPlayer.getPlayer().leaveVehicle();
								game.getUser(gPlayer).exitVehicle();
								gPlayer.toggleSpectator();
							}
						}
					},60);
				}
				gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_PLAYER_LEVELUP,1f,1f);
			} else {
				game.getUser(gPlayer).setLastCheckpoint(null);
				game.getUser(gPlayer).addRound();
				Title.showTitle(gPlayer.getPlayer(),"",0.2,2,0.2);
				Title.showSubTitle(gPlayer.getPlayer(),"§f§l"+game.getUser(gPlayer).getRounds()+"/"+game.getArena().getRounds(),0.2,2,0.2);
				gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_BELL,1f,1f);
			}
		}
	}
}