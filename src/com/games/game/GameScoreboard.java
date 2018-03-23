package com.games.game;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.games.player.GamePlayer;

public abstract class GameScoreboard {

	private Game game;
	private GameScoreboardType type;

	private Scoreboard scoreboard;
	private Objective objective;
	private Team spectatorTeam;

	private String title;
	private HashMap<Integer,String> lines = new HashMap<Integer,String>();

	public GameScoreboard(Game game,GameScoreboardType type){
		this.game = game;
		this.type = type;
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
		spectatorTeam = scoreboard.registerNewTeam("xSpectator");
		spectatorTeam.setAllowFriendlyFire(false);
		spectatorTeam.setColor(ChatColor.GRAY);
		spectatorTeam.setCanSeeFriendlyInvisibles(true);
		spectatorTeam.setOption(Option.COLLISION_RULE,OptionStatus.NEVER);
	}

	public Game getGame(){
		return game;
	}

	public GameScoreboardType getType(){
		return type;
	}

	public Scoreboard getScoreboard(){
		return scoreboard;
	}

	public Objective getObjective(){
		return objective;
	}

	public String getTitle(){
		return title;
	}

	public void setTitle(String name){
		this.title = name;
	}

	public HashMap<Integer,String> getLines(){
		return lines;
	}

	public String getLine(int index){
		return lines.get(index);
	}

	public void setLine(int index,String name){
		lines.put(index,name);
	}

	public void clearLines(){
		lines.clear();
	}

	public void update(){
		scoreboard.clearSlot(DisplaySlot.SIDEBAR);
		objective = scoreboard.getObjective(type.toString());
		if(objective != null){
			objective.unregister();
		}
		objective = scoreboard.registerNewObjective(type.toString(),"dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(title);
		for(Entry<Integer,String> entry : lines.entrySet()){
			int index = lines.size()-entry.getKey();
			String line = entry.getValue();
			if(line.trim().equals("")){
				for(int i=0;i<index;i++){
					line = line + " ";
				}
			}
			Score score = objective.getScore(line);
			score.setScore(index);
		}
	}

	public void addPlayer(GamePlayer gPlayer){
		if(gPlayer.getPlayer().getScoreboard() != scoreboard) gPlayer.getPlayer().setScoreboard(scoreboard);
	}

	public void removePlayer(GamePlayer gPlayer){
		if(gPlayer.getPlayer().getScoreboard() == scoreboard) gPlayer.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}

	public void addSpectator(GamePlayer gPlayer){
		spectatorTeam.addEntry(gPlayer.getPlayer().getName());
	}

	public void removeSpectator(GamePlayer gPlayer){
		spectatorTeam.removeEntry(gPlayer.getPlayer().getName());
	}

	public enum GameScoreboardType {
		LOBBY, GAME;

		public static GameScoreboardType getByName(String name){
			return GameScoreboardType.valueOf(name.toUpperCase());
		}

		public String toString(){
			return this.name().toLowerCase();
		}
	}
}