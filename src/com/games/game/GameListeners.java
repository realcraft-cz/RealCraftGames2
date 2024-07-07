package com.games.game;

import com.games.Games;
import com.games.events.GameEndEvent;
import com.games.events.GameRegionLoadEvent;
import com.games.events.GameStateChangeEvent;
import com.games.exceptions.GameMaintenanceException;
import com.games.exceptions.GameMaxPlayersException;
import com.games.exceptions.GameNotLoadedException;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.material.Door;
import org.bukkit.material.Gate;
import org.bukkit.material.TrapDoor;
import org.bukkit.util.Vector;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import realcraft.bukkit.anticheat.AntiCheat;
import realcraft.bukkit.lobby.LobbyMenu;
import realcraft.bukkit.utils.LocationUtil;
import realcraft.bukkit.utils.MaterialUtil;

public class GameListeners implements Listener {

	private Game game;

	public GameListeners(Game game){
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this,Games.getInstance());
	}

	@EventHandler
	public void PlayerLoginEvent(PlayerLoginEvent event){
		try {
			game.tryToConnect(event.getPlayer());
		} catch (GameMaxPlayersException exception){
			event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
			event.setKickMessage(game.getPrefix()+"§cTato hra je jiz plna.");
		} catch (GameMaintenanceException e){
			event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
			event.setKickMessage(game.getPrefix()+"§cTato hra je docasne nedostupna, zkuste to prosim pozdeji.");
		} catch (GameNotLoadedException e){
			event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
			event.setKickMessage(game.getPrefix()+"§cTato hra se teprve nacita, vyckejte prosim.");
		}
	}

	@EventHandler
	public void PlayerJoinEvent(PlayerJoinEvent event){
		try {
			game.joinPlayer(event.getPlayer());
		} catch (GameMaxPlayersException exception){
			event.getPlayer().kickPlayer(game.getPrefix()+"§cTato hra je jiz plna.");
		} catch (GameMaintenanceException e){
			event.getPlayer().kickPlayer(game.getPrefix()+"§cTato hra je docasne nedostupna, zkuste to prosim pozdeji.");
		} catch (GameNotLoadedException e){
			event.getPlayer().kickPlayer(game.getPrefix()+"§cTato hra se teprve nacita, vyckejte prosim.");
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void PlayerQuitEvent(PlayerQuitEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		gPlayer.setLeaving();
		Bukkit.getScheduler().runTask(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				game.removePlayer(gPlayer);
			}
		});
	}

	@EventHandler
	public void PlayerSpawnLocationEvent(PlayerSpawnLocationEvent event){
		event.setSpawnLocation(game.getLobbyLocation());
	}

	@EventHandler(priority=EventPriority.LOW)
	public void PlayerDeathEvent(PlayerDeathEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getEntity());
		Bukkit.getScheduler().scheduleSyncDelayedTask(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				gPlayer.getPlayer().spigot().respawn();
			}
		},20);
		event.setDroppedExp(0);
		event.getDrops().clear();
	}

	@EventHandler(priority=EventPriority.LOW)
	public void EntityDamageEvent(EntityDamageEvent event){
		if(event.getEntity() instanceof Player){
			if(game.getState() != GameState.INGAME){
				event.setCancelled(true);
				if(event.getCause() == DamageCause.LAVA || event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK) event.getEntity().setFireTicks(0);
				if(event.getCause() == DamageCause.VOID) event.getEntity().teleport(game.getLobbyLocation());
			}
			if(game.getState().isGame()){
				GamePlayer gPlayer = game.getGamePlayer((Player)event.getEntity());
				if(gPlayer.getState() == GamePlayerState.SPECTATOR){
					event.setCancelled(true);
					event.getEntity().setFireTicks(0);
					return;
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		if (game.getState() != GameState.INGAME && (!(event.getDamager() instanceof Player) || ((Player) event.getDamager()).getGameMode() != GameMode.CREATIVE))
			event.setCancelled(true);
		if (event.getDamager() instanceof Player && game.getGamePlayer((Player) event.getDamager()).getState() == GamePlayerState.SPECTATOR)
			event.setCancelled(true);
		if (event.getEntity() instanceof ItemFrame) {
			if (event.getDamager() instanceof Player) {
				Player player = (Player) event.getDamager();
				if (player.getGameMode() != GameMode.CREATIVE) {
					event.setCancelled(true);
				}
				if (game.getState().isLobby())
					game.getVoting().clickVoting(game.getGamePlayer((Player) event.getDamager()), event.getEntity().getLocation().getBlock().getLocation());
			} else event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void BlockBreakEvent(BlockBreakEvent event){
		if((game.getState().isLobby() || game.getGamePlayer(event.getPlayer()).getState() == GamePlayerState.SPECTATOR || GameFlag.DESTROY == false) && event.getPlayer().getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void BlockPlaceEvent(BlockPlaceEvent event){
		if((game.getState().isLobby() || game.getGamePlayer(event.getPlayer()).getState() == GamePlayerState.SPECTATOR || GameFlag.BUILD == false) && event.getPlayer().getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void BlockCanBuildEvent(BlockCanBuildEvent event){
		if(!event.isBuildable()){
			for(GamePlayer gPlayer : game.getPlayers()){
				if(gPlayer.getState() != GamePlayerState.SPECTATOR) continue;
				Location location = gPlayer.getPlayer().getLocation();
				Location blockLocation = event.getBlock().getLocation();
				if (location.getX() > blockLocation.getBlockX()-1 && location.getX() < blockLocation.getBlockX()+1){
					if (location.getZ() > blockLocation.getBlockZ()-1 && location.getZ() < blockLocation.getBlockZ()+1){
						if (location.getY() > blockLocation.getBlockY()-2 && location.getY() < blockLocation.getBlockY()+1){
							event.setBuildable(true);
							gPlayer.getPlayer().setVelocity(new Vector(0,1,0));
							Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
								@Override
								public void run(){
									gPlayer.getPlayer().setAllowFlight(true);
									gPlayer.getPlayer().setFlying(true);
								}
							},10);
						}
					}
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void BlockFadeEvent(BlockFadeEvent event){
		Block block = event.getBlock();
		if(block != null && block.getType() == Material.FARMLAND){
			event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void BlockSpreadEvent(BlockSpreadEvent event){
		if(event.getSource().getType() == Material.VINE || event.getSource().getType() == Material.BROWN_MUSHROOM || event.getSource().getType() == Material.RED_MUSHROOM){
			event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void BlockGrowEvent(BlockGrowEvent event){
		if(event.getNewState().getType() == Material.SUGAR_CANE){
			event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void LeavesDecayEvent(LeavesDecayEvent event){
		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void EntityExplodeEvent(EntityExplodeEvent event){
		if(game.getState().isLobby() && event.getEntityType() == EntityType.TNT) event.blockList().clear();
	}

	@EventHandler(priority=EventPriority.LOW)
	public void EntityInteractEvent(EntityInteractEvent event){
		if(event.getEntity() instanceof Animals){
			Block block = event.getBlock();
			if(block != null && block.getType() == Material.FARMLAND){
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void HangingBreakEvent(HangingBreakEvent event){
		if(event.getCause() == RemoveCause.EXPLOSION){
			event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void HangingBreakByEntityEvent(HangingBreakByEntityEvent event){
		if(event.getRemover() instanceof Player){
			if(((Player)event.getRemover()).getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
		}
		else event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void PlayerInteractEntityEvent(PlayerInteractEntityEvent event){
		if(event.getPlayer().getGameMode() != GameMode.CREATIVE && event.getRightClicked() != null && event.getRightClicked().getType() == EntityType.ITEM_FRAME){
			event.setCancelled(true);
			if(game.getState().isLobby()) game.getVoting().clickVoting(game.getGamePlayer(event.getPlayer()),event.getRightClicked().getLocation().getBlock().getLocation());
		}
		else if(event.getPlayer().getGameMode() != GameMode.CREATIVE && (game.getState().isLobby() || game.getGamePlayer(event.getPlayer()).getState() == GamePlayerState.SPECTATOR)) event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void PlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent event){
		if(game.getState().isLobby() && event.getPlayer().getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void PlayerInteractEventFix(PlayerInteractEvent event){
		if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR){
			event.setCancelled(false);
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void PlayerInteractEvent(PlayerInteractEvent event){
		if(game.getState().isLobby()){
			Block block = event.getClickedBlock();
			if(event.getPlayer().getGameMode() != GameMode.CREATIVE && (block == null || block.getType() != Material.LEGACY_WOOD_PLATE)) event.setCancelled(true);
			if(event.getAction() == Action.PHYSICAL && block != null && block.getType() == Material.FARMLAND){
				event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
				event.setCancelled(true);
			}
			if(block != null && MaterialUtil.isConcrete(block.getType()) && event.getHand() == EquipmentSlot.HAND){
				Entity entities[] = block.getLocation().getChunk().getEntities();
				for(Entity entity : entities){
					if(!(entity instanceof ItemFrame)) continue;
					if(LocationUtil.isSimilar(block.getRelative(BlockFace.NORTH).getLocation(),entity.getLocation().getBlock().getLocation())
						|| LocationUtil.isSimilar(block.getRelative(BlockFace.SOUTH).getLocation(),entity.getLocation().getBlock().getLocation())
						|| LocationUtil.isSimilar(block.getRelative(BlockFace.EAST).getLocation(),entity.getLocation().getBlock().getLocation())
						|| LocationUtil.isSimilar(block.getRelative(BlockFace.WEST).getLocation(),entity.getLocation().getBlock().getLocation())){
						game.getVoting().clickVoting(game.getGamePlayer(event.getPlayer()),entity.getLocation().getBlock().getLocation());
						break;
					}
				}
			}
		} else {
			if(game.getGamePlayer(event.getPlayer()).getState() == GamePlayerState.SPECTATOR) event.setCancelled(true);
			else {
				Block block = event.getClickedBlock();
				if(block != null){
					BlockState blockState = block.getState();
					if(blockState != null){
						if(event.getAction() == Action.PHYSICAL && blockState != null && blockState.getType() == Material.FARMLAND){
							event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
							event.setCancelled(true);
						}
						else if((blockState.getData() instanceof Door || blockState.getData() instanceof TrapDoor || blockState.getData() instanceof Gate) && GameFlag.USE_DOOR == false){
							event.setCancelled(true);
						}
						else if((blockState.getType() == Material.CHEST || blockState.getType() == Material.ENDER_CHEST || blockState.getType() == Material.TRAPPED_CHEST) && GameFlag.USE_CONTAINER == false){
							event.setCancelled(true);
						}
						else if((blockState.getType() == Material.LEVER || blockState.getType() == Material.COMPARATOR || blockState.getType() == Material.REPEATER) && GameFlag.USE_REDSTONE == false){
							event.setCancelled(true);
						}
						else if(blockState.getType() == Material.CRAFTING_TABLE && GameFlag.CRAFT == false){
							event.setCancelled(true);
						}
						else if(blockState instanceof Furnace && GameFlag.USE_FURNACE == false){
							event.setCancelled(true);
						}
						else if(blockState.getType() == Material.ANVIL && GameFlag.USE_ANVIL == false){
							event.setCancelled(true);
						}
						else if(blockState.getType() == Material.FLOWER_POT){
							event.setCancelled(true);
						}
						else if(event.getAction() == Action.LEFT_CLICK_BLOCK && GameFlag.USE_FIRE == false){
							if(block.getRelative(BlockFace.UP).getType() == Material.FIRE){
								event.setCancelled(true);
							}
						}
					}
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void PlayerMoveEvent(PlayerMoveEvent event){
		if(game.getGamePlayer(event.getPlayer()).getState() == GamePlayerState.SPECTATOR && event.getPlayer().getAllowFlight()){
			AntiCheat.exempt(event.getPlayer(),1000);
			if(event.getPlayer().getLocation().getBlockY() < 0){
				event.getPlayer().setAllowFlight(true);
				event.getPlayer().setFlying(true);
				event.getPlayer().setVelocity(event.getPlayer().getVelocity().setY(2.0));
			}
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void PlayerBedEnterEvent(PlayerBedEnterEvent event){
		if(game.getState().isLobby() || GameFlag.USE_BED == false || game.getGamePlayer(event.getPlayer()).getState() == GamePlayerState.SPECTATOR) event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void PlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent event){
		if(game.getState().isLobby() || GameFlag.SWAPHAND == false || game.getGamePlayer(event.getPlayer()).getState() == GamePlayerState.SPECTATOR) event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void CraftItemEvent(CraftItemEvent event){
		if(game.getState().isLobby() || GameFlag.CRAFT == false) event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void PlayerDropItemEvent(PlayerDropItemEvent event){
		if(game.getState().isLobby() || GameFlag.DROP == false || game.getGamePlayer(event.getPlayer()).getState() == GamePlayerState.SPECTATOR) event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOW)
    public void EntityPickupItemEvent(EntityPickupItemEvent event){
		if(event.getEntity() instanceof Player && (game.getState().isLobby() || GameFlag.PICKUP == false || game.getGamePlayer((Player)event.getEntity()).getState() == GamePlayerState.SPECTATOR)) event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void PlayerEggThrowEvent(PlayerEggThrowEvent event){
		event.setHatching(false);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void PlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event){
		if(Games.getCommands() != null && Games.getCommands().length > 0){
			for(String command : Games.getCommands()){
				if(event.getMessage().substring(1).toLowerCase().indexOf(command) == 0){
					return;
				}
			}
		}
		if(event.getMessage().equalsIgnoreCase("/spawn") || event.getMessage().equalsIgnoreCase("/leave")){
			game.leavePlayer(game.getGamePlayer(event.getPlayer()));
			event.setCancelled(true);
			return;
		}
		else if(event.getMessage().equalsIgnoreCase("/voting") && event.getPlayer().hasPermission("group.Manazer")){
			game.getVoting().resetVoting();
			event.setCancelled(true);
			return;
		}
		else if(event.getMessage().equalsIgnoreCase("/clearentities") && event.getPlayer().hasPermission("group.Manazer")){
			game.getArena().getRegion().clearEntities();
			event.setCancelled(true);
			return;
		}
		if(!event.getPlayer().hasPermission("group.Admin")){
			event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void GameStateChangeEvent(GameStateChangeEvent event){
		if(game.getState() == GameState.LOBBY || game.getState() == GameState.INGAME){
			game.resetLobbyTime();
			game.resetGameTime();
			game.resetEndTime();
			game.getLobbyScoreboard().update();
			game.getLobbyBossBar().update();
		}
		else if(game.getState() == GameState.STARTING){
			game.sendGameStartingReminder();
		}
	}

	@EventHandler(priority=EventPriority.LOW,ignoreCancelled = true)
	public void GameEndEvent(GameEndEvent event){
		game.setState(GameState.LOBBY);
		game.getVoting().resetVoting();
		game.getLeaderboard().update();
		game.getStats().addGame(game.getStartPlayers());
		for(GamePlayer gPlayer : game.getPlayers()){
			gPlayer.resetPlayer();
			gPlayer.getPlayer().resetPlayerTime();
			gPlayer.getPlayer().teleport(game.getLobbyLocation());
			gPlayer.getPlayer().getInventory().setItem(0,LobbyMenu.getItem());
			game.getLobbyScoreboard().updateForPlayer(gPlayer);
			game.getLobbyBossBar().updateForPlayer(gPlayer);
			game.sendPremiumOffer(gPlayer);
		}
		Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				game.getArena().resetRegion();
				game.getArena().getRegion().clearEntities();
			}
		},20);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void GameRegionLoadEvent(GameRegionLoadEvent event){
		game.resetArenas();
	}
}