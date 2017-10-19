package com.games.game;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.material.Bed;
import org.bukkit.material.Door;
import org.bukkit.material.Gate;
import org.bukkit.material.TrapDoor;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.games.Games;
import com.games.events.GameEndEvent;
import com.games.events.GameStateChangeEvent;
import com.games.exceptions.GameMaxPlayersException;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.LocationUtil;
import com.games.utils.ReflectionUtils;
import com.realcraft.RealCraft;
import com.realcraft.lobby.LobbyMenu;

import net.minecraft.server.v1_12_R1.EnumGamemode;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;

public class GameListeners implements Listener {

	private Game game;

	public GameListeners(Game game){
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this,Games.getInstance());
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(RealCraft.getInstance(),PacketType.Play.Server.PLAYER_INFO,PacketType.Play.Server.GAME_STATE_CHANGE,PacketType.Play.Server.CAMERA){
			@Override
			public void onPacketSending(PacketEvent event){
				Player player = event.getPlayer();
				GamePlayer gPlayer = game.getGamePlayer(player);
				if(gPlayer != null && gPlayer.getState() == GamePlayerState.SPECTATOR){
					try {
						if(event.getPacketType() == PacketType.Play.Server.PLAYER_INFO){
							String name = event.getPacket().getPlayerInfoDataLists().read(0).get(0).getProfile().getName();
							if(player.getName().equalsIgnoreCase(name)){
								PacketPlayOutPlayerInfo packet = (PacketPlayOutPlayerInfo) event.getPacket().getHandle();
								PacketPlayOutPlayerInfo.EnumPlayerInfoAction action = (PacketPlayOutPlayerInfo.EnumPlayerInfoAction) ReflectionUtils.getField(packet.getClass(),true,"a").get(packet);
								if(action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE){
									@SuppressWarnings("unchecked")
									List<PacketPlayOutPlayerInfo.PlayerInfoData> infoList = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) ReflectionUtils.getField(packet.getClass(),true,"b").get(packet);
									for(PacketPlayOutPlayerInfo.PlayerInfoData infoData : infoList){
										if(infoData.c() == EnumGamemode.SPECTATOR){
											ReflectionUtils.setValue(infoData,true,"c",EnumGamemode.ADVENTURE);
										}
									}
								}
							}
						}
						else if(event.getPacketType() == PacketType.Play.Server.GAME_STATE_CHANGE){
							event.setCancelled(true);
						}
						else if(event.getPacketType() == PacketType.Play.Server.CAMERA){
							event.setCancelled(true);
						}
					} catch (Exception e){
						e.printStackTrace();
					}
				}
			}
		});
	}

	@EventHandler
	public void PlayerLoginEvent(PlayerLoginEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		try {
			game.tryToConnect(gPlayer);
		} catch (GameMaxPlayersException exception){
			event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
			event.setKickMessage("GameMaxPlayersException");
		}
	}

	@EventHandler
	public void PlayerJoinEvent(PlayerJoinEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		try {
			game.joinPlayer(gPlayer);
		} catch (GameMaxPlayersException exception){
			event.getPlayer().kickPlayer("GameMaxPlayersException");
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void PlayerQuitEvent(PlayerQuitEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		game.removePlayer(gPlayer);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void PlayerChangedWorldEvent(PlayerChangedWorldEvent event){
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
				if(event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK){
					if(GameFlag.USE_FIRE == false){
						event.setCancelled(true);
						event.getEntity().setFireTicks(0);
					}
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event){
		if(game.getState() != GameState.INGAME) event.setCancelled(true);
		if(event.getDamager() instanceof Player && game.getGamePlayer((Player)event.getDamager()).getState() == GamePlayerState.SPECTATOR && ((Player)event.getDamager()).getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
		if(event.getEntity() instanceof ItemFrame){
			if(event.getDamager() instanceof Player){
				Player player = (Player) event.getDamager();
				if(player.getGameMode() != GameMode.CREATIVE){
					event.setCancelled(true);
				}
				if(game.getState().isLobby()) game.getVoting().clickVoting(game.getGamePlayer((Player)event.getDamager()),event.getEntity().getLocation().getBlock().getLocation());
			}
			else event.setCancelled(true);
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
	public void BlockFadeEvent(BlockFadeEvent event){
		Block block = event.getBlock();
		if(block != null && block.getType() == Material.SOIL){
			event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void BlockGrowEvent(BlockGrowEvent event){
		if(event.getNewState().getType() == Material.SUGAR_CANE_BLOCK){
			event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void LeavesDecayEvent(LeavesDecayEvent event){
		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void EntityExplodeEvent(EntityExplodeEvent event){
		if(event.getEntityType() == EntityType.PRIMED_TNT) event.blockList().clear();
	}

	@EventHandler(priority=EventPriority.LOW)
	public void EntityInteractEvent(EntityInteractEvent event){
		if(event.getEntity() instanceof Animals){
			Block block = event.getBlock();
			if(block != null && block.getType() == Material.SOIL){
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
		if(event.getRemover() instanceof Player && ((Player)event.getRemover()).getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
		else event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void PlayerInteractEntityEvent(PlayerInteractEntityEvent event){
		if(event.getPlayer().getGameMode() != GameMode.CREATIVE && event.getRightClicked() != null && event.getRightClicked().getType() == EntityType.ITEM_FRAME){
			event.setCancelled(true);
			if(game.getState().isLobby()) game.getVoting().clickVoting(game.getGamePlayer(event.getPlayer()),event.getRightClicked().getLocation().getBlock().getLocation());
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void PlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent event){
		if(event.getPlayer().getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void PlayerInteractEvent(PlayerInteractEvent event){
		if(game.getState().isLobby()){
			Block block = event.getClickedBlock();
			if(event.getPlayer().getGameMode() != GameMode.CREATIVE && (block == null || block.getType() != Material.WOOD_PLATE)) event.setCancelled(true);
			if(event.getAction() == Action.PHYSICAL && block != null && block.getType() == Material.SOIL){
				event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
				event.setCancelled(true);
			}
			if(block != null && block.getType() == Material.CONCRETE && event.getHand() == EquipmentSlot.HAND){
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
						if(event.getAction() == Action.PHYSICAL && blockState != null && blockState.getType() == Material.SOIL){
							event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
							event.setCancelled(true);
						}
						else if((blockState.getData() instanceof Door || blockState.getData() instanceof TrapDoor || blockState.getData() instanceof Gate) && GameFlag.USE_DOOR == false){
							event.setCancelled(true);
						}
						else if(blockState.getData() instanceof Bed && GameFlag.USE_BED == false){
							event.setCancelled(true);
						}
						else if((blockState.getType() == Material.CHEST || blockState.getType() == Material.ENDER_CHEST || blockState.getType() == Material.TRAPPED_CHEST) && GameFlag.USE_CONTAINER == false){
							event.setCancelled(true);
						}
						else if((blockState.getType() == Material.LEVER || blockState.getType() == Material.REDSTONE_COMPARATOR || blockState.getType() == Material.DIODE) && GameFlag.USE_REDSTONE == false){
							event.setCancelled(true);
						}
						else if(blockState.getType() == Material.WORKBENCH && GameFlag.CRAFT == false){
							event.setCancelled(true);
						}
						else if(blockState instanceof Furnace && GameFlag.USE_FURNACE == false){
							event.setCancelled(true);
						}
						else if(blockState.getType() == Material.ANVIL && GameFlag.USE_ANVIL == false){
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
		if(event.getEntity() instanceof Player && game.getState().isLobby() || GameFlag.PICKUP == false || game.getGamePlayer((Player)event.getEntity()).getState() == GamePlayerState.SPECTATOR) event.setCancelled(true);
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
		if(!event.getPlayer().hasPermission("group.Admin")) event.setCancelled(true);
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
	}

	@EventHandler(priority=EventPriority.LOW)
	public void GameEndEvent(GameEndEvent event){
		game.setState(GameState.LOBBY);
		game.getVoting().resetVoting();
		for(GamePlayer gPlayer : game.getPlayers()){
			gPlayer.resetPlayer();
			gPlayer.getPlayer().teleport(game.getLobbyLocation());
			gPlayer.getPlayer().getInventory().setItem(0,LobbyMenu.getItem());
			game.getLobbyScoreboard().updateForPlayer(gPlayer);
			game.getLobbyBossBar().updateForPlayer(gPlayer);
		}
	}
}