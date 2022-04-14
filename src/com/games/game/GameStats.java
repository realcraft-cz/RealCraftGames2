package com.games.game;

import com.games.Games;
import com.games.player.GamePlayer;
import org.bukkit.Bukkit;
import realcraft.bukkit.RealCraft;
import realcraft.bukkit.database.DB;
import realcraft.bukkit.users.Users;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class GameStats {

	private Game game;
	private static final String GAMES = "minigames_games";
	private static final String SCORES = "minigames_scores";

	public GameStats(Game game){
		this.game = game;
	}

	public Game getGame(){
		return game;
	}

	public ArrayList<GameStatsScore> getScores(GameStatsType type){
		return this.getScores(type,3);
	}

	public ArrayList<GameStatsScore> getScores(GameStatsType type,int limit){
		ArrayList<GameStatsScore> scores = new ArrayList<GameStatsScore>();
		ResultSet rs = DB.query("SELECT t2.user_name,SUM(score_value) AS score FROM "+SCORES+" t1 INNER JOIN authme t2 USING(user_id) WHERE game_id = '"+game.getType().getId()+"' AND score_type = '"+type.getId()+"' AND score_created >= '"+((System.currentTimeMillis()/1000)-30*86400)+"' GROUP BY t1.user_id ORDER BY score DESC LIMIT "+limit);
		try {
			while(rs.next()){
				scores.add(new GameStatsScore(rs.getString("user_name"),rs.getInt("score")));
			}
			rs.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
		return scores;
	}

	public void addScore(GamePlayer gPlayer,GameStatsType type,int value){
		this.addScore(Users.getUser(gPlayer.getPlayer()).getId(),type,value);
	}

	public void addScore(int userid,GameStatsType type,int value){
		if(!RealCraft.isTestServer()){
			Bukkit.getScheduler().runTaskAsynchronously(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					DB.update("INSERT INTO "+SCORES+" (game_id,user_id,score_value,score_type,score_created) VALUES('"+game.getType().getId()+"','"+userid+"','"+value+"','"+type.getId()+"','"+(System.currentTimeMillis()/1000)+"')");
				}
			});
		}
	}

	public void addGame(int players){
		if(!RealCraft.isTestServer()){
			Bukkit.getScheduler().runTaskAsynchronously(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					DB.update("INSERT INTO "+GAMES+" (game_id,game_players,game_created) VALUES('"+game.getType().getId()+"','"+players+"','"+(System.currentTimeMillis()/1000)+"')");
				}
			});
		}
	}

	public class GameStatsScore {

		private String name;
		private int value;

		public GameStatsScore(String name,int value){
			this.name = name;
			this.value = value;
		}

		public String getName(){
			return name;
		}

		public int getValue(){
			return value;
		}
	}

	public enum GameStatsType {
		GAMES, WINS, KILLS, DEATHS;

		public int getId(){
			switch(this){
				case GAMES: return 0;
				case WINS: return 1;
				case KILLS: return 2;
				case DEATHS: return 3;
			}
			return 0;
		}

		public String getName(){
			switch(this){
				case GAMES: return "her";
				case WINS: return "vyher";
				case KILLS: return "zabiti";
				case DEATHS: return "smrti";
			}
			return null;
		}
	}
}