package com.bedwars;

import com.bedwars.BedWarsResource.BedWarsResourceType;
import com.bedwars.specials.BedWarsSpecialSheep;
import com.bedwars.specials.BedWarsSpecialStray;
import com.games.Games;
import com.games.events.*;
import com.games.game.GameState;
import com.games.game.GameStats.GameStatsType;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import realcraft.bukkit.utils.Title;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import realcraft.bukkit.RealCraft;
import realcraft.bukkit.coins.Coins;
import realcraft.bukkit.users.Users;
import realcraft.bukkit.utils.ItemUtil;
import realcraft.bukkit.utils.LocationUtil;
import realcraft.bukkit.utils.MaterialUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BedWarsListeners implements Listener {

	private static final int SPAWN_PROTECTION_DELAY = 20*1000;

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
			gPlayer.getSettings().setInt("beds",0);
			gPlayer.getSettings().setLong("pickup",0);
			gPlayer.getSettings().setLong("spawned",0);
			gPlayer.getPlayer().setGameMode(GameMode.SURVIVAL);
			BedWarsTeam team = game.getTeams().getPlayerTeam(gPlayer);
			gPlayer.getPlayer().teleport(team.getSpawnLocation());
			game.getScoreboard().updateForPlayer(gPlayer);
		}
		Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				game.sendMessage("§7Pro psani vsem tymum zadej §e!§7 na zacatek zpravy");
			}
		},40);
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
		game.setState(GameState.ENDING);
	}

	@EventHandler
	public void GameStateChangeEvent(GameStateChangeEvent event){
		if(game.getState() == GameState.ENDING){
			BedWarsTeam winner = game.getTeams().getWinnerTeam();
			if(winner != null){
				game.sendMessage(winner.getType().getChatColor()+winner.getType().toName()+" §fvyhrali tuto hru");
				for(GamePlayer gPlayer : game.getPlayers()){
					if(gPlayer.getState() != GamePlayerState.SPECTATOR && game.getTeams().getPlayerTeam(gPlayer).getType() == winner.getType()){
						if(game.getGameTime() < game.getGameTimeDefault()-60){
							int kdreward = (game.getConfig().getInt("reward.kill",0)*gPlayer.getSettings().getInt("kills"))-(game.getConfig().getInt("reward.death",0)*gPlayer.getSettings().getInt("deaths"));
							if(kdreward < 0) kdreward = 0;
							final int reward = Users.getUser(gPlayer.getPlayer()).giveCoins(
								(game.getConfig().getInt("reward.base",0))+
								kdreward+
								(game.getConfig().getInt("reward.bed",0)*gPlayer.getSettings().getInt("beds"))
							);

							game.getStats().addScore(gPlayer,GameStatsType.WINS,1);

							Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
								public void run(){
									Coins.runCoinsEffect(gPlayer.getPlayer(),"§a§lVitezstvi!",reward);
								}
							},10*20);
						}
						Title.showTitle(gPlayer.getPlayer(),"§a§lVitezstvi!",0.5,8,0.5);
						Title.showSubTitle(gPlayer.getPlayer(),"§fTvuj tym vyhral tuto hru",0.5,8,0.5);
					} else {
						Title.showTitle(gPlayer.getPlayer(),"§c§lProhra",0.5,8,0.5);
						Title.showSubTitle(gPlayer.getPlayer(),winner.getType().getChatColor()+winner.getType().toName()+" §fvyhrali tuto hru",0.5,8,0.5);
					}
				}
			} else {
				game.sendMessage("§fNikdo tuto hru nevyhral");
				for(GamePlayer gPlayer : game.getPlayers()){
					Title.showTitle(gPlayer.getPlayer(),"§c§lProhra",0.5,8,0.5);
					Title.showSubTitle(gPlayer.getPlayer(),"§fNikdo tuto hru nevyhral",0.5,8,0.5);
				}
			}
			for(GamePlayer gPlayer : game.getPlayers()){
				gPlayer.getPlayer().getInventory().clear();
				if(gPlayer.getSettings().getInt("kills") > 0) game.getStats().addScore(gPlayer,GameStatsType.KILLS,gPlayer.getSettings().getInt("kills"));
				if(gPlayer.getSettings().getInt("deaths") > 0) game.getStats().addScore(gPlayer,GameStatsType.DEATHS,gPlayer.getSettings().getInt("deaths"));
				gPlayer.getSettings().setInt("kills",0);
				gPlayer.getSettings().setInt("deaths",0);
				gPlayer.getSettings().setInt("beds",0);
			}
		}
	}

	@EventHandler
	public void GameCycleEvent(GameCycleEvent event){
		if(game.getState() == GameState.INGAME){
			if(game.getTeams().getActiveTeams().size() < 2 && !RealCraft.isTestServer()) game.setState(GameState.ENDING);
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
			gPlayer.getSettings().setLong("spawned",System.currentTimeMillis());
		}
		else if(gPlayer.getState() == GamePlayerState.SPECTATOR){
			gPlayer.resetPlayer();
			gPlayer.setState(GamePlayerState.SPECTATOR);
			gPlayer.toggleSpectator();
			event.setRespawnLocation(game.getArena().getSpectator());
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
		if(MaterialUtil.isBed(block.getType())){
			BedWarsTeam team = game.getTeams().getPlayerTeam(gPlayer);

			Block breakBlock = block;
			Block neighbor;
			Bed breakBed = (Bed) breakBlock.getBlockData();

			if(breakBed.getPart() == Bed.Part.FOOT){
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
		if(game.getArena().getRegion().isLocationInside(block.getLocation())){
			game.getArena().addPlayerBlock(block);
			if(block.getType() == Material.ENDER_CHEST){
				game.getTeams().getPlayerTeam(gPlayer).addChest(block);
			}
		}
		else event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled=true)
	public void EntityShootBowEvent(EntityShootBowEvent event){
		int level = event.getBow().getEnchantmentLevel(Enchantment.DURABILITY);
		int damage = 9;
		if(level == 1) damage = 8;
		else if(level == 2) damage = 7;
		else if(level == 3) damage = 6;
		event.getBow().setDurability((short)(event.getBow().getDurability()+damage));
		if(event.getEntityType() == EntityType.PLAYER){
			GamePlayer gPlayer = game.getGamePlayer((Player)event.getEntity());
			if(game.getState().isGame() && gPlayer.getSettings().getLong("spawned") != 0){
				gPlayer.getSettings().setLong("spawned",0);
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void EntityExplodeEvent(EntityExplodeEvent event){
		if(event.getEntityType() == EntityType.PRIMED_TNT){
			List<Block> blocks = new ArrayList<Block>(event.blockList());
			for(Block block : blocks){
				if(game.getArena().getRegion().isLocationInside(block.getLocation()) && !game.getArena().isPlayerBlock(block)){
					event.blockList().remove(block);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void EntityDamageEvent(EntityDamageEvent event){
		if(event.getEntityType() == EntityType.STRAY && (event.getCause() == DamageCause.FIRE_TICK)){
			event.setCancelled(true);
			event.getEntity().setFireTicks(0);
		}
		else if(event.getEntityType() == EntityType.PLAYER &&
				(event.getCause() == DamageCause.ENTITY_ATTACK ||
				event.getCause() == DamageCause.ENTITY_SWEEP_ATTACK ||
				event.getCause() == DamageCause.PROJECTILE ||
				event.getCause() == DamageCause.FIRE ||
				event.getCause() == DamageCause.FIRE_TICK)){
			GamePlayer gPlayer = game.getGamePlayer((Player)event.getEntity());
			if(gPlayer.getState() != GamePlayerState.SPECTATOR && gPlayer.getSettings().getLong("spawned")+SPAWN_PROTECTION_DELAY > System.currentTimeMillis()){
				event.setCancelled(true);
				event.getEntity().setFireTicks(0);
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void PlayerMoveEvent(PlayerMoveEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		if(event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockY() != event.getTo().getBlockY() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()){
			if(game.getState().isGame() && gPlayer.getState() != GamePlayerState.SPECTATOR && gPlayer.getSettings().getLong("spawned") != 0 && System.currentTimeMillis()-gPlayer.getSettings().getLong("spawned") > 500){
				gPlayer.getSettings().setLong("spawned",0);
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void EntityDeathEvent(EntityDeathEvent event){
		if(event.getEntityType() == EntityType.STRAY){
			event.setDroppedExp(0);
			event.getDrops().clear();
		}
	}

	@EventHandler
	public void EntityTargetEvent(EntityTargetEvent event){
		if(event.getEntityType() == EntityType.STRAY) event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled=true)
    public void EntityPickupItemEvent(EntityPickupItemEvent event){
		if(event.getEntity() instanceof Player){
			GamePlayer gPlayer = game.getGamePlayer((Player)event.getEntity());
			if(gPlayer.getSettings().getLong("pickup") < System.currentTimeMillis()-40){
				gPlayer.getSettings().setLong("pickup",System.currentTimeMillis());
			} else {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void ItemMergeEvent(ItemMergeEvent event){
		if(event.getEntity().getItemStack().getType() == BedWarsResourceType.BRONZE.toMaterial()
			|| event.getEntity().getItemStack().getType() == BedWarsResourceType.IRON.toMaterial()
			|| event.getEntity().getItemStack().getType() == BedWarsResourceType.GOLD.toMaterial()){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void PlayerInteractEvent(PlayerInteractEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		if(game.getGamePlayer(event.getPlayer()).getState() == GamePlayerState.SPECTATOR) return;
		if(game.getState().isLobby()){
			ItemStack itemStack = gPlayer.getPlayer().getInventory().getItemInMainHand();
			if(MaterialUtil.isWool(itemStack.getType())){
				if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
					event.setCancelled(true);
					for(BedWarsTeam team : game.getTeams().getTeams()){
						if(MaterialUtil.getDyeColor(itemStack.getType()) == team.getType().getDyeColor()){
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
			else if(MaterialUtil.isSpawnEgg(item.getType()) && MaterialUtil.getSpawnEggType(item.getType()) == EntityType.SHEEP && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)){
				event.setCancelled(true);
				ItemUtil.removeItems(gPlayer.getPlayer().getInventory(),item,1);
				BedWarsSpecialSheep sheep = new BedWarsSpecialSheep(game,game.getTeams().getPlayerTeam(gPlayer));
				sheep.activate(gPlayer);
			}
			else if(MaterialUtil.isSpawnEgg(item.getType()) && MaterialUtil.getSpawnEggType(item.getType()) == EntityType.STRAY && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)){
				event.setCancelled(true);
				ItemUtil.removeItems(gPlayer.getPlayer().getInventory(),item,1);
				BedWarsSpecialStray stray = new BedWarsSpecialStray(game,game.getTeams().getPlayerTeam(gPlayer));
				stray.activate(gPlayer);
			}
			if(gPlayer.getSettings().getLong("spawned") != 0) gPlayer.getSettings().setLong("spawned",0);
		}
	}

	@EventHandler
	public void InventoryClickEvent(InventoryClickEvent event){
		if(game.getState().isGame()){
			if(event.getView().getTitle().equalsIgnoreCase("Obchod")){
				game.getShop().onPlayerClick(event);
			}
		}
		else event.setCancelled(true);
	}

	@EventHandler
	public void InventoryDragEvent(InventoryDragEvent event){
		if(event.getView().getTitle().equalsIgnoreCase("Obchod")){
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

	@EventHandler(priority = EventPriority.HIGH,ignoreCancelled=true)
	public void AsyncPlayerChatEvent(AsyncPlayerChatEvent event){
		String message = event.getMessage();
		boolean forTeam = true;
		if(message.startsWith("!") || message.startsWith("@")){
			message = message.substring(1,message.length());
			event.setMessage(message.trim());
			forTeam = false;
		}
		if(forTeam){
			if(game.getState() == GameState.INGAME){
				GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
				if(gPlayer.getState() != GamePlayerState.SPECTATOR){
					event.setCancelled(true);
					RealCraft.getInstance().chatlog.onPlayerChat(event.getPlayer(),message);
					message = game.getTeams().getPlayerTeam(gPlayer).getType().getChatColor()+"[Team] "+gPlayer.getPlayer().getDisplayName()+": "+ChatColor.GRAY+message;
					for(GamePlayer gPlayer2 : game.getTeams().getPlayerTeam(gPlayer).getPlayers()){
						gPlayer2.getPlayer().sendMessage(message);
					}
				}
			}
		}
	}
}