package com.paintball;

import com.games.game.Game;
import com.games.game.GamePodium;
import com.games.game.GamePodium.GamePodiumType;
import com.games.game.GameScoreboard;
import com.games.game.GameSpectator.SpectatorMenuItem;
import com.games.game.GameSpectator.SpectatorMenuItemLocation;
import com.games.game.GameSpectator.SpectatorMenuItemPlayer;
import com.games.game.GameStats.GameStatsScore;
import com.games.game.GameStats.GameStatsType;
import com.games.game.GameType;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.FormatUtil;
import com.games.utils.StringUtil;
import com.paintball.PaintballTeam.PaintballTeamType;
import org.bukkit.DyeColor;
import org.bukkit.Material;
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
import java.util.HashMap;

public class Paintball extends Game {

	private PaintballScoreboard scoreboard;
	private PaintballTeams teams;
	private PaintballDrops drops;
	private HashMap<GamePlayer,PaintballUser> users = new HashMap<GamePlayer,PaintballUser>();

	public Paintball(){
		super(GameType.PAINTBALL);
		if(this.isMaintenance()) return;
		new PaintballListeners(this);
		this.scoreboard = new PaintballScoreboard(this);
		this.teams = new PaintballTeams(this);
		this.drops = new PaintballDrops(this);
		new PaintballPodium(this,GamePodiumType.LEFT);
		new PaintballPodium(this,GamePodiumType.RIGHT);
		this.loadArenas();
	}

