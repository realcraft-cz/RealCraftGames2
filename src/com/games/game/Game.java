package com.games.game;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.games.Games;
import com.games.arena.GameArena;
import com.games.events.GameCycleEvent;
import com.games.events.GameEndEvent;
import com.games.events.GamePlayerJoinEvent;
import com.games.events.GameStartEvent;
import com.games.events.GameStateChangeEvent;
import com.games.events.GameTimeoutEvent;
import com.games.exceptions.GameMaxPlayersException;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.LocationUtil;
import com.realcraft.RealCraft;
import com.realcraft.ServerType;
import com.realcraft.lobby.LobbyAutoParkour;
import com.realcraft.lobby.LobbyMenu;
import com.realcraft.utils.StringUtil;
import com.realcraft.utils.Title;

public abstract class Game implements Runnable {

	private GameType type;
	private GameState state = GameState.LOBBY;
	private GameArena arena;
	private FileConfiguration config;

	private HashMap<String,GamePlayer> players = new HashMap<String,GamePlayer>();
	private HashMap<String,GameArena> arenas = new HashMap<String,GameArena>();
	private Location lobbyLocation;
	private LobbyScoreboard lobbyScoreboard;
	private LobbyBossBar lobbyBossBar;
	private GameVoting gameVoting;

	private int lobbyTimeDefault;
	private int lobbyTime;
	private int gameTimeDefault;
	private int gameTime;
	private int endTimeDefault;
	private int endTime;
	private int minPlayers;
	private int maxPlayers;
	private String prefix;

	public Game(GameType type){
		this.type = type;
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
		this.gameVoting = new GameVoting(this);
		new GameListeners(this);
		for(World world : Bukkit.getWorlds()){
			world.setGameRuleValue("doDaylightCycle","false");
			world.setGameRuleValue("doWeatherCycle","false");
			world.setFullTime(6000);
		}
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Games.getInstance(),this,20,20);
		Bukkit.getScheduler().runTask(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				gameVoting.resetVoting();
			}
		});
		new LobbyMenu(RealCraft.getInstance());
		new LobbyAutoParkour(RealCraft.getInstance());
	}

	@Override
	public void run(){
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
						gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_HAT,1,1);
						if(lobbyTime <= 5) Title.showTitle(gPlayer.getPlayer(),"�3"+lobbyTime,0.2,0.6,0.2);
					}
				}
			} else {
				this.setArena(gameVoting.getWinningArena());
				this.setState(GameState.INGAME);
				for(GamePlayer gPlayer : this.getPlayers()){
					lobbyScoreboard.updateForPlayer(gPlayer);
					lobbyBossBar.updateForPlayer(gPlayer);
				}
				Bukkit.getServer().getPluginManager().callEvent(new GameStartEvent(this));
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

	public int getPlayersCount(){
		int count = 0;
		for(GamePlayer gPlayer : this.getPlayers()) if(gPlayer.getState() != GamePlayerState.SPECTATOR) count ++;
		return count;
	}

	public GamePlayer getGamePlayer(Player player){
		if(!players.containsKey(player.getName())) players.put(player.getName(),new GamePlayer(player,this));
		return players.get(player.getName());
	}

	public ArrayList<GameArena> getArenas(){
		return new ArrayList<GameArena>(arenas.values());
	}

	public void addArena(GameArena arena){
		arenas.put(arena.getName(),arena);
	}

	public Location getLobbyLocation(){
		if(lobbyLocation == null) lobbyLocation = LocationUtil.getConfigLocation(Games.getInstance().getConfig(),"lobby");
		return lobbyLocation;
	}

	public LobbyScoreboard getLobbyScoreboard(){
		return lobbyScoreboard;
	}

	public LobbyBossBar getLobbyBossBar(){
		return lobbyBossBar;
	}

	public GameVoting getVoting(){
		return gameVoting;
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

	public void tryToConnect(GamePlayer gPlayer) throws GameMaxPlayersException {
		if(this.getPlayers().size() >= this.getMaxPlayers()) throw new GameMaxPlayersException();
	}

	public void joinPlayer(GamePlayer gPlayer) throws GameMaxPlayersException {
		this.tryToConnect(gPlayer);
		gPlayer.resetPlayer();
		gPlayer.getPlayer().teleport(this.getLobbyLocation());
		gPlayer.getPlayer().getInventory().setItem(0,LobbyMenu.getItem());

		lobbyScoreboard.updateForPlayer(gPlayer);
		lobbyBossBar.updateForPlayer(gPlayer);

		Bukkit.getServer().getPluginManager().callEvent(new GamePlayerJoinEvent(this,gPlayer));

		Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				gPlayer.getPlayer().teleport(Game.this.getLobbyLocation());
			}
		},5);
	}

	public void leavePlayer(GamePlayer gPlayer){
		gPlayer.getPlayer().sendMessage("Teleporting to lobby");
		gPlayer.connectToServer(ServerType.LOBBY);
	}

	public void removePlayer(GamePlayer gPlayer){
		players.remove(gPlayer.getPlayer().getName());
		gameVoting.removePlayer(gPlayer);
		if(this.getState() == GameState.STARTING && this.getPlayers().size() < this.getMinPlayers()){
			this.setState(GameState.LOBBY);
		}
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
}