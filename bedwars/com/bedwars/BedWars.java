package com.bedwars;

import com.bedwars.BedWarsTeam.BedWarsTeamType;
import com.games.game.*;
import com.games.game.GamePodium.GamePodiumType;
import com.games.game.GameSpectator.SpectatorMenuItem;
import com.games.game.GameSpectator.SpectatorMenuItemLocation;
import com.games.game.GameSpectator.SpectatorMenuItemPlayer;
import com.games.game.GameStats.GameStatsScore;
import com.games.game.GameStats.GameStatsType;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.FormatUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import realcraft.bukkit.database.DB;
import realcraft.bukkit.utils.MaterialUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class BedWars extends Game {

	private BedWarsScoreboard scoreboard;
	private BedWarsTeams teams;
	private BedWarsShop shop;

	public BedWars(){
		super(GameType.BEDWARS);
		if(this.isMaintenance()) return;
		GameFlag.PICKUP = true;
		GameFlag.DROP = true;
		GameFlag.BUILD = true;
		GameFlag.DESTROY = true;
		GameFlag.SWAPHAND = true;
		GameFlag.USE_CONTAINER = true;
		GameFlag.USE_DOOR = true;
		GameFlag.USE_FIRE = true;
		new BedWarsListeners(this);
		this.scoreboard = new BedWarsScoreboard(this);
		this.teams = new BedWarsTeams(this);
		this.shop = new BedWarsShop(this);
		new BedWarsPodium(this,GamePodiumType.LEFT);
		new BedWarsPodium(this,GamePodiumType.RIGHT);
		this.loadArenas();
	}

	public void loadArenas(){
		ResultSet rs = DB.query("SELECT * FROM "+MAPS+" WHERE map_type = '"+this.getType().getId()+"' AND map_state = '1'");
		try {
			while(rs.next()){
				int id = rs.getInt("map_id");
				this.addArena(new BedWarsArena(this,id));
			}
			rs.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}

	public BedWarsArena getArena(){
		return (BedWarsArena) super.getArena();
	}

	public BedWarsScoreboard getScoreboard(){
		return scoreboard;
	}

	public BedWarsTeams getTeams(){
		return teams;
	}

	public BedWarsShop getShop(){
		return shop;
	}

	public void loadLobbyInventories(){
		for(GamePlayer gPlayer : this.getPlayers()){
			this.loadLobbyInventory(gPlayer);
		}
	}

	public void loadLobbyInventory(GamePlayer gPlayer){
		BedWarsTeam pTeam = this.getTeams().getPlayerTeam(gPlayer);
		ItemStack itemStack;
		ItemMeta meta;
		int amount;
		int index = 5;
		for(BedWarsTeam team : this.getTeams().getTeams()){
			amount = team.getPlayers().size();
			if(amount == 0) amount = 1;
			itemStack = new ItemStack(MaterialUtil.getWool(team.getType().getDyeColor()));
			meta = itemStack.getItemMeta();
			meta.setDisplayName(team.getType().getChatColor()+"§l"+team.getType().toName());
			if(pTeam != null && pTeam.getType() == team.getType()) meta.addEnchant(Enchantment.LURE,1,true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			itemStack.setItemMeta(meta);
			gPlayer.getPlayer().getInventory().setItem(index++,itemStack);
		}
		if(pTeam != null) pTeam.setPlayerInventory(gPlayer);
	}

	public HashMap<Integer,SpectatorMenuItem> getSpectatorMenuItems(){
		HashMap<Integer,SpectatorMenuItem> items = new HashMap<Integer,SpectatorMenuItem>();
		int row = 0;
		int index = 0;
		for(BedWarsTeam team : this.getTeams().getTeams()){
			int column = 0;
			index = (row*9)+(column++);
			ItemStack item = new ItemStack(MaterialUtil.getWool(team.getType().getDyeColor()));
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(team.getType().getChatColor()+"§l"+team.getType().toName());
			item.setItemMeta(meta);
			items.put(index,new SpectatorMenuItemLocation(index,item,team.getSpawnLocation()));
			for(GamePlayer gPlayer : team.getPlayers()){
				if(column < 8){
					index = (row*9)+(column++);
					items.put(index,new SpectatorMenuItemPlayer(index,team.getType().getChatColor()+gPlayer.getPlayer().getName(),gPlayer));
				}
			}
			row ++;
		}
		return items;
	}

	public class BedWarsScoreboard extends GameScoreboard {
		private HashMap<BedWarsTeamType,Team> teams = null;

		public BedWarsScoreboard(Game game){
			super(game,GameScoreboardType.GAME);
			this.setTitle("");
			this.setLine(0,"");
			this.setLine(1,"");
			this.setLine(2,"");
			this.setLine(3,"");
			this.setLine(4,"");
			this.setLine(5,"");
			this.setLine(6,"§ewww.realcraft.cz");
			if(teams == null){
				teams = new HashMap<BedWarsTeamType,Team>();
				teams.put(BedWarsTeamType.RED,this.getScoreboard().registerNewTeam("0"+BedWarsTeamType.RED.toName()));
				teams.put(BedWarsTeamType.BLUE,this.getScoreboard().registerNewTeam("1"+BedWarsTeamType.BLUE.toName()));
				teams.put(BedWarsTeamType.GREEN,this.getScoreboard().registerNewTeam("2"+BedWarsTeamType.GREEN.toName()));
				teams.put(BedWarsTeamType.YELLOW,this.getScoreboard().registerNewTeam("3"+BedWarsTeamType.YELLOW.toName()));
			}
			for(Entry<BedWarsTeamType,Team> entry : teams.entrySet()){
				entry.getValue().setAllowFriendlyFire(false);
				entry.getValue().setColor(entry.getKey().getChatColor());
				entry.getValue().setPrefix(entry.getKey().getChatColor().toString());
				entry.getValue().setOption(Option.COLLISION_RULE,OptionStatus.NEVER);
			}
		}

		public BedWars getGame(){
			return (BedWars) super.getGame();
		}

		public void update(){
			this.setTitle("§l"+this.getGame().getArena().getName()+"§r - "+FormatUtil.timeFormat(this.getGame().getGameTime()));
			ArrayList<BedWarsTeam> teams = this.getGame().getTeams().getTeams();
			Collections.sort(teams,new Comparator<BedWarsTeam>(){
				public int compare(BedWarsTeam team1, BedWarsTeam team2){
					return Integer.compare(team2.getPlayers().size(),team1.getPlayers().size());
				}
			});
			for(int i=0;i<teams.size();i++){
				BedWarsTeam team = teams.get(i);
				String state = "§a\u2714";
				String name = team.getType().getChatColor()+team.getType().toName()+"§r ["+team.getPlayers().size()+"]";
				if(team.getPlayers().size() == 0){
					state = "§7\u303C";
					name = "§7"+team.getType().toName();
				}
				else if(!team.hasBed()) state = "§c\u2715";
				this.setLine(i+1,state+" "+name);
			}
			super.update();
		}

		public void updateForPlayer(GamePlayer gPlayer){
			if(this.getGame().getState().isGame()){
				this.addPlayer(gPlayer);
				if(this.getGame().getTeams().getPlayerTeam(gPlayer) != null){
					for(Entry<BedWarsTeamType,Team> entry : teams.entrySet()){
						if(entry.getKey() == this.getGame().getTeams().getPlayerTeam(gPlayer).getType()) entry.getValue().addEntry(gPlayer.getPlayer().getName());
					}
				}
				else if(gPlayer.getState() == GamePlayerState.SPECTATOR){
					this.addSpectator(gPlayer);
				}
			} else {
				this.removeSpectator(gPlayer);
				this.removePlayer(gPlayer);
				for(Team team : teams.values()){
					team.removeEntry(gPlayer.getPlayer().getName());
				}
			}
		}
	}

	public class BedWarsPodium extends GamePodium {
		public BedWarsPodium(Game game,GamePodiumType type){
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