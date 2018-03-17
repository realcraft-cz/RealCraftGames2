package com.bedwars;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Bed;

import com.games.Games;
import com.games.events.GameCycleEvent;
import com.games.events.GameEndEvent;
import com.games.events.GamePlayerJoinEvent;
import com.games.events.GamePlayerLeaveEvent;
import com.games.events.GameStartEvent;
import com.games.events.GameStateChangeEvent;
import com.games.events.GameTimeoutEvent;
import com.games.game.GamePodium.GamePodiumType;
import com.games.game.GameState;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.ItemUtil;
import com.games.utils.LocationUtil;
import com.games.utils.Title;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import realcraft.bukkit.playermanazer.PlayerManazer;

public class BedWarsListeners implements Listener {

	private BedWars game;

	public BedWarsListeners(BedWars game){
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this,Games.getInstance());
	}

	public BedWars getGame(){
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
	}

	@EventHandler
	public void GameStartEvent(GameStartEvent event){
		game.getTeams().autoBalancingTeams();
		for(GamePlayer gPlayer : game.getPlayers()){
			gPlayer.resetPlayer();
			gPlayer.getSettings().setInt("kills",0);
			gPlayer.getSettings().setInt("deaths",0);
			gPlayer.getSettings().setInt("beds",0);
			gPlayer.getSettings().setLong("pickup",0);
			gPlayer.getPlayer().setGameMode(GameMode.SURVIVAL);
			BedWarsTeam team = game.getTeams().getPlayerTeam(gPlayer);
			gPlayer.getPlayer().teleport(team.getSpawnLocation());
			game.getScoreboard().updateForPlayer(gPlayer);
		}
	}

	@EventHandler
	public void GameEndEvent(GameEndEvent event){
		game.getTeams().resetTeams();
		for(GamePlayer gPlayer : game.getPlayers()){
			game.getScoreboard().updateForPlayer(gPlayer);
			game.loadLobbyInventory(gPlayer);
		}
	}

	@EventHandler
	public void GameTimeoutEvent(GameTimeoutEvent event){
		if(game.getTeams().getWinnerTeam() != null) game.setState(GameState.ENDING);
	}

	@EventHandler
	public void GameStateChangeEvent(GameStateChangeEvent event){
		if(game.getState() == GameState.ENDING){
			BedWarsTeam winner = game.getTeams().getWinnerTeam();
			if(winner != null){
				game.sendMessage(winner.getType().getChatColor()+winner.getType().toName()+" §fvyhrali tuto hru");
				for(GamePlayer gPlayer : game.getPlayers()){
					gPlayer.getPlayer().getInventory().clear();
					if(gPlayer.getState() != GamePlayerState.SPECTATOR && game.getTeams().getPlayerTeam(gPlayer).getType() == winner.getType()){
						if(game.getGameTime() < game.getGameTimeDefault()-120){
							int kdreward = (game.getConfig().getInt("reward.kill",0)*gPlayer.getSettings().getInt("kills"))-(game.getConfig().getInt("reward.death",0)*gPlayer.getSettings().getInt("deaths"));
							if(kdreward < 0) kdreward = 0;
							final int reward = PlayerManazer.getPlayerInfo(gPlayer.getPlayer()).giveCoins(
								(game.getConfig().getInt("reward.base",0))+
								kdreward+
								(game.getConfig().getInt("reward.bed",0)*gPlayer.getSettings().getInt("beds"))
							);

							game.getStats().addScore(gPlayer,1,GamePodiumType.LEFT.getId());
							if(gPlayer.getSettings().getInt("kills") > 0) game.getStats().addScore(gPlayer,gPlayer.getSettings().getInt("kills"),GamePodiumType.RIGHT.getId());

							Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
								public void run(){
									PlayerManazer.getPlayerInfo(gPlayer.getPlayer()).runCoinsEffect("§a§lVitezstvi!",reward);
								}
							},10*20);
						}
						Title.showTitle(gPlayer.getPlayer(),"§a§lVitezstvi!",0.5,8,0.5);
						Title.showSubTitle(gPlayer.getPlayer(),"§fTvuj tym vyhral tuto hru",0.5,8,0.5);
					} else {
						if(gPlayer.getSettings().getInt("kills") > 0) game.getStats().addScore(gPlayer,gPlayer.getSettings().getInt("kills"),GamePodiumType.RIGHT.getId());
						Title.showTitle(gPlayer.getPlayer(),"§c§lProhra",0.5,8,0.5);
						Title.showSubTitle(gPlayer.getPlayer(),winner.getType().getChatColor()+winner.getType().toName()+" §fvyhrali tuto hru",0.5,8,0.5);
					}
				}
			}
		}
	}

	@EventHandler
	public void GameCycleEvent(GameCycleEvent event){
		if(game.getState() == GameState.INGAME){
			if(game.getTeams().getActiveTeams().size() < 2) game.setState(GameState.ENDING);
			else if(game.getGameTime() == 0 && game.getTeams().getWinnerTeam() != null) game.setState(GameState.ENDING);
		}
		game.getScoreboard().update();
	}

	@EventHandler(ignoreCancelled=true)
	public void PlayerDeathEvent(PlayerDeathEvent event){
		event.setDeathMessage(null);
		Player player = event.getEntity();
		Player killer = player.getKiller();
		GamePlayer gPlayer = game.getGamePlayer(player);

		player.getInventory().clear();
		gPlayer.getSettings().addInt("deaths",1);

		if(killer != null && killer != player){
			game.getGamePlayer(killer).getSettings().addInt("kills",1);
			killer.playSound(killer.getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1f,1f);
			game.sendMessage("§c\u271E §b"+killer.getName()+" §7zabil hrace §b"+player.getName());
		}
		else game.sendMessage("§c\u271E §b"+player.getName()+" §7zemrel");
		if(!game.getTeams().getPlayerTeam(gPlayer).hasBed()){
			game.getTeams().getPlayerTeam(gPlayer).removePlayer(gPlayer);
			gPlayer.setState(GamePlayerState.SPECTATOR);
		}
	}

	@EventHandler
	public void PlayerRespawnEvent(PlayerRespawnEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		if(game.getTeams().getPlayerTeam(gPlayer) != null){
			event.setRespawnLocation(game.getTeams().getPlayerTeam(gPlayer).getSpawnLocation());
			gPlayer.resetPlayer();
		}
		else if(gPlayer.getState() == GamePlayerState.SPECTATOR){
			gPlayer.resetPlayer();
			gPlayer.setState(GamePlayerState.SPECTATOR);
			gPlayer.toggleSpectator();
			event.setRespawnLocation(game.getArena().getSpectatorLocation());
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void BlockBreakEvent(BlockBreakEvent event){
		if(game.getState() != GameState.INGAME){
			event.setCancelled(true);
			return;
		}
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		Block block = event.getBlock();
		if(block.getType() == Material.BED_BLOCK){
			BedWarsTeam team = game.getTeams().getPlayerTeam(gPlayer);

			Block breakBlock = block;
			Block neighbor = null;
			Bed breakBed = (Bed) breakBlock.getState().getData();

			if(!breakBed.isHeadOfBed()){
				neighbor = breakBlock;
				breakBlock = LocationUtil.getBedNeighbor(neighbor);
			}
			else neighbor = LocationUtil.getBedNeighbor(breakBlock);
			if(team.getBedBlock().equals(breakBlock) || team.getBedBlock().equals(neighbor)){
				event.setCancelled(true);
				return;
			}

			BedWarsTeam destroyedTeam = game.getTeams().getBedTeam(breakBlock);
			if(destroyedTeam == null) destroyedTeam = game.getTeams().getBedTeam(neighbor);

			event.setDropItems(false);
			neighbor.getDrops().clear();
			breakBlock.getDrops().clear();

			if(destroyedTeam != null) destroyedTeam.destroyBed(gPlayer);
		} else {
			if(game.getArena().isPlayerBlock(block)){
				if(block.getType() == Material.ENDER_CHEST){
					event.setCancelled(true);
					BedWarsTeam team = game.getTeams().getChestTeam(block);
					if(team != null){
						team.removeChest(block);
						block.getDrops().clear();
						block.setType(Material.AIR);
						block.getWorld().dropItemNaturally(block.getLocation(),new ItemStack(Material.ENDER_CHEST,1));
					}
				} else {
					Collection<Entity> entities = block.getLocation().getWorld().getNearbyEntities(block.getLocation().clone().add(0.5,2.0,0.5),0.8,0.0,0.8);
					for(Entity entity : entities){
						if(entity instanceof Player){
							GamePlayer gVictim = game.getGamePlayer((Player) entity);
							if(gPlayer.getPlayer().getName().equalsIgnoreCase(gVictim.getPlayer().getName()) == false && game.getTeams().getPlayerTeam(gPlayer) == game.getTeams().getPlayerTeam(gVictim)){
								event.setCancelled(true);
								return;
							}
						}
					}
				}
				game.getArena().removePlayerBlock(block);
			}
			else event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void BlockPlaceEvent(BlockPlaceEvent event){
		if(game.getState() != GameState.INGAME){
			event.setCancelled(true);
			return;
		}
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		Block block = event.getBlock();
		if(game.getArena().isBlockInArena(block)){
			game.getArena().addPlayerBlock(block);
			if(block.getType() == Material.ENDER_CHEST){
				game.getTeams().getPlayerTeam(gPlayer).addChest(block);
			}
		}
		else event.setCancelled(true);
	}

	/*@EventHandler(ignoreCancelled=true)
	public void EntityShootBowEvent(EntityShootBowEvent event){
		int level = event.getBow().getEnchantmentLevel(Enchantment.DURABILITY);
		int damage = 9;
		if(level == 1) damage = 8;
		else if(level == 2) damage = 7;
		else if(level == 3) damage = 6;
		event.getBow().setDurability((short)(event.getBow().getDurability()+damage));
	}*/

	@EventHandler(ignoreCancelled=true)
	public void EntityExplodeEvent(EntityExplodeEvent event){
		if(event.getEntityType() == EntityType.PRIMED_TNT){
			List<Block> blocks = new ArrayList<Block>(event.blockList());
			for(Block block : blocks){
				if(game.getArena().isBlockInArena(block) && !game.getArena().isPlayerBlock(block)){
					event.blockList().remove(block);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
    public void EntityPickupItemEvent(EntityPickupItemEvent event){
		if(event.getEntity() instanceof Player){
			GamePlayer gPlayer = game.getGamePlayer((Player)event.getEntity());
			if(gPlayer.getSettings().getLong("pickup") < System.currentTimeMillis()-50){
				gPlayer.getSettings().setLong("pickup",System.currentTimeMillis());
			} else {
				event.setCancelled(true);
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
					for(BedWarsTeam team : game.getTeams().getTeams()){
						if(itemStack.getDurability() == team.getType().getDyeColor().getWoolData()){
							if(game.getTeams().getPlayerTeam(gPlayer) != team){
								game.getTeams().setPlayerTeam(gPlayer,team);
								gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,1);
								game.loadLobbyInventories();
								break;
							}
						}
					}
				}
			}
		} else {
			ItemStack item = gPlayer.getPlayer().getInventory().getItemInMainHand();
			Block block = event.getClickedBlock();
			if(block != null && block.getType() == Material.ENDER_CHEST && event.getAction() == Action.RIGHT_CLICK_BLOCK && (!event.isBlockInHand() || !gPlayer.getPlayer().isSneaking())){
				event.setCancelled(true);
				BedWarsTeam team = game.getTeams().getChestTeam(block);
				if(team != null){
					BedWarsTeam playerTeam = game.getTeams().getPlayerTeam(gPlayer);
					if(team.equals(playerTeam)){
						gPlayer.getPlayer().openInventory(team.getInventory());
					}
				}
			}
			else if(item.getType() == Material.MONSTER_EGG && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)){
				event.setCancelled(true);
				ItemUtil.removeItems(gPlayer.getPlayer().getInventory(),item,1);
				game.getTeams().getPlayerTeam(gPlayer).spawnSheep(gPlayer.getPlayer().getLocation());
			}
		}
	}

	@EventHandler
	public void InventoryClickEvent(InventoryClickEvent event){
		if(game.getState().isGame()){
			if(event.getInventory().getTitle().equalsIgnoreCase("Obchod")){
				game.getShop().onPlayerClick(event);
			}
		}
		else event.setCancelled(true);
	}

	@EventHandler
	public void InventoryDragEvent(InventoryDragEvent event){
		if(event.getInventory().getTitle().equalsIgnoreCase("Obchod")){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void NPCRightClickEvent(NPCRightClickEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getClicker());
		if(gPlayer.getState() != GamePlayerState.SPECTATOR){
			if(event.getNPC().getEntity().getType() == EntityType.VILLAGER){
				game.getShop().open(gPlayer);
			}
		}
	}

	@EventHandler
	public void NPCLeftClickEvent(NPCLeftClickEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getClicker());
		if(gPlayer.getState() != GamePlayerState.SPECTATOR){
			if(event.getNPC().getEntity().getType() == EntityType.VILLAGER){
				game.getShop().open(gPlayer);
			}
		}
	}
}