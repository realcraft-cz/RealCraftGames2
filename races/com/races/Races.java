package com.races;

import com.games.game.Game;
import com.games.game.GamePodium;
import com.games.game.GamePodium.GamePodiumType;
import com.games.game.GameScoreboard;
import com.games.game.GameSpectator.SpectatorMenuItem;
import com.games.game.GameSpectator.SpectatorMenuItemPlayer;
import com.games.game.GameStats.GameStatsScore;
import com.games.game.GameStats.GameStatsType;
import com.games.game.GameType;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.FormatUtil;
import com.races.arenas.RaceArena;
import realcraft.bukkit.database.DB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Races extends Game {

	public static final int MAX_WINNERS = 3;
	public static final int COUNTDOWN = 11;
	public static final String[] NUMBERS = new String[]{"§c\u278A","§6\u278B","§e\u278C","§e\u278D","§e\u278E","§e\u278F","§e\u2790","§e\u2791","§e\u2792","§e\u2793"};
	public static final String[] NUMBERS2 = new String[]{"\u278A","\u278B","\u278C","\u278D","\u278E","\u278F","\u2790","\u2791","\u2792","\u2793"};

	private RacesScoreboard scoreboard;
	private HashMap<GamePlayer,RaceUser> users = new HashMap<GamePlayer,RaceUser>();
	private int countdown = 0;
	private HashMap<GamePlayer,GameWinner> winners = new HashMap<GamePlayer,GameWinner>();

	public Races(){
		super(GameType.RACES);
		if(this.isMaintenance()) return;
		new RaceListeners(this);
		this.scoreboard = new RacesScoreboard(this);
		new RacesPodium(this,GamePodiumType.LEFT);
		new RacesPodium(this,GamePodiumType.RIGHT);
		this.loadArenas();
	}

	public void loadArenas(){
		ResultSet rs = DB.query("SELECT * FROM "+MAPS+" WHERE map_type = '"+this.getType().getId()+"' AND map_state = '1'");
		try {
			while(rs.next()){
				int id = rs.getInt("map_id");
				this.addArena(new RaceArena(this,id));
			}
			rs.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}

	public RaceArena getArena(){
		return (RaceArena) super.getArena();
	}

	public RacesScoreboard getScoreboard(){
		return scoreboard;
	}

	public RaceUser getUser(GamePlayer gPlayer){
		if(!users.containsKey(gPlayer)) users.put(gPlayer,new RaceUser(this,gPlayer));
		return users.get(gPlayer);
	}

	public int getCountdown(){
		return countdown;
	}

	public void setCountdown(int countdown){
		this.countdown = countdown;
	}

	public GameWinner getWinner(GamePlayer gPlayer){
		return winners.get(gPlayer);
	}

	public void addWinner(GamePlayer gPlayer){
		winners.put(gPlayer,new GameWinner(winners.size()+1,gPlayer));
	}

	public void clearWinners(){
		winners.clear();
	}

	@Override
	public HashMap<Integer,SpectatorMenuItem> getSpectatorMenuItems(){
		HashMap<Integer,SpectatorMenuItem> items = new HashMap<Integer,SpectatorMenuItem>();
		int row = 0;
		int column = 0;
		for(GamePlayer gPlayer : this.getGamePlayers()){
			int index = (row*9)+(column++);
			items.put(index,new SpectatorMenuItemPlayer(index,gPlayer.getPlayer().getName(),gPlayer));
			if(column == 8){
				column = 0;
				row ++;
			}
		}
		return items;
	}

	public class RacesScoreboard extends GameScoreboard {
		private static final int MAX_PLAYERS = 10;

		public RacesScoreboard(Game game){
			super(game,GameScoreboardType.GAME);
			this.setTitle("");
			this.setLine(0,"");
		}

		public Races getGame(){
			return (Races) super.getGame();
		}

		public void update(){
			this.setTitle(this.getGame().getType().getColor()+"§l"+this.getGame().getArena().getName()+"§r - "+FormatUtil.timeFormat(this.getGame().getGameTime()));
			if(this.getGame().getCountdown() > 0){
				this.clearLines();
				this.setLine(0,"");
				this.setLine(1,"§fZavod zacina");
				this.setLine(2,"§fza §a"+this.getGame().getCountdown()+" §fsekund");
				this.setLine(3,"");
				this.setLine(4,"§ewww.realcraft.cz");
			}
			else if(this.getGame().getState().isGame()){
				ArrayList<GamePlayer> players = new ArrayList<GamePlayer>(this.getGame().getPlayers());
				for(GamePlayer player : this.getGame().getPlayers()) if(player.getState() == GamePlayerState.SPECTATOR && this.getGame().getWinner(player) == null) players.remove(player);
				Collections.sort(players,new Comparator<GamePlayer>(){
					public int compare(GamePlayer player1,GamePlayer player2){
						return Double.compare(RacesScoreboard.this.getGame().getUser(player2).getPosition(),RacesScoreboard.this.getGame().getUser(player1).getPosition());
					}
				});
				int posOffset = 0;
				for(GameWinner winner : winners.values()){
					if(!this.getGame().getPlayers().contains(winner.getGamePlayer())) posOffset ++;
				}
				int index = 1;
				for(GamePlayer gPlayer : players){
					if(index-1 < MAX_PLAYERS){
						char color = '7';
						if(index == 1) color = 'c';
						else if(index == 2) color = '6';
						else if(index == 3) color = 'e';
						this.setLine(index,"§"+color+Races.NUMBERS2[index-1+posOffset]+" §f"+(this.getGame().getWinner(gPlayer) != null ? "§n" : "")+gPlayer.getPlayer().getName());
						index ++;
					}
				}
				this.setLine(index++,"");
				this.setLine(index++,"§ewww.realcraft.cz");
			}
			super.update();
		}

		public void updateForPlayer(GamePlayer gPlayer){
			if(this.getGame().getState().isGame()){
				this.addPlayer(gPlayer);
				if(gPlayer.getState() == GamePlayerState.SPECTATOR){
					this.addSpectator(gPlayer);
				}
			} else {
				this.removeSpectator(gPlayer);
				this.removePlayer(gPlayer);
			}
		}
	}

	public class RacesPodium extends GamePodium {
		public RacesPodium(Game game,GamePodiumType type){
			super(game,type);
		}

		@Override
		public void update(){
			GameStatsType type = GameStatsType.WINS;
			ArrayList<GameStatsScore> scores = this.getGame().getStats().getScores(type);
			int index = 0;
			for(GamePodiumStand stand : this.getStands()){
				if(scores.size() <= index) continue;
				stand.setData(scores.get(index).getName(),scores.get(index).getValue()+" "+type.getName());
				index ++;
			}
		}
	}

	public class GameWinner {

		private int position;
		private GamePlayer gPlayer;

		public GameWinner(int position,GamePlayer gPlayer){
			this.position = position;
			this.gPlayer = gPlayer;
		}

		public int getPosition(){
			return position;
		}

		public GamePlayer getGamePlayer(){
			return gPlayer;
		}
	}
}