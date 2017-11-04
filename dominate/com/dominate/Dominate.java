package com.dominate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Team;

import com.dominate.DominateTeam.DominateTeamType;
import com.games.Games;
import com.games.game.Game;
import com.games.game.GameFlag;
import com.games.game.GamePodium;
import com.games.game.GamePodium.GamePodiumType;
import com.games.game.GameScoreboard;
import com.games.game.GameState;
import com.games.game.GameStats.GameStatsScore;
import com.games.game.GameType;
import com.games.player.GamePlayer;
import com.games.utils.Glow;

public class Dominate extends Game {

	private DominateScoreboard scoreboard;
	private DominateTeams teams;
	private HashMap<GamePlayer,DominateUser> users = new HashMap<GamePlayer,DominateUser>();
	private int cycleTime;

	public Dominate(){
		super(GameType.DOMINATE);
		if(this.isMaintenance()) return;
		GameFlag.PICKUP = true;
		new DominateListeners(this);
		this.scoreboard = new DominateScoreboard(this);
		this.teams = new DominateTeams(this);
		new DominatePodium(this,GamePodiumType.LEFT);
		new DominatePodium(this,GamePodiumType.RIGHT);
		this.loadArenas();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				if(Dominate.this.getState().isGame()){
					cycleTime ++;
					for(GamePlayer gPlayer : Dominate.this.getPlayers()){
						Dominate.this.getUser(gPlayer).updateBossBar();
					}
					if(cycleTime == 10 || cycleTime == 20){
						if(Dominate.this.getState() == GameState.INGAME){
							for(DominatePoint point : Dominate.this.getArena().getPoints()){
								point.run();
							}
							for(GamePlayer gPlayer : Dominate.this.getGamePlayers()){
								Dominate.this.getUser(gPlayer).runSpawn();
							}
						}
						Dominate.this.getScoreboard().update();
					}
					if(cycleTime == 20) cycleTime = 0;
				}
			}
		},1,1);
	}

	public void loadArenas(){
		File [] arenasFiles = new File(Games.getInstance().getDataFolder()+"/"+this.getType().getName()).listFiles();
		if(arenasFiles != null){
			for(File file : arenasFiles){
				if(file.isDirectory()){
					File config = new File(file.getPath()+"/config.yml");
					if(config.exists()){
						new DominateArena(this,file.getName());
					}
				}
			}
		}
	}

	public DominateArena getArena(){
		return (DominateArena) super.getArena();
	}

	public DominateScoreboard getScoreboard(){
		return scoreboard;
	}

	public DominateTeams getTeams(){
		return teams;
	}

	public DominateUser getUser(GamePlayer gPlayer){
		if(!users.containsKey(gPlayer)) users.put(gPlayer,new DominateUser(this,gPlayer));
		return users.get(gPlayer);
	}

	public void loadLobbyInventories(){
		for(GamePlayer gPlayer : this.getPlayers()){
			this.loadLobbyInventory(gPlayer);
		}
	}

	@SuppressWarnings("deprecation")
	public void loadLobbyInventory(GamePlayer gPlayer){
		DominateTeam team = this.getTeams().getPlayerTeam(gPlayer);
		ItemStack itemStack;
		ItemMeta meta;
		int amount;

		amount = this.getTeams().getTeam(DominateTeamType.RED).getPlayers().size();
		if(amount == 0) amount = 1;
		itemStack = new ItemStack(Material.WOOL,amount,DyeColor.RED.getWoolData());
		meta = itemStack.getItemMeta();
		meta.setDisplayName(DominateTeamType.RED.getChatColor()+"§l"+DominateTeamType.RED.toName());
		if(team != null && team.getType() == DominateTeamType.RED) meta.addEnchant(new Glow(255),10,true);
		itemStack.setItemMeta(meta);
		gPlayer.getPlayer().getInventory().setItem(7,itemStack);

		amount = this.getTeams().getTeam(DominateTeamType.BLUE).getPlayers().size();
		if(amount == 0) amount = 1;
		itemStack = new ItemStack(Material.WOOL,amount,DyeColor.BLUE.getWoolData());
		meta = itemStack.getItemMeta();
		meta.setDisplayName(DominateTeamType.BLUE.getChatColor()+"§l"+DominateTeamType.BLUE.toName());
		if(team != null && team.getType() == DominateTeamType.BLUE) meta.addEnchant(new Glow(255),10,true);
		itemStack.setItemMeta(meta);
		gPlayer.getPlayer().getInventory().setItem(8,itemStack);
	}

	public void reset(){
		for(DominateEmerald emerald : this.getArena().getEmeralds()){
			emerald.reset();
		}
	}

	public class DominateScoreboard extends GameScoreboard {
		private Team teamRed;
		private Team teamBlue;

		public DominateScoreboard(Game game){
			super(game,GameScoreboardType.GAME);
			this.setTitle("");
			this.setLine(0,"  §7"+DominateTeams.WIN_SCORE+" vyhrava");
			this.setLine(1,"");
			this.setLine(2,"§c§lRed");
			this.setLine(3,"§f0 ");
			this.setLine(4,"");
			this.setLine(5,"§b§lBlue");
			this.setLine(6,"§f0");
			this.setLine(7,"");
			this.setLine(8,"");
			this.setLine(9,"");
			this.setLine(10,"");
			this.setLine(11,"");
			this.setLine(12,"");
			this.setLine(13,"");
			this.setLine(14,"§ewww.realcraft.cz");
			if(teamRed == null) teamRed = this.getScoreboard().registerNewTeam("0teamRed");
			if(teamBlue == null) teamBlue = this.getScoreboard().registerNewTeam("1teamBlue");
			teamRed.setAllowFriendlyFire(false);
			teamBlue.setAllowFriendlyFire(false);
			teamRed.setColor(DominateTeamType.RED.getChatColor());
			teamBlue.setColor(DominateTeamType.BLUE.getChatColor());
			teamRed.setPrefix(DominateTeamType.RED.getChatColor().toString());
			teamBlue.setPrefix(DominateTeamType.BLUE.getChatColor().toString());
		}

		public Dominate getGame(){
			return (Dominate) super.getGame();
		}

		public void update(){
			this.setTitle(this.getGame().getType().getColor()+"§l"+this.getGame().getArena().getName());
			this.setLine(3,"§f"+this.getGame().getTeams().getTeam(DominateTeamType.RED).getPoints()+" ");
			this.setLine(6,"§f"+this.getGame().getTeams().getTeam(DominateTeamType.BLUE).getPoints());
			int index = 8;
			for(DominatePoint point : this.getGame().getArena().getPoints()){
				this.setLine(index++,point.getNameColor()+point.getName());
			}
			super.update();
		}

		public void updateForPlayer(GamePlayer gPlayer){
			if(this.getGame().getState().isGame()){
				this.addPlayer(gPlayer);
				if(this.getGame().getTeams().getPlayerTeam(gPlayer) != null){
					if(this.getGame().getTeams().getPlayerTeam(gPlayer).getType() == DominateTeamType.RED) teamRed.addEntry(gPlayer.getPlayer().getName());
					else if(this.getGame().getTeams().getPlayerTeam(gPlayer).getType() == DominateTeamType.BLUE) teamBlue.addEntry(gPlayer.getPlayer().getName());
				}
			} else {
				this.removePlayer(gPlayer);
				teamRed.removeEntry(gPlayer.getPlayer().getName());
				teamBlue.removeEntry(gPlayer.getPlayer().getName());
			}
		}
	}

	public class DominatePodium extends GamePodium {
		public DominatePodium(Game game,GamePodiumType type){
			super(game,type);
		}

		@Override
		public void update(){
			ArrayList<GameStatsScore> scores = this.getGame().getStats().getScores(this.getType().getId());
			int index = 0;
			for(GamePodiumStand stand : this.getStands()){
				if(scores.size() <= index) continue;
				if(this.getType() == GamePodiumType.LEFT) stand.setData(scores.get(index).getName(),scores.get(index).getValue()+" vyher");
				else if(this.getType() == GamePodiumType.RIGHT) stand.setData(scores.get(index).getName(),scores.get(index).getValue()+" zabiti");
				index ++;
			}
		}
	}
}