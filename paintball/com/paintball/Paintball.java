package com.paintball;

import java.io.File;
import java.util.HashMap;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.games.Games;
import com.games.game.Game;
import com.games.game.GameScoreboard;
import com.games.game.GameType;
import com.games.player.GamePlayer;
import com.games.utils.FormatUtil;
import com.games.utils.Glow;
import com.realcraft.utils.StringUtil;

public class Paintball extends Game {

	private PaintballScoreboard scoreboard;
	private PaintballTeams teams;
	private HashMap<GamePlayer,PaintballPlayer> players = new HashMap<GamePlayer,PaintballPlayer>();

	public Paintball(){
		super(GameType.PAINTBALL);
		new PaintballListeners(this);
		this.scoreboard = new PaintballScoreboard(this);
		this.teams = new PaintballTeams(this);
		this.loadArenas();
	}

	public void loadArenas(){
		File [] arenasFiles = new File(Games.getInstance().getDataFolder()+"/"+this.getType().getName()).listFiles();
		if(arenasFiles != null){
			for(File file : arenasFiles){
				if(file.isDirectory()){
					File config = new File(file.getPath()+"/config.yml");
					if(config.exists()){
						new PaintballArena(this,file.getName());
					}
				}
			}
		}
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

	public PaintballPlayer getPlayer(GamePlayer gPlayer){
		if(!players.containsKey(gPlayer)) players.put(gPlayer,new PaintballPlayer());
		return players.get(gPlayer);
	}

	@SuppressWarnings("deprecation")
	public void loadLobbyInventory(GamePlayer gPlayer){
		PaintballTeam team = this.getTeams().getPlayerTeam(gPlayer);
		ItemStack itemStack;
		ItemMeta meta;

		itemStack = new ItemStack(Material.WOOL,1,DyeColor.RED.getWoolData());
		meta = itemStack.getItemMeta();
		meta.setDisplayName(PaintballTeamType.RED.getChatColor()+"§l"+PaintballTeamType.RED.toName());
		if(team != null && team.getType() == PaintballTeamType.RED) meta.addEnchant(new Glow(255),10,true);
		itemStack.setItemMeta(meta);
		gPlayer.getPlayer().getInventory().setItem(3,itemStack);

		itemStack = new ItemStack(Material.WOOL,1,DyeColor.BLUE.getWoolData());
		meta = itemStack.getItemMeta();
		meta.setDisplayName(PaintballTeamType.BLUE.getChatColor()+"§l"+PaintballTeamType.BLUE.toName());
		if(team != null && team.getType() == PaintballTeamType.BLUE) meta.addEnchant(new Glow(255),10,true);
		itemStack.setItemMeta(meta);
		gPlayer.getPlayer().getInventory().setItem(5,itemStack);

		if(team != null){
			itemStack = new ItemStack(Material.LEATHER_CHESTPLATE,1);
			LeatherArmorMeta meta2 = (LeatherArmorMeta) itemStack.getItemMeta();
			meta2.setColor(team.getType().getColor());
	        itemStack.setItemMeta(meta2);
	        gPlayer.getPlayer().getInventory().setItem(4,itemStack);
		}
	}

	public void setPlayerWeapons(GamePlayer gPlayer){
		this.setPlayerWeapons(gPlayer,false);
	}

	public void setPlayerWeapons(GamePlayer gPlayer,boolean respawn){
		this.setPlayerWeapons(gPlayer,respawn,false);
	}

	public void setPlayerWeapons(GamePlayer gPlayer,boolean respawn,boolean fire){
		if(respawn) gPlayer.getPlayer().getInventory().setHeldItemSlot(0);
		PaintballPlayer user = this.getPlayer(gPlayer);
		gPlayer.getPlayer().getInventory().setItem(0,new ItemStack(Material.SNOW_BALL,user.getPistols()));
	}

	public class PaintballScoreboard extends GameScoreboard {
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
			if(this.getGame().getState().isGame()) this.addPlayer(gPlayer);
			else this.removePlayer(gPlayer);
		}
	}
}