package com.dominate;

import com.dominate.DominateTeam.DominateTeamType;
import com.games.Games;
import com.games.game.*;
import com.games.game.GamePodium.GamePodiumType;
import com.games.game.GameSpectator.SpectatorMenuItem;
import com.games.game.GameSpectator.SpectatorMenuItemLocation;
import com.games.game.GameSpectator.SpectatorMenuItemPlayer;
import com.games.game.GameStats.GameStatsScore;
import com.games.game.GameStats.GameStatsType;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.Glow;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Team;
import realcraft.bukkit.utils.MaterialUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

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

	public void loadLobbyInventory(GamePlayer gPlayer){
		DominateTeam team = this.getTeams().getPlayerTeam(gPlayer);
		ItemStack itemStack;
		ItemMeta meta;
		int amount;

		amount = this.getTeams().getTeam(DominateTeamType.RED).getPlayers().size();
		if(amount == 0) amount = 1;
		itemStack = new ItemStack(MaterialUtil.getWool(DyeColor.RED),amount);
		meta = itemStack.getItemMeta();
		meta.setDisplayName(DominateTeamType.RED.getChatColor()+"§l"+DominateTeamType.RED.toName());
		if(team != null && team.getType() == DominateTeamType.RED) meta.addEnchant(Glow.getGlow(),10,true);
		itemStack.setItemMeta(meta);
		gPlayer.getPlayer().getInventory().setItem(7,itemStack);

		amount = this.getTeams().getTeam(DominateTeamType.BLUE).getPlayers().size();
		if(amount == 0) amount = 1;
		itemStack = new ItemStack(MaterialUtil.getWool(DyeColor.BLUE),amount);
		meta = itemStack.getItemMeta();
		meta.setDisplayName(DominateTeamType.BLUE.getChatColor()+"§l"+DominateTeamType.BLUE.toName());
		if(team != null && team.getType() == DominateTeamType.BLUE) meta.addEnchant(Glow.getGlow(),10,true);
		itemStack.setItemMeta(meta);
		gPlayer.getPlayer().getInventory().setItem(8,itemStack);
	}

	public void reset(){
		for(DominateEmerald emerald : this.getArena().getEmeralds()){
			emerald.reset();
		}
	}

	public HashMap<Integer,SpectatorMenuItem> getSpectatorMenuItems(){
		HashMap<Integer,SpectatorMenuItem> items = new HashMap<Integer,SpectatorMenuItem>();
		int row = 0;
		int index = 0;
		for(DominateTeam team : this.getTeams().getTeams()){
			int column = 0;
			index = (row*9)+(column++);
			ItemStack item = new ItemStack(MaterialUtil.getWool(team.getType().getDyeColor()));
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(team.getType().getChatColor()+"§l"+team.getType().toName());
			item.setItemMeta(meta);
			items.put(index,new SpectatorMenuItemLocation(index,item,team.getSpawnLocation()));
			for(GamePlayer gPlayer : team.getPlayers()){
				index = (row*9)+(column++);
				items.put(index,new SpectatorMenuItemPlayer(index,team.getType().getChatColor()+gPlayer.getPlayer().getName(),gPlayer));
				if(column == 8){
					column = 0;
					row ++;
				}
			}
			row ++;
		}
		return items;
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
				else if(gPlayer.getState() == GamePlayerState.SPECTATOR){
					this.addSpectator(gPlayer);
				}
			} else {
				this.removeSpectator(gPlayer);
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
			GameStatsType type = GameStatsType.WINS;
			if(this.getType() == GamePodiumType.RIGHT) type = GameStatsType.KILLS;
			ArrayList<GameStatsScore> scores = this.getGame().getStats().getScores(type);
			int index = 0;
			for(GamePodiumStand stand : this.getStands()){
				if(scores.size() <= index) continue;
				stand.setData(scores.get(index).getName(),scores.get(index).getValue()+" "+type.getName());
				index ++;
			}
		}
	}
}