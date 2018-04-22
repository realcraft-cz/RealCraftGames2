package com.blockparty;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.games.Games;
import com.games.events.GameCycleEvent;
import com.games.events.GameEndEvent;
import com.games.events.GamePlayerJoinEvent;
import com.games.events.GamePlayerStateChangeEvent;
import com.games.events.GameStartEvent;
import com.games.events.GameStateChangeEvent;
import com.games.events.GameTimeoutEvent;
import com.games.game.GameState;
import com.games.game.GameStats.GameStatsType;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.FireworkUtil;
import com.games.utils.Particles;
import com.games.utils.Particles.BlockData;
import com.games.utils.Title;

import realcraft.bukkit.coins.Coins;
import realcraft.bukkit.users.Users;

public class BlockPartyListeners implements Listener {

	private BlockParty game;

	public BlockPartyListeners(BlockParty game){
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this,Games.getInstance());
	}

	public BlockParty getGame(){
		return game;
	}

	@EventHandler
	public void GamePlayerJoinEvent(GamePlayerJoinEvent event){
		if(game.getState().isGame()) game.getScoreboard().updateForPlayer(event.getPlayer());
	}

	@EventHandler
	public void GamePlayerStateChangeEvent(GamePlayerStateChangeEvent event){
		if(game.getState().isGame()) game.getScoreboard().updateForPlayer(event.getPlayer());
	}

	@EventHandler
	public void GameStartEvent(GameStartEvent event){
		game.reset();
		game.getArena().getWorld().setFullTime(1000);
		int idx = 0;
		for(GamePlayer gPlayer : game.getPlayers()){
			gPlayer.resetPlayer();
			gPlayer.getPlayer().teleport(game.getArena().getStartLocation(idx++,game.getPlayersCount()));
			gPlayer.getPlayer().setGameMode(GameMode.SURVIVAL);
			gPlayer.getPlayer().getInventory().setHeldItemSlot(4);
			game.getScoreboard().updateForPlayer(gPlayer);
			game.getBossBar().updateForPlayer(gPlayer);
		}
		for(int i=0;i<=40;i++){
			final int index = i;
			Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					game.getArena().getWorld().setFullTime((25000-(index*50))%24000);
				}
			},60+i);
		}
	}

	@EventHandler
	public void GameEndEvent(GameEndEvent event){
		for(GamePlayer gPlayer : game.getPlayers()){
			game.getScoreboard().updateForPlayer(gPlayer);
			game.getBossBar().updateForPlayer(gPlayer);
		}
	}

	@EventHandler
	public void GameTimeoutEvent(GameTimeoutEvent event){
		game.setState(GameState.ENDING);
	}

	@EventHandler
	public void GameStateChangeEvent(GameStateChangeEvent event){
		if(game.getState() == GameState.ENDING){
			game.getArena().chooseDefaultFloor();
			GamePlayer winner = game.getWinner();
			if(winner != null){
				Location sideLoc1 = game.getArena().getGameLocation().clone().add(10,1,10);
				Location sideLoc2 = game.getArena().getGameLocation().clone().add(-10,1,-10);
				Location sideLoc3 = game.getArena().getGameLocation().clone().add(10,1,-10);
				Location sideLoc4 = game.getArena().getGameLocation().clone().add(-10,1,10);
				for(int i=1;i<120;i+=20){
					Bukkit.getScheduler().scheduleSyncDelayedTask(Games.getInstance(),new Runnable(){
						@Override
						public void run(){
							FireworkUtil.spawnFirework(sideLoc1,FireworkEffect.Type.BURST,false);
							FireworkUtil.spawnFirework(sideLoc2,FireworkEffect.Type.BURST,false);
							FireworkUtil.spawnFirework(sideLoc3,FireworkEffect.Type.BURST,false);
							FireworkUtil.spawnFirework(sideLoc4,FireworkEffect.Type.BURST,false);
						}
					},i);
				}
				for(int i=10;i<120;i+=20){
					Location randomLoc = game.getArena().getGameLocation().clone();
					Random random = new Random();
					randomLoc.add((random.nextInt(10)-5),4,(random.nextInt(10)-5));
					Bukkit.getScheduler().scheduleSyncDelayedTask(Games.getInstance(),new Runnable(){
						@Override
						public void run(){
							FireworkUtil.spawnFirework(randomLoc,null,true);
							randomLoc.getWorld().playSound(randomLoc,Sound.ENTITY_FIREWORK_LAUNCH,1f,1f);
						}
					},i);
				}
			}

			for(GamePlayer gPlayer : game.getPlayers()){
				gPlayer.getPlayer().getInventory().clear();
				if(gPlayer == winner){
					gPlayer.getPlayer().teleport(game.getArena().getGameLocation());

					final int reward = Users.getUser(gPlayer.getPlayer()).giveCoins(
						(game.getConfig().getInt("reward.base",0))+
						(game.getConfig().getInt("reward.player",0)*game.getStartPlayers())
					);

					game.sendMessage("�b"+gPlayer.getPlayer().getName()+" �fvyhral a ziskava �a+"+reward+" coins");

					game.getStats().addScore(gPlayer,GameStatsType.WINS,1);

					Title.showTitle(gPlayer.getPlayer(),"�a�lVitezstvi!",0.5,8,0.5);
					Title.showSubTitle(gPlayer.getPlayer(),"�fVyhral jsi tuto hru",0.5,8,0.5);

					Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
						@Override
						public void run(){
							Coins.runCoinsEffect(gPlayer.getPlayer(),"�a�lVitezstvi!",reward);
						}
					},10*20);
				} else {
					Title.showTitle(gPlayer.getPlayer(),"�c�lProhra",0.5,8,0.5);
					if(winner != null) Title.showSubTitle(gPlayer.getPlayer(),"�b"+winner.getPlayer().getName()+" �fvyhral tuto hru",0.5,8,0.5);
					else Title.showSubTitle(gPlayer.getPlayer(),"�fNikdo tuto hru nevyhral",0.5,8,0.5);
				}
			}
		}
	}

	@EventHandler
	public void GameCycleEvent(GameCycleEvent event){
		if(game.getState() == GameState.INGAME){
			if(game.getPlayersCount() < 2) game.setState(GameState.ENDING);//TODO: uncomment
			Particles.FIREWORKS_SPARK.display(10f,4f,10f,0f,26,game.getArena().getGameLocation().clone().add(0,8,0),64);
			if(game.getRoundState() == BlockPartyState.WAITING){
				if(game.getCountdown() > 0){
					game.setCountdown(game.getCountdown()-1);
				} else {
					game.setRoundState(BlockPartyState.COUNTDOWN);
					game.setCountdown(game.getRoundSpeed());
					game.getArena().chooseRandomBlock();
					game.playRoundSound(game.getCountdown()+1);
				}
			}
			else if(game.getRoundState() == BlockPartyState.COUNTDOWN){
				if(game.getCountdown() > 0){
					game.setCountdown(game.getCountdown()-1);
					game.playRoundSound(game.getCountdown()+1);
				} else {
					game.setRoundState(BlockPartyState.FALLING);
					game.setCountdown(4);
					game.clearFloor();
				}
			}
			else if(game.getRoundState() == BlockPartyState.FALLING){
				if(game.getCountdown() > 0){
					game.setCountdown(game.getCountdown()-1);
				} else {
					game.setRoundState(BlockPartyState.WAITING);
					game.setCountdown(4);
					game.nextRound();
				}
			}
		}
		game.getScoreboard().update();
		game.getBossBar().update();
	}

	@EventHandler
	public void PlayerRespawnEvent(PlayerRespawnEvent event){
		event.setRespawnLocation(game.getArena().getLobbyLocation());
		game.getGamePlayer(event.getPlayer()).resetPlayer();
	}

	@EventHandler(ignoreCancelled=true)
	public void EntityDamageEvent(EntityDamageEvent event){
		if(event.getEntity() instanceof Player && event.getCause() != DamageCause.ENTITY_ATTACK && event.getCause() != DamageCause.PROJECTILE && event.getCause() != DamageCause.LIGHTNING){
			event.setCancelled(true);
			event.getEntity().setFireTicks(0);
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event){
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Player){
			event.setCancelled(true);
		}
		else if(event.getDamager().getType() == EntityType.SILVERFISH || event.getDamager().getType() == EntityType.ZOMBIE){
			event.setCancelled(false);
			event.setDamage(0);
		}
		else if(event.getCause() == DamageCause.LIGHTNING){
			event.setDamage(0);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void PlayerMoveEvent(PlayerMoveEvent event){
		if(game.getState().isLobby()) return;
		if(game.getGamePlayer(event.getPlayer()).getState() == GamePlayerState.DEFAULT){
			if(event.getPlayer().getLocation().getY() < BlockParty.MINY){
				event.getPlayer().setFallDistance(0);
				event.getPlayer().getWorld().strikeLightningEffect(event.getPlayer().getLocation());
				event.getPlayer().teleport(game.getArena().getLobbyLocation());
				game.getGamePlayer(event.getPlayer()).setState(GamePlayerState.SPECTATOR);
				game.getGamePlayer(event.getPlayer()).toggleSpectator();
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	public void PlayerInteractEvent(PlayerInteractEvent event){
		Block block = event.getClickedBlock();
		if((event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) && block != null && block.getType() == Material.BEACON){
			event.getPlayer().getWorld().playSound(block.getLocation(),Sound.BLOCK_GLASS_BREAK,1f,1f);
			game.getPickup().activate(game.getGamePlayer(event.getPlayer()));
			game.getPickup().remove();
			game.sendMessage("�b"+event.getPlayer().getName()+" "+game.getPickup().getType().getMessage());
		}
		else if(event.getAction() == Action.LEFT_CLICK_BLOCK && event.getItem() != null && event.getItem().getType() == Material.GOLD_SPADE && block != null && this.getGame().getArena().isBlockInArena(block.getLocation())){
			Particles.BLOCK_CRACK.display(new BlockData(block.getType(),block.getData()),0.3f,0.3f,0.3f,0.0f,64,block.getLocation().add(0.5,0.7,0.5),64);
			block.getWorld().playSound(block.getLocation(),Sound.BLOCK_STONE_PLACE,1f,1f);
			block.setType(Material.AIR);
			event.getItem().setDurability((short)(event.getItem().getDurability()+2));
			if(event.getItem().getType().getMaxDurability() <= event.getItem().getDurability()){
				event.getPlayer().getInventory().remove(event.getItem().getType());
				block.getWorld().playSound(event.getPlayer().getLocation(),Sound.ENTITY_ITEM_BREAK,1f,1f);
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
    public void EntityPickupItemEvent(EntityPickupItemEvent event){
		if(event.getEntity() instanceof Player){
			if(event.getItem().getItemStack().getType() == Material.SNOW_BALL){
				((Player)event.getEntity()).getInventory().addItem(new ItemStack(Material.SNOW_BALL,1));
				event.getItem().getWorld().playSound(event.getEntity().getLocation(),Sound.ENTITY_ITEM_PICKUP,0.5f,2f);
				event.getItem().remove();
			}
			else if(event.getItem().getItemStack().getType() == Material.GOLD_SPADE){
				if(!((Player)event.getEntity()).getInventory().contains(Material.GOLD_SPADE)){
					((Player)event.getEntity()).getInventory().remove(Material.GOLD_SPADE);
					((Player)event.getEntity()).getInventory().addItem(new ItemStack(Material.GOLD_SPADE,1));
					event.getItem().getWorld().playSound(event.getEntity().getLocation(),Sound.ENTITY_ITEM_PICKUP,0.5f,2f);
					event.getItem().remove();
				}
			} else {
				event.getItem().remove();
			}
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void InventoryClickEvent(InventoryClickEvent event){
		event.setCancelled(true);
	}

	@EventHandler
	public void EntityTargetEvent(EntityTargetEvent event){
		event.setCancelled(true);
	}
}