	public void loadArenas(){
		ResultSet rs = DB.query("SELECT * FROM "+MAPS+" WHERE map_type = '"+this.getType().getId()+"' AND map_state = '1'");
		try {
			while(rs.next()){
				int id = rs.getInt("map_id");
				this.addArena(new PaintballArena(this,id));
			}
			rs.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable(){
		super.onDisable();
		this.getDrops().clear();
	}

	public PaintballArena getArena(){
		return (PaintballArena) super.getArena();
	}

	public PaintballScoreboard getScoreboard(){
		return scoreboard;
	}

	public PaintballTeams getTeams(){
		return teams;
	}

	public PaintballDrops getDrops(){
		return drops;
	}

	public PaintballUser getUser(GamePlayer gPlayer){
		if(!users.containsKey(gPlayer)) users.put(gPlayer,new PaintballUser());
		return users.get(gPlayer);
	}

	public void loadLobbyInventories(){
		for(GamePlayer gPlayer : this.getPlayers()){
			this.loadLobbyInventory(gPlayer);
		}
	}

	public void loadLobbyInventory(GamePlayer gPlayer){
		PaintballTeam team = this.getTeams().getPlayerTeam(gPlayer);
		ItemStack itemStack;
		ItemMeta meta;
		int amount;

		amount = this.getTeams().getTeam(PaintballTeamType.RED).getPlayers().size();
		if(amount == 0) amount = 1;
		itemStack = new ItemStack(MaterialUtil.getWool(DyeColor.RED),amount);
		meta = itemStack.getItemMeta();
		meta.setDisplayName(PaintballTeamType.RED.getChatColor()+"§l"+PaintballTeamType.RED.toName());
		if(team != null && team.getType() == PaintballTeamType.RED) meta.addEnchant(Enchantment.LURE,10,true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		itemStack.setItemMeta(meta);
		gPlayer.getPlayer().getInventory().setItem(7,itemStack);

		amount = this.getTeams().getTeam(PaintballTeamType.BLUE).getPlayers().size();
		if(amount == 0) amount = 1;
		itemStack = new ItemStack(MaterialUtil.getWool(DyeColor.BLUE),amount);
		meta = itemStack.getItemMeta();
		meta.setDisplayName(PaintballTeamType.BLUE.getChatColor()+"§l"+PaintballTeamType.BLUE.toName());
		if(team != null && team.getType() == PaintballTeamType.BLUE) meta.addEnchant(Enchantment.LURE,10,true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		itemStack.setItemMeta(meta);
		gPlayer.getPlayer().getInventory().setItem(8,itemStack);
	}

	public void setPlayerWeapons(GamePlayer gPlayer){
		this.setPlayerWeapons(gPlayer,false);
	}

	public void setPlayerWeapons(GamePlayer gPlayer,boolean respawn){
		if(respawn) gPlayer.getPlayer().getInventory().setHeldItemSlot(0);
		PaintballUser user = this.getUser(gPlayer);
		gPlayer.getPlayer().getInventory().setItem(0,new ItemStack(Material.SNOWBALL,user.getPistols()));
		gPlayer.getPlayer().getInventory().setItem(1,user.getGrenades() > 0 ? new ItemStack(Material.EGG,user.getGrenades()) : null);
	}

	public HashMap<Integer,SpectatorMenuItem> getSpectatorMenuItems(){
		HashMap<Integer,SpectatorMenuItem> items = new HashMap<Integer,SpectatorMenuItem>();
		int row = 0;
		int index = 0;
		for(PaintballTeam team : this.getTeams().getActiveTeams()){
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

	public class PaintballScoreboard extends GameScoreboard {
		private Team teamRed;
		private Team teamBlue;

		public PaintballScoreboard(Game game){
			super(game,GameScoreboardType.GAME);
			this.setTitle("");
			this.setLine(0,"");
			this.setLine(1,"§c§lRed");
			this.setLine(2,"§f0 bodu ");
			this.setLine(3,"");
			this.setLine(4,"§9§lBlue");
			this.setLine(5,"§f0 bodu");
			this.setLine(6,"");
			this.setLine(7,"§ewww.realcraft.cz");
			if(teamRed == null) teamRed = this.getScoreboard().registerNewTeam("0teamRed");
			if(teamBlue == null) teamBlue = this.getScoreboard().registerNewTeam("1teamBlue");
			teamRed.setOption(Option.NAME_TAG_VISIBILITY,OptionStatus.FOR_OTHER_TEAMS);
			teamBlue.setOption(Option.NAME_TAG_VISIBILITY,OptionStatus.FOR_OTHER_TEAMS);
			teamRed.setAllowFriendlyFire(false);
			teamBlue.setAllowFriendlyFire(false);
			teamRed.setColor(PaintballTeamType.RED.getChatColor());
			teamBlue.setColor(PaintballTeamType.BLUE.getChatColor());
			teamRed.setPrefix(PaintballTeamType.RED.getChatColor().toString());
			teamBlue.setPrefix(PaintballTeamType.BLUE.getChatColor().toString());
		}

		public Paintball getGame(){
			return (Paintball) super.getGame();
		}

		public void update(){
			this.setTitle(this.getGame().getType().getColor()+"§l"+this.getGame().getArena().getName()+"§r - "+FormatUtil.timeFormat(this.getGame().getGameTime()));
			this.setLine(2,"§f"+this.getGame().getTeams().getTeam(PaintballTeamType.RED).getKills()+" "+StringUtil.inflect(this.getGame().getTeams().getTeam(PaintballTeamType.RED).getKills(),new String[]{"bod","body","bodu"})+" ");
			this.setLine(5,"§f"+this.getGame().getTeams().getTeam(PaintballTeamType.BLUE).getKills()+" "+StringUtil.inflect(this.getGame().getTeams().getTeam(PaintballTeamType.BLUE).getKills(),new String[]{"bod","body","bodu"}));
			super.update();
		}

		public void updateForPlayer(GamePlayer gPlayer){
			if(this.getGame().getState().isGame()){
				this.addPlayer(gPlayer);
				if(this.getGame().getTeams().getPlayerTeam(gPlayer) != null){
					if(this.getGame().getTeams().getPlayerTeam(gPlayer).getType() == PaintballTeamType.RED) teamRed.addEntry(gPlayer.getPlayer().getName());
					else if(this.getGame().getTeams().getPlayerTeam(gPlayer).getType() == PaintballTeamType.BLUE) teamBlue.addEntry(gPlayer.getPlayer().getName());
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

	public class PaintballPodium extends GamePodium {
		public PaintballPodium(Game game,GamePodiumType type){
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