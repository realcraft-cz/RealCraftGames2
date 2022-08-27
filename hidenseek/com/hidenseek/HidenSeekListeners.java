package com.hidenseek;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.games.Games;
import com.games.events.*;
import com.games.game.GameState;
import com.games.game.GameStats.GameStatsType;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.hidenseek.HidenSeekTeam.HidenSeekTeamType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import realcraft.bukkit.RealCraft;
import realcraft.bukkit.coins.Coins;
import realcraft.bukkit.users.Users;
import realcraft.bukkit.utils.Title;

public class HidenSeekListeners implements Listener {

	private HidenSeek game;

	public HidenSeekListeners(HidenSeek game){
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this,Games.getInstance());
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Games.getInstance(),ListenerPriority.HIGH,PacketType.Play.Client.USE_ENTITY,PacketType.Play.Server.BLOCK_CHANGE,PacketType.Play.Server.PLAYER_INFO,PacketType.Play.Client.BLOCK_DIG){
			@Override
			public void onPacketSending(PacketEvent event){
				if(event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE){
					for(HidenSeekUser user : game.getUsers()){
						if(user.getOriginalBlock() != null){
							Location location = event.getPacket().getBlockPositionModifier().read(0).toLocation(user.getOriginalBlock().getWorld());
							Material material = event.getPacket().getBlockData().read(0).getType();
							if(material == Material.AIR && location.equals(user.getOriginalBlock().getLocation())){
								event.setCancelled(true);
							}
						}
					}
				}
				else if(event.getPacketType() == PacketType.Play.Server.PLAYER_INFO){
					if(event.getPacket().getPlayerInfoAction().read(0) == PlayerInfoAction.REMOVE_PLAYER){
						String name = event.getPacket().getPlayerInfoDataLists().read(0).get(0).getProfile().getName();
						if(Bukkit.getServer().getPlayer(name) != null && Bukkit.getServer().getPlayer(name).isOnline()){
							event.setCancelled(true);
						}
					}
				}
			}
			@Override
			public void onPacketReceiving(PacketEvent event){
				Player player = event.getPlayer();
				if(game.getGamePlayer(player).getState() != GamePlayerState.SPECTATOR){
					if(event.getPacketType() == PacketType.Play.Client.USE_ENTITY){
						if(event.getPacket().getEnumEntityUseActions().read(0).getAction() == EntityUseAction.ATTACK){
							for(HidenSeekUser user : game.getUsers()){
								if(event.getPacket().getIntegers().read(0) == user.getEntityId()){
									Bukkit.getServer().getScheduler().runTask(Games.getInstance(),new Runnable(){
										@Override
										public void run(){
											if(game.getState() == GameState.INGAME){
												if(game.getTeams().getPlayerTeam(game.getGamePlayer(player)).getType() == HidenSeekTeamType.SEEKERS && game.getTeams().getPlayerTeam(user.getGamePlayer()).getType() == HidenSeekTeamType.HIDERS){
													user.getGamePlayer().getPlayer().damage(1,player);
												}
											}
										}
									});
								}
							}
						}
					}
					else if(event.getPacketType() == PacketType.Play.Client.BLOCK_DIG){
						for(HidenSeekUser user : game.getUsers()){
							if(user.getOriginalBlock() != null){
								if(event.getPacket().getPlayerDigTypes().read(0) == PlayerDigType.START_DESTROY_BLOCK){
									Location location = event.getPacket().getBlockPositionModifier().read(0).toLocation(user.getOriginalBlock().getWorld());
									if(location.equals(user.getOriginalBlock().getLocation())){
										Bukkit.getServer().getScheduler().runTask(Games.getInstance(),new Runnable(){
											@Override
											public void run(){
												if(game.getState() == GameState.INGAME){
													if(game.getTeams().getPlayerTeam(game.getGamePlayer(player)).getType() == HidenSeekTeamType.SEEKERS && game.getTeams().getPlayerTeam(user.getGamePlayer()).getType() == HidenSeekTeamType.HIDERS){
														user.getGamePlayer().getPlayer().damage(1,player);
													}
												}
											}
										});
									}
								}
							}
						}
					}
				}
			}
		});
	}

	public HidenSeek getGame(){
		return game;
	}

	@EventHandler
	public void GamePlayerJoinEvent(GamePlayerJoinEvent event){
		if(game.getState().isGame()) game.getScoreboard().updateForPlayer(event.getPlayer());
	}

	@EventHandler
	public void GamePlayerLeaveEvent(GamePlayerLeaveEvent event){
		if(game.getTeams().getPlayerTeam(event.getPlayer()) != null){
			game.getTeams().getPlayerTeam(event.getPlayer()).removePlayer(event.getPlayer());
		}
		game.getUser(event.getPlayer()).cancelDisguise();
		if(game.getState().isGame()) game.getTeams().autoBalancingAfterLeft();
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
			gPlayer.getSettings().setLong("died",0);
			gPlayer.getPlayer().setGameMode(GameMode.SURVIVAL);
			HidenSeekTeam team = game.getTeams().getPlayerTeam(gPlayer);
			gPlayer.getPlayer().teleport(team.getSpawnLocation());
			game.getUser(gPlayer).reset();
			if(team.getType() == HidenSeekTeamType.HIDERS) game.getUser(gPlayer).disguiseRandomBlock();
			game.loadGameInventory(gPlayer);
			game.getScoreboard().updateForPlayer(gPlayer);
		}
	}

	@EventHandler
	public void GameEndEvent(GameEndEvent event){
		game.getTeams().resetTeams();
		for(GamePlayer gPlayer : game.getPlayers()){
			game.getScoreboard().updateForPlayer(gPlayer);
			game.getUser(gPlayer).cancelDisguise();
		}
	}

	@EventHandler
	public void GameTimeoutEvent(GameTimeoutEvent event){
		game.setState(GameState.ENDING);
	}

	@EventHandler
	public void GameStateChangeEvent(GameStateChangeEvent event){
		if(game.getState() == GameState.ENDING){
			HidenSeekTeam winner = game.getTeams().getWinnerTeam();
			game.sendMessage(winner.getType().getChatColor()+winner.getType().toName()+" §fvyhrali tuto hru");
			for(GamePlayer gPlayer : game.getPlayers()){
				gPlayer.getPlayer().getInventory().clear();
				if(gPlayer.getState() != GamePlayerState.SPECTATOR && game.getTeams().getPlayerTeam(gPlayer).getType() == winner.getType()){
					if(game.getGameTime() < game.getGameTimeDefault()-60){
						int kdreward = (game.getConfig().getInt("reward.kill",0)*gPlayer.getSettings().getInt("kills"))-(game.getConfig().getInt("reward.death",0)*gPlayer.getSettings().getInt("deaths"));
						if(kdreward < 0) kdreward = 0;
						final int reward = Users.getUser(gPlayer.getPlayer()).giveCoins(
							(game.getConfig().getInt("reward.base",0))+
							kdreward+
							(winner.getType() == HidenSeekTeamType.HIDERS ? game.getConfig().getInt("reward.player",0)*(game.getStartPlayers()-winner.getPlayers().size()) : 0)
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
				ItemStack item = gPlayer.getPlayer().getInventory().getItem(0);
				if(item != null && item.getType() != Material.AIR && item.getType() != Material.IRON_AXE && item.getDurability() != (byte)0){
					gPlayer.getPlayer().updateInventory();
				}
				gPlayer.getPlayer().setFoodLevel(20);
				gPlayer.getPlayer().setSaturation(0);
				if(game.getTeams().getPlayerTeam(gPlayer).getType() == HidenSeekTeamType.SEEKERS){
					if(game.getUser(gPlayer).getSpawnTime() > 0){
						if (game.getUser(gPlayer).getSpawnTime() < System.currentTimeMillis()) {
							game.getUser(gPlayer).setSpawnTime(0);
							gPlayer.getPlayer().getWorld().playSound(gPlayer.getPlayer().getLocation(), Sound.ENTITY_HORSE_ARMOR, 1, 1);
							gPlayer.getPlayer().teleport(game.getTeams().getTeam(HidenSeekTeamType.HIDERS).getSpawnLocation());
						} else {
							Title.showActionTitle(gPlayer.getPlayer(),"§fZbyva §e"+Math.round(((game.getUser(gPlayer).getSpawnTime()-System.currentTimeMillis())/1000)+1)+" sekund§f do respawnu");
						}
					}
					Block block = gPlayer.getPlayer().getLocation().getBlock();
					if(block.getType() == Material.WATER || block.getType() == Material.LAVA){
						gPlayer.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,5*25,1),true);
					}
				}
				else if(game.getTeams().getPlayerTeam(gPlayer).getType() == HidenSeekTeamType.HIDERS){
					Block block = gPlayer.getPlayer().getLocation().getBlock();
					if(block.getType() == Material.WATER || block.getType() == Material.LAVA){
						gPlayer.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,5*25,1),true);
						gPlayer.getPlayer().damage(2);
					}
				}
			}
			if(game.getGameTimeDefault()-game.getGameTime() == 30){
				for(GamePlayer gPlayer : game.getPlayers()){
					if(gPlayer.getState() == GamePlayerState.SPECTATOR) continue;
					if(game.getTeams().getPlayerTeam(gPlayer).getType() == HidenSeekTeamType.HIDERS){
						Title.showActionTitle(gPlayer.getPlayer(),"§bHledaci vypusteni, schovejte se!");
						game.loadGameInventory(gPlayer);
					}
					else if(game.getTeams().getPlayerTeam(gPlayer).getType() == HidenSeekTeamType.SEEKERS){
						gPlayer.getPlayer().teleport(game.getTeams().getTeam(HidenSeekTeamType.HIDERS).getSpawnLocation());
					}
					gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_HORSE_ARMOR,1,1);
				}
			}
			else if(game.getGameTimeDefault()-game.getGameTime() >= 10 && game.getGameTimeDefault()-game.getGameTime() < 30){
				for(GamePlayer gPlayer : game.getPlayers()){
					Title.showActionTitle(gPlayer.getPlayer(),"§fZbyva §e"+(30-(game.getGameTimeDefault()-game.getGameTime()))+" sekund§f do vypusteni hledacu");
					gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_HAT,1,1);
				}
			}
			else if(game.getGameTime() == 60){
				for (GamePlayer gPlayer : game.getPlayers()) {
					game.sendMessage("§ePosledni minuta!");
					gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(), Sound.ITEM_GOAT_HORN_SOUND_0, 1000f, 1f);
				}

				for(GamePlayer gPlayer : game.getTeams().getTeam(HidenSeekTeamType.SEEKERS).getPlayers()){
					game.loadGameInventory(gPlayer);
					gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ITEM_PICKUP,1,1);
					Title.showActionTitle(gPlayer.getPlayer(),"§fObdrzel jsi §b§lLokalizator§f");
				}
			}
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
			killer.playSound(killer.getLocation(),Sound.ENTITY_PLAYER_LEVELUP,1f,1f);
			game.sendMessage("§c\u271E §b"+killer.getName()+" §7zabil hrace §b"+player.getName());
		}
		else game.sendMessage("§c\u271E §b"+player.getName()+" §7zemrel");

		if(game.getTeams().getPlayerTeam(gPlayer).getType() == HidenSeekTeamType.HIDERS){
			game.getUser(gPlayer).cancelDisguise();
			game.getTeams().setPlayerTeam(gPlayer,game.getTeams().getTeam(HidenSeekTeamType.SEEKERS));
			gPlayer.getSettings().setLong("died",System.currentTimeMillis());
		}
	}

	@EventHandler
	public void PlayerRespawnEvent(PlayerRespawnEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		event.setRespawnLocation(game.getTeams().getPlayerTeam(gPlayer).getSpawnLocation());
		gPlayer.resetPlayer();
		game.loadGameInventory(gPlayer);
		game.getScoreboard().updateForPlayer(gPlayer);
		game.getUser(gPlayer).setSpawnTime(System.currentTimeMillis()+(20*1000));
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
		if(event.getDamager() instanceof Player && event.getEntity() instanceof Player){
			GamePlayer gPlayer = game.getGamePlayer((Player)event.getEntity());
			GamePlayer gAttacker = game.getGamePlayer((Player)event.getDamager());
			if(game.getTeams().getPlayerTeam(gAttacker).getType() == HidenSeekTeamType.SEEKERS){
				if(!game.getUser(gAttacker).isWeaponActive() || gAttacker.getPlayer().getInventory().getItemInMainHand().getType() != Material.IRON_AXE){
					event.setCancelled(true);
					System.out.println(game.getUser(gAttacker).isWeaponActive());
					return;
				}
				event.setDamage(6);
				gAttacker.getPlayer().spawnParticle(Particle.CRIT, gPlayer.getPlayer().getLocation().add(0, 1, 0), 20, 0.2f, 0.4f, 0.2f, 0.4f);
				game.getUser(gPlayer).setSolid(false);
			}
			else if(game.getTeams().getPlayerTeam(gAttacker).getType() == HidenSeekTeamType.HIDERS) event.setDamage(2);
			gAttacker.getPlayer().getWorld().playSound(event.getEntity().getLocation(),Sound.ENTITY_PLAYER_HURT,1,1);
		}
		else if(event.getDamager() instanceof Player && event.getEntity() instanceof Animals){
			event.setCancelled(true);
		}
		else if(event.getEntity().getType() == EntityType.ARMOR_STAND || event.getEntity().getType() == EntityType.FIREWORK){
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void EntityChangeBlockEvent(EntityChangeBlockEvent event){
		if(event.getEntityType() == EntityType.FALLING_BLOCK){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void PlayerInteractEvent(PlayerInteractEvent event){
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		if(game.getGamePlayer(event.getPlayer()).getState() == GamePlayerState.SPECTATOR) return;
		if(game.getState().isGame()){
			if(event.getHand() == EquipmentSlot.HAND){
				ItemStack itemStack = gPlayer.getPlayer().getInventory().getItemInMainHand();
				if(itemStack.getType() == Material.SLIME_BALL){
					if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
						if(game.getUser(gPlayer).getBlockTime() < System.currentTimeMillis()){
							Block block = event.getClickedBlock();
							if(block != null && game.isBlockValid(block)){
								game.getUser(gPlayer).disguiseBlock(block);
								gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_BAT_HURT,1f,1f);
								game.getUser(gPlayer).setBlockTime(System.currentTimeMillis()+(10*1000));
								gPlayer.getPlayer().setCooldown(Material.SLIME_BALL, 10*20);
							} else {
								gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ITEM_BREAK,1f,1f);
								Title.showActionTitle(event.getPlayer(),"§c\u2716 §fNelze se promenit za tento blok §c\u2716");
							}
						} else {
							gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ITEM_BREAK,1f,1f);
							Title.showActionTitle(event.getPlayer(),"§c\u2716 §fZmenu bloku muzes pouzit za "+Math.round((game.getUser(gPlayer).getBlockTime()-System.currentTimeMillis())/1000)+" sekund §c\u2716");
						}
					}
					event.setCancelled(true);
				}
				else if(itemStack.getType() == Material.SUGAR){
					if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
						if(game.getUser(gPlayer).getMeowTime() < System.currentTimeMillis()){
							gPlayer.getPlayer().getWorld().spawnParticle(Particle.NOTE, gPlayer.getPlayer().getLocation().add(0, 1, 0), 8, 0.4, 0.4, 0.4, 1f);
							gPlayer.getPlayer().getLocation().getWorld().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_CAT_AMBIENT,0.1f,1.0f);
							gPlayer.getPlayer().getLocation().getWorld().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_CAT_AMBIENT,0.1f,1.0f);
							game.getUser(gPlayer).setMeowTime(System.currentTimeMillis()+1000);
						}
					}
					event.setCancelled(true);
				}
				else if(itemStack.getType() == Material.RED_CANDLE){
					if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
						if(game.getUser(gPlayer).getFireworkTime() < System.currentTimeMillis()){
							Item firecracker = gPlayer.getPlayer().getWorld().dropItem(gPlayer.getPlayer().getEyeLocation(), new ItemStack(Material.RED_CANDLE));
							firecracker.setPickupDelay(Integer.MAX_VALUE);
							firecracker.setVelocity(gPlayer.getPlayer().getLocation().getDirection().multiply(0.4).add(gPlayer.getPlayer().getVelocity()));
							firecracker.getWorld().playSound(firecracker.getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);

							new BukkitRunnable() {
								private int count;

								@Override
								public void run() {
									if (firecracker.getTicksLived() > 10*20) {
										firecracker.remove();
										this.cancel();
										return;
									}

									if (firecracker.isOnGround()) {
										count ++;

										if (count < 6) {
											firecracker.getWorld().spawnParticle(Particle.SMOKE_NORMAL, firecracker.getLocation().add(0, 0.8, 0), 4, 0, 0.1, 0, 0);
											return;
										}

										if (count == 6) {
											firecracker.getWorld().playSound(firecracker.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 3f, 1f);
											firecracker.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, firecracker.getLocation().add(0, 0.2, 0), 1, 0, 0, 0, 0);
										}

										if (count == 7 || count == 8 || count == 9) {
											firecracker.getWorld().playSound(firecracker.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 3f, 1f);
											firecracker.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, firecracker.getLocation().add(0, 0.2, 0), 8, 0.2, 0.2, 0.2, 0.1f);
											firecracker.getWorld().spawnParticle(Particle.WAX_ON, firecracker.getLocation().add(0, 0.2, 0), 8, 0.2, 0.2, 0.2, 10f);
											firecracker.getWorld().spawnParticle(Particle.LAVA, firecracker.getLocation().add(0, 0.2, 0), 4, 0.2, 0.2, 0.2, 1f);
										}

										if (!firecracker.isDead()) {
											firecracker.remove();
										}

										if (count >= 9) {
											this.cancel();
										}
									}
								}
							}.runTaskTimer(Games.getInstance(), 6, 6);

							gPlayer.getPlayer().setCooldown(Material.RED_CANDLE, 30 * 20);
							game.getUser(gPlayer).setFireworkTime(System.currentTimeMillis()+(30*1000));
							game.getUser(gPlayer).setFireworks(game.getUser(gPlayer).getFireworks()-1);
							itemStack.setAmount(game.getUser(gPlayer).getFireworks());
							gPlayer.getPlayer().getInventory().setItem(5,itemStack);
							if(game.getUser(gPlayer).getFireworks() == 0){
								int coins = Users.getUser(gPlayer.getPlayer()).giveCoins(20);
								game.sendMessage(gPlayer,"§eZiskal jsi §a+"+coins+" coins §eza odpaleni petard");
							} else {
								Title.showActionTitle(event.getPlayer(),"§fOdpal vsech §e5 petard§f a ziskej §a+20 coins",6*20);
							}
						}
					}
					event.setCancelled(true);
				}
				else if(itemStack.getType() == Material.IRON_AXE){
					if(event.getAction() == Action.LEFT_CLICK_BLOCK){
						if(game.getTeams().getPlayerTeam(gPlayer).getType() == HidenSeekTeamType.SEEKERS){
							game.getUser(gPlayer).useWeapon();
						}
					}
				}
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void PlayerInteractEntityEvent(PlayerInteractEntityEvent event){
		if(event.getHand() == EquipmentSlot.HAND){
			ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
			if(itemStack.getType() == Material.SLIME_BALL){
				event.setCancelled(true);
				GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
				Entity entity = event.getRightClicked();
				if(game.isEntityValid(entity.getType())){
					/*if(game.getUser(gPlayer).getBlockTime() < System.currentTimeMillis()){
						game.getUser(gPlayer).disguiseEntity(entity);
						gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_BAT_HURT,1f,1f);
						game.getUser(gPlayer).setBlockTime(System.currentTimeMillis()+(10*1000));
					} else {*/
					gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ITEM_BREAK,1f,1f);
					//Title.showActionTitle(gPlayer.getPlayer(),"§c\u2716 §fZmenu bloku muzes pouzit za "+Math.round((game.getUser(gPlayer).getBlockTime()-System.currentTimeMillis())/1000)+" sekund §c\u2716");
				}
			}
		}
	}

	@EventHandler
	public void InventoryClickEvent(InventoryClickEvent event){
		event.setCancelled(true);
	}

	@EventHandler
	public void PlayerMoveEvent(PlayerMoveEvent event){
		if(game.getState().isLobby()) return;
		GamePlayer gPlayer = game.getGamePlayer(event.getPlayer());
		if(gPlayer.getState() != GamePlayerState.SPECTATOR){
			if(event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockY() != event.getTo().getBlockY() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()){
				if(game.getTeams().getPlayerTeam(gPlayer).getType() == HidenSeekTeamType.HIDERS){
					game.getUser(gPlayer).setSolid(false);
				}
			}
		}
	}

	@EventHandler
	public void GameRegionLoadEvent(GameRegionLoadEvent event){
		((HidenSeekArena)event.getArena()).loadBlocks();
	}
}