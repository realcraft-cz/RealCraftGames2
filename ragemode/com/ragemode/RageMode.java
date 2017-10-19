package com.ragemode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.games.Games;
import com.games.game.Game;
import com.games.game.GameFlag;
import com.games.game.GameScoreboard;
import com.games.game.GameType;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.FormatUtil;

public class RageMode extends Game {

	public static final int WINSCORE = 20;
	private int startPlayers;
	private RageModeScoreboard scoreboard;

	public RageMode(){
		super(GameType.RAGEMODE);
		GameFlag.PICKUP = true;
		new RageModeListeners(this);
		this.scoreboard = new RageModeScoreboard(this);
		this.loadArenas();
	}

	public void loadArenas(){
		File [] arenasFiles = new File(Games.getInstance().getDataFolder()+"/"+this.getType().getName()).listFiles();
		if(arenasFiles != null){
			for(File file : arenasFiles){
				if(file.isDirectory()){
					File config = new File(file.getPath()+"/config.yml");
					if(config.exists()){
						new RageModeArena(this,file.getName());
					}
				}
			}
		}
	}

	public RageModeArena getArena(){
		return (RageModeArena) super.getArena();
	}

	public int getStartPlayers(){
		return startPlayers;
	}

	public void setStartPlayers(int startPlayers){
		this.startPlayers = startPlayers;
	}

	public RageModeScoreboard getScoreboard(){
		return scoreboard;
	}

	public void loadInventory(GamePlayer gPlayer){
		ItemStack item = new ItemStack(Material.BOW,1);
		item.addEnchantment(Enchantment.ARROW_INFINITE,1);
		item.addEnchantment(Enchantment.DURABILITY,3);
		gPlayer.getPlayer().getInventory().clear();
		gPlayer.getPlayer().getInventory().addItem(item);
		gPlayer.getPlayer().getInventory().setItem(8,new ItemStack(Material.ARROW,1));
		gPlayer.getPlayer().getInventory().addItem(new ItemStack(Material.IRON_AXE,1));
		gPlayer.getPlayer().getInventory().addItem(new ItemStack(Material.EGG,1));
	}

	public GamePlayer getWinner(){
		GamePlayer winner = null;
		for(GamePlayer gPlayer : this.getPlayers()){
			if(gPlayer.getState() == GamePlayerState.SPECTATOR) continue;
			if(winner == null || gPlayer.getSettings().getInt("kills") > winner.getSettings().getInt("kills") ||
					(gPlayer.getSettings().getInt("kills") == winner.getSettings().getInt("kills") && gPlayer.getSettings().getInt("deaths") >= winner.getSettings().getInt("deaths"))){
				winner = gPlayer;
			}
		}
		return winner;
	}

	public class RageModeScoreboard extends GameScoreboard {
		private static final int PLAYERS = 7;

		public RageModeScoreboard(Game game){
			super(game,GameScoreboardType.GAME);
			this.setTitle("");
			this.setLine(0,"");
			this.setLine(1,"�e�lZebricek");
			this.setLine(2,"");
			this.setLine(3,"");
			this.setLine(4,"");
			this.setLine(5,"");
			this.setLine(6,"");
			this.setLine(7,"");
			this.setLine(8,"");
			this.setLine(9,"�ewww.realcraft.cz");
			if(this.getScoreboard().getTeam("team") == null) this.getScoreboard().registerNewTeam("team");
			this.getScoreboard().getTeam("team").setOption(Option.NAME_TAG_VISIBILITY,OptionStatus.NEVER);
		}

		public void update(){
			this.setTitle(this.getGame().getType().getColor()+"�l"+this.getGame().getArena().getName()+"�r - "+FormatUtil.timeFormat(this.getGame().getGameTime()));
			ArrayList<GamePlayer> players = new ArrayList<GamePlayer>(this.getGame().getPlayers());
			for(GamePlayer player : this.getGame().getPlayers()) if(player.getState() == GamePlayerState.SPECTATOR) players.remove(player);
			Collections.sort(players,new Comparator<GamePlayer>(){
				public int compare(GamePlayer player1,GamePlayer player2){
					Integer score1 = player1.getSettings().getInt("kills");
					Integer score2 = player2.getSettings().getInt("kills");
					return (Integer.compare(score2,score1) != 0 ? Integer.compare(score2,score1) : Integer.compare(player2.getSettings().getInt("deaths"),player1.getSettings().getInt("deaths")));
				}
			});
			for(int i=0;i<PLAYERS;i++){
				if(i < players.size()){
					this.setLine(i+2,"�f"+players.get(i).getSettings().getInt("kills")+" �3- �b"+players.get(i).getPlayer().getName());
				} else {
					this.setLine(i+2,"�f");
				}
			}
			super.update();
		}

		public void updateForPlayer(GamePlayer gPlayer){
			if(this.getGame().getState().isGame()){
				this.addPlayer(gPlayer);
				this.getScoreboard().getTeam("team").addEntry(gPlayer.getPlayer().getName());
			} else {
				this.removePlayer(gPlayer);
				this.getScoreboard().getTeam("team").removeEntry(gPlayer.getPlayer().getName());
			}
		}
	}
}