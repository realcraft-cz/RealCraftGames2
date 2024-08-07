package com.games.game;

import com.games.Games;
import com.games.arena.GameArena;
import com.games.events.*;
import com.games.exceptions.GameMaintenanceException;
import com.games.exceptions.GameMaxPlayersException;
import com.games.exceptions.GameNotLoadedException;
import com.games.game.GameSpectator.SpectatorMenuItem;
import com.games.game.GameStats.GameStatsType;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.StringUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import realcraft.bukkit.RealCraft;
import realcraft.bukkit.lobby.LobbyAutoParkour;
import realcraft.bukkit.lobby.LobbyMenu;
import realcraft.bukkit.minihry.GamesReminder;
import realcraft.bukkit.users.Users;
import realcraft.bukkit.utils.LocationUtil;
import realcraft.bukkit.utils.Title;
import realcraft.share.ServerType;
import realcraft.share.users.UserRank;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class Game implements Runnable {

	public static final String MAPS = "minigames_maps";
	private static final int PREMIUM_OFFER_TIMEOUT = 7200;

	private GameType type;
	private GameState state = GameState.LOBBY;
	private GameArena arena;
	private FileConfiguration config;

	protected HashMap<String,GamePlayer> players = new HashMap<String,GamePlayer>();
	private HashMap<Integer,GameArena> arenas = new HashMap<Integer,GameArena>();
	private HashMap<String,Long> playersPremiumOffers = new HashMap<String,Long>();
	private Location lobbyLocation;
	private LobbyScoreboard lobbyScoreboard;
	private LobbyBossBar lobbyBossBar;
	private GameLeaderboard gameLeaderboard;
	private GameVoting gameVoting;
	private GameStats gameStats;
	private GameSpectator gameSpectator;

	private boolean maintenance;
	private boolean loaded = false;
	private int lobbyTimeDefault;
	private int lobbyTime;
	private int gameTimeDefault;
	private int gameTime;
	private int endTimeDefault;
	private int endTime;
	private int minPlayers;
	private int maxPlayers;
	private String prefix;
	private int startPlayers;
	private long lastReminder;
	private static final String[] numbers = new String[]{"�c\u278A","�6\u278B","�e\u278C","�e\u278D","�e\u278E"};

	public Game(GameType type){
		this.type = type;
		this.maintenance = this.getConfig().getBoolean("maintenance",false);
		this.lobbyTimeDefault = this.getConfig().getInt("lobbyTime");
		this.gameTimeDefault = this.getConfig().getInt("gameTime");
		this.endTimeDefault = this.getConfig().getInt("endTime");
		this.minPlayers = this.getConfig().getInt("minPlayers");
		this.maxPlayers = this.getConfig().getInt("maxPlayers");
		this.prefix = ChatColor.translateAlternateColorCodes('&',this.getConfig().getString("prefix"));
		this.lobbyTime = lobbyTimeDefault;
		this.gameTime = gameTimeDefault;
		this.endTime = endTimeDefault;
		this.lobbyScoreboard = new LobbyScoreboard(this);
		this.lobbyBossBar = new LobbyBossBar(this);
		this.gameLeaderboard = new GameLeaderboard(this);
		this.gameVoting = new GameVoting(this);
		this.gameStats = new GameStats(this);
		this.gameSpectator = new GameSpectator(this);
		new GameListeners(this);
		for(World world : Bukkit.getWorlds()){
			world.setGameRuleValue("doDaylightCycle","false");
			world.setGameRuleValue("doWeatherCycle","false");
			world.setFullTime(6000);
		}
		if(!this.isMaintenance()){
			Bukkit.getScheduler().scheduleSyncRepeatingTask(Games.getInstance(),this,20,20);
			Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					gameVoting.resetVoting();
					gameLeaderboard.update();
				}
			}, 40);
			Bukkit.getScheduler().runTask(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					Game.this.resetArenas();
				}
			});
		}
		new LobbyMenu(RealCraft.getInstance());
		new LobbyAutoParkour(RealCraft.getInstance());
	}

	public abstract HashMap<Integer,SpectatorMenuItem> getSpectatorMenuItems();

	@Override
	public void run(){
		if(this.isMaintenance()) return;
		this.removeOfflinePlayers();
		if(this.getState() == GameState.LOBBY){
			if(this.getPlayers().size() >= this.getMinPlayers()){
				this.setState(GameState.STARTING);
			}
		}
		else if(this.getState() == GameState.STARTING){
			lobbyTime --;
			if(lobbyTime > 0){
				if(lobbyTime <= 10){
					for(GamePlayer gPlayer : this.getPlayers()){
						gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_HAT,1,1);
						if(lobbyTime <= 5) Title.showTitle(gPlayer.getPlayer(),numbers[lobbyTime-1],0,1.2,0);
					}
				}
			} else {
				this.setArena(gameVoting.getWinningArena());
				this.setState(GameState.INGAME);
				this.setStartPlayers(this.getPlayersCount());
				this.getArena().getWorld().setFullTime(this.getArena().getTime());
				for(GamePlayer gPlayer : this.getPlayers()){
					lobbyScoreboard.updateForPlayer(gPlayer);
					lobbyBossBar.updateForPlayer(gPlayer);
					this.getStats().addScore(gPlayer,GameStatsType.GAMES,1);
				}
				Bukkit.getServer().getPluginManager().callEvent(new GameStartEvent(this));
				Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
					@Override
					public void run(){
						Game.this.getArena().getRegion().clearEntities();
					}
				},10);
			}
		}
		if(this.getState().isLobby()){
			lobbyScoreboard.update();
			lobbyBossBar.update();
			gameVoting.runEffects();
		}
		else if(this.getState().isGame()){
			if(this.getState() == GameState.INGAME){
				if(gameTimeDefault != -1 && gameTime > 0){
					gameTime --;
					if(gameTime == 0) Bukkit.getServer().getPluginManager().callEvent(new GameTimeoutEvent(this));
				}
			}
			else if(this.getState() == GameState.ENDING){
				if(endTime > 0){
					endTime --;
					if(endTime == 0) Bukkit.getServer().getPluginManager().callEvent(new GameEndEvent(this));
				}
			}
			Bukkit.getServer().getPluginManager().callEvent(new GameCycleEvent(this));
		}
	}

	public void onDisable(){
		new Thread(new Runnable(){
			public void run(){
				for(int i=0;i<5;i++){
					try {
						Thread.sleep(i*200);
					} catch (InterruptedException e){
						e.printStackTrace();
					}
					for(World world : Bukkit.getWorlds()){
						File [] mapsFiles = new File(world.getWorldFolder()+"/data/").listFiles();
						if(mapsFiles != null){
							for(File file : mapsFiles){
								if(!file.isDirectory() && (file.getName().startsWith("map_") || file.getName().startsWith("idcounts"))){
									file.delete();
								}
							}
						}
					}
				}
			}
		}).start();
	}

	public GameType getType(){
		return type;
	}

	public GameState getState(){
		return state;
	}

	public void setState(GameState state){
		this.state = state;
		Bukkit.getServer().getPluginManager().callEvent(new GameStateChangeEvent(this));
	}

	public GameArena getArena(){
		return arena;
	}

	public void setArena(GameArena arena){
		this.arena = arena;
	}

	public FileConfiguration getConfig(){
		if(config == null){
			File file = new File(Games.getInstance().getDataFolder()+"/"+this.getType().getName()+"/"+"config.yml");
			if(file.exists()){
				config = new YamlConfiguration();
				try {
					config.load(file);
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		return config;
	}

	public ArrayList<GamePlayer> getPlayers(){
		return new ArrayList<GamePlayer>(players.values());
	}

	public ArrayList<GamePlayer> getGamePlayers(){
		ArrayList<GamePlayer> tmpPlayers = new ArrayList<GamePlayer>();
		for(GamePlayer gPlayer : this.getPlayers()){
			if(gPlayer.getState() != GamePlayerState.SPECTATOR) tmpPlayers.add(gPlayer);
		}
		return tmpPlayers;
	}

	public int getPlayersCount(){
		int count = 0;
		for(GamePlayer gPlayer : this.getPlayers()) if(gPlayer.getState() != GamePlayerState.SPECTATOR) count ++;
		return count;
	}

	public int getStartPlayers(){
		return startPlayers;
	}

	public void setStartPlayers(int startPlayers){
		this.startPlayers = startPlayers;
	}

	public GamePlayer getGamePlayer(Player player){
		if(!players.containsKey(player.getName())) players.put(player.getName(),new GamePlayer(player,this));
		return players.get(player.getName());
	}

	private void removeOfflinePlayers(){
		ArrayList<GamePlayer> toRemove = new ArrayList<GamePlayer>();
		for(GamePlayer gPlayer : this.getPlayers()){
			if(!gPlayer.getPlayer().isOnline()) toRemove.add(gPlayer);
		}
		for(GamePlayer gPlayer : toRemove) this.removePlayer(gPlayer);
	}

	public ArrayList<GameArena> getArenas(){
		return new ArrayList<GameArena>(arenas.values());
	}

	public void addArena(GameArena arena){
		arenas.put(arena.getId(),arena);
		arena.load();
	}

	public void resetArenas(){
		for(GameArena arena : Game.this.getArenas()){
			if(!arena.isLoaded()){
				Games.DEBUG("Reseting map: "+arena.getName());
				arena.resetRegion();
				return;
			}
		}
		Games.DEBUG("Reseting complete");
		this.setLoaded(true);
	}

	public Location getLobbyLocation(){
		if(lobbyLocation == null) {
			if (this.getConfig().isSet("lobby")) {
				lobbyLocation = LocationUtil.getConfigLocation(this.getConfig(), "lobby");
			} else {
				lobbyLocation = LocationUtil.getConfigLocation(Games.getInstance().getConfig(), "lobby");
			}
		}
		return lobbyLocation;
	}

	public LobbyScoreboard getLobbyScoreboard(){
		return lobbyScoreboard;
	}

	public LobbyBossBar getLobbyBossBar(){
		return lobbyBossBar;
	}

	public GameLeaderboard getLeaderboard(){
		return gameLeaderboard;
	}

	public GameVoting getVoting(){
		return gameVoting;
	}

	public GameStats getStats(){
		return gameStats;
	}

	public GameSpectator getSpectator(){
		return gameSpectator;
	}

	public boolean isMaintenance(){
		return maintenance;
	}

	public boolean isLoaded(){
		return loaded;
	}

	public void setLoaded(boolean loaded){
		this.loaded = loaded;
	}

	public int getLobbyTime(){
		return lobbyTime;
	}

	public int getLobbyTimeDefault(){
		return lobbyTimeDefault;
	}

	public void resetLobbyTime(){
		lobbyTime = lobbyTimeDefault;
	}

	public int getGameTime(){
		return gameTime;
	}

	public int getGameTimeDefault(){
		return gameTimeDefault;
	}

	public void resetGameTime(){
		gameTime = gameTimeDefault;
	}

	public void setGameTime(int gameTime){
		this.gameTime = gameTime;
	}

	public int getEndTime(){
		return endTime;
	}

	public int getEndTimeDefault(){
		return endTimeDefault;
	}

	public void resetEndTime(){
		endTime = endTimeDefault;
	}

	public int getMinPlayers(){
		return minPlayers;
	}

	public int getMaxPlayers(){
		return maxPlayers;
	}

	public String getPrefix(){
		return prefix;
	}

	public void tryToConnect(Player player) throws GameMaintenanceException, GameNotLoadedException, GameMaxPlayersException {
		if(this.isMaintenance() && !player.hasPermission("group.Admin") && !player.hasPermission("group.Builder")) throw new GameMaintenanceException();
		if(!this.isLoaded() && !player.hasPermission("group.Admin") && !player.hasPermission("group.Builder")) throw new GameNotLoadedException();
		if(this.getPlayers().size() >= this.getMaxPlayers() && !player.hasPermission("group.Admin") && !player.hasPermission("group.Builder")) throw new GameMaxPlayersException();
	}

	public void joinPlayer(Player player) throws GameMaintenanceException, GameNotLoadedException, GameMaxPlayersException {
		this.tryToConnect(player);
		GamePlayer gPlayer = this.getGamePlayer(player);
		gPlayer.resetPlayer();
		gPlayer.getPlayer().teleport(this.getLobbyLocation());
		if(this.getState().isLobby()){
			gPlayer.getPlayer().getInventory().setItem(0,LobbyMenu.getItem());

			lobbyScoreboard.updateForPlayer(gPlayer);
			lobbyBossBar.updateForPlayer(gPlayer);

			Bukkit.getServer().getPluginManager().callEvent(new GamePlayerJoinEvent(this,gPlayer));

			Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					gPlayer.getPlayer().teleport(Game.this.getLobbyLocation());
					Games.getEssentials().getUser(gPlayer.getPlayer()).setNickname(Games.getEssentials().getUser(gPlayer.getPlayer()).getName());
					Games.getEssentials().getUser(gPlayer.getPlayer()).setDisplayNick();
				}
			},5);

			if (Bukkit.getOnlinePlayers().size() == 1) {
				Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
					@Override
					public void run(){
						Game.this.getLeaderboard().update();
					}
				}, 20);
			}
		} else {
			gPlayer.setState(GamePlayerState.SPECTATOR);
			Bukkit.getServer().getPluginManager().callEvent(new GamePlayerJoinEvent(this,gPlayer));
			Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					gPlayer.teleportToSpectatorLocation();
					gPlayer.toggleSpectator();
				}
			},5);
		}
	}

	public void leavePlayer(GamePlayer gPlayer){
		gPlayer.connectToServer(ServerType.LOBBY);
	}

	public void removePlayer(GamePlayer gPlayer){
		players.remove(gPlayer.getPlayer().getName());
		gameVoting.removePlayer(gPlayer);
		if(this.getState() == GameState.STARTING && this.getPlayers().size() < this.getMinPlayers()){
			this.setState(GameState.LOBBY);
		}
		/*for(GamePlayer gPlayer2 : this.getPlayers()){
			PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e,((CraftPlayer)gPlayer.getPlayer()).getHandle());
			((CraftPlayer)gPlayer2.getPlayer()).getHandle().b.a(packet);
		}*/
		Bukkit.getServer().getPluginManager().callEvent(new GamePlayerLeaveEvent(this,gPlayer));
	}

	public class LobbyScoreboard extends GameScoreboard {
		public LobbyScoreboard(Game game){
			super(game,GameScoreboardType.LOBBY);
			this.setTitle(this.getGame().getType().getColor()+""+ChatColor.BOLD+this.getGame().getType().getName());
			this.setLine(0,"");
			this.setLine(2,"");
			this.setLine(5,"");
			this.setLine(6,"�ewww.realcraft.cz");
			this.update();
		}

		public void update(){
			this.setLine(1,"�fHraci: �a"+this.getGame().getPlayers().size()+"/"+this.getGame().getMaxPlayers());
			if(this.getGame().getState() == GameState.LOBBY){
				int players = (this.getGame().getMinPlayers()-this.getGame().getPlayers().size());
				if(players > 0){
					this.setLine(3,"�fCekame na");
					this.setLine(4,"�f"+players+" "+StringUtil.inflect(players,new String[]{"dalsiho","dalsi","dalsich"})+" "+StringUtil.inflect(players,new String[]{"hrace","hrace","hracu"}));
				}
			}
			else if(this.getGame().getState() == GameState.STARTING){
				this.setLine(3,"�fHra zacina");
				this.setLine(4,"�fza �a"+this.getGame().getLobbyTime()+" �fsekund");
			}
			super.update();
		}

		public void updateForPlayer(GamePlayer gPlayer){
			if(this.getGame().getState().isLobby()) this.addPlayer(gPlayer);
			else this.removePlayer(gPlayer);
		}
	}

	public class LobbyBossBar extends GameBossBar {
		public LobbyBossBar(Game game){
			super(game,GameBossBarType.LOBBY);
			this.setColor(BarColor.BLUE);
			this.setStyle(BarStyle.SOLID);
			this.update();
		}

		public void update(){
			if(this.getGame().getState() == GameState.LOBBY) this.setTitle("�fCekani na ostatni hrace");
			else if(this.getGame().getState() == GameState.STARTING) this.setTitle("�fZaciname za �a"+this.getGame().getLobbyTime()+"s");
			this.setProgress(((float)lobbyTime)/lobbyTimeDefault);
		}

		public void updateForPlayer(GamePlayer gPlayer){
			if(this.getGame().getState().isLobby()) this.addPlayer(gPlayer);
			else this.removePlayer(gPlayer);
		}
	}

	public void sendMessage(String message){
		Bukkit.broadcastMessage(prefix+message);
	}

	public void sendMessage(Player player,String message){
		player.sendMessage(prefix+message);
	}

	public void sendMessage(GamePlayer gPlayer,String message){
		this.sendMessage(gPlayer.getPlayer(),message);
	}

	public void sendGameStartingReminder(){
		if (lobbyTimeDefault <= 10) {
			return;
		}

		Bukkit.getServer().getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				if(Game.this.getState() == GameState.STARTING){
					if(lastReminder+(5*60*1000) < System.currentTimeMillis()){
						lastReminder = System.currentTimeMillis();
						GamesReminder.sendGameStartingMessage(type.getName(),"",0,players.size());
					}
				}
			}
		},(lobbyTimeDefault/3)*20);
	}

	public long getLastPlayerPremiumOffer(GamePlayer gPlayer){
		if(!playersPremiumOffers.containsKey(gPlayer.getPlayer().getName())) playersPremiumOffers.put(gPlayer.getPlayer().getName(),0L);
		return playersPremiumOffers.get(gPlayer.getPlayer().getName());
	}

	public void sendPremiumOffer(GamePlayer gPlayer){
		if (true) {
			return;
		}

		Player player = gPlayer.getPlayer();
		if(Users.getUser(player).getRank().isMaximum(UserRank.HRAC) && this.getLastPlayerPremiumOffer(gPlayer)+(PREMIUM_OFFER_TIMEOUT*1000) < System.currentTimeMillis()){
			Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					playersPremiumOffers.put(gPlayer.getPlayer().getName(),System.currentTimeMillis());
					player.sendMessage(ChatColor.LIGHT_PURPLE+""+ChatColor.STRIKETHROUGH+" ".repeat(60));
					player.sendMessage("");
					player.sendMessage("      "+ChatColor.BOLD+gPlayer.getPlayer().getName()+", stale nemas VIP ucet?");
					player.sendMessage("    "+ChatColor.GRAY+"Ziskej zdarma "+ChatColor.LIGHT_PURPLE+"doplnky"+ChatColor.GRAY+" a vyuzivej "+ChatColor.YELLOW+"vyhody,");
					player.sendMessage("  "+ChatColor.GRAY+"o kterych se ostatnim hracum muze jen zdat!");
					player.sendMessage("");
					player.sendMessage("          Podpor nas a kup si "+ChatColor.AQUA+"VIP ucet");
					TextComponent message = new TextComponent("            ");
					TextComponent website = new TextComponent(ChatColor.GREEN+""+ChatColor.BOLD+">> www.realcraft.cz <<");
					website.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,"https://www.realcraft.cz/shop/vip"));
					website.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("�7Klikni pro otevreni").create()));
					message.addExtra(website);
					player.spigot().sendMessage(message);
					player.sendMessage(ChatColor.LIGHT_PURPLE+""+ChatColor.STRIKETHROUGH+" ".repeat(60));
				}
			},4*20);
		}
	}
}