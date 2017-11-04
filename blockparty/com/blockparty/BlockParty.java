package com.blockparty;

import java.util.ArrayList;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.inventory.ItemStack;

import com.games.game.Game;
import com.games.game.GameBossBar;
import com.games.game.GamePodium;
import com.games.game.GamePodium.GamePodiumType;
import com.games.game.GameScoreboard;
import com.games.game.GameState;
import com.games.game.GameStats.GameStatsScore;
import com.games.game.GameType;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.Particles;
import com.games.utils.StringUtil;

public class BlockParty extends Game {

	private BlockPartyState state;
	private BlockPartyScoreboard scoreboard;
	private BlockPartyBossBar bossbar;

	private static final int ROUNDS = 20;
	public static final int MINY = -20;
	private int round;
	private int countdown;

	public BlockParty(){
		super(GameType.BLOCKPARTY);
		if(this.isMaintenance()) return;
		new BlockPartyListeners(this);
		this.scoreboard = new BlockPartyScoreboard(this);
		this.bossbar = new BlockPartyBossBar(this);
		new BlockPartyPodium(this,GamePodiumType.LEFT);
		new BlockPartyPodium(this,GamePodiumType.RIGHT);
		this.loadArenas();
	}

	public void loadArenas(){
		new BlockPartyArena(this,"BlockParty");
	}

	public BlockPartyArena getArena(){
		return (BlockPartyArena) super.getArena();
	}

	public BlockPartyState getRoundState(){
		return state;
	}

	public void setRoundState(BlockPartyState state){
		this.state = state;
	}

	public int getRound(){
		return round;
	}

	public int getCountdown(){
		return countdown;
	}

	public void setCountdown(int countdown){
		this.countdown = countdown;
	}

	public BlockPartyScoreboard getScoreboard(){
		return scoreboard;
	}

	public BlockPartyBossBar getBossBar(){
		return bossbar;
	}

	public GamePlayer getWinner(){
		GamePlayer winner = null;
		for(GamePlayer gPlayer : this.getPlayers()){
			if(gPlayer.getState() == GamePlayerState.SPECTATOR) continue;
			if(winner == null) winner = gPlayer;
			else if(winner != null) return null;
		}
		return winner;
	}

	public int getRoundSpeed(){
		int order = ROUNDS-round;
		switch(order){
			case 0: return 0;
			case 1: return 0;
			case 2: return 0;
			case 3: return 0;
			case 4: return 0;
			case 5: return 0;
			case 6: return 0;
			case 7: return 0;
			case 8: return 0;
			case 9: return 1;
			case 10: return 1;
			case 11: return 2;
			case 12: return 2;
			case 13: return 3;
			case 14: return 3;
			case 15: return 4;
			case 16: return 4;
			case 17: return 5;
			case 18: return 5;
			case 19: return 6;
			case 20: return 6;
		}
		return 6;
	}

	public void playRoundSound(int countdown){
		for(GamePlayer gPlayer : this.getPlayers()){
			if(countdown == 1){
				gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_PLING,1,0.61f);
				gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_BASS,1,0.61f);
			}
			else if(countdown == 2){
				gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_PLING,1,0.8f);
				gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_BASS,1,0.8f);
			}
			else if(countdown == 3){
				gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_PLING,1,1);
				gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_BASS,1,1);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void loadRoundInventory(Block block){
		for(GamePlayer gPlayer : this.getPlayers()){
			ItemStack item = new ItemStack(block.getType(),1,(short)0,block.getData());
			gPlayer.getPlayer().getInventory().clear();
			gPlayer.getPlayer().setFoodLevel(20);
			gPlayer.getPlayer().setSaturation(20);
			for(int i=0;i<9;i++) gPlayer.getPlayer().getInventory().setItem(i,item);
		}
	}

	public void reset(){
		round = 0;
		countdown = 5;
		state = BlockPartyState.WAITING;
		this.getArena().reset();
		this.getArena().chooseDefaultFloor();
	}

	public void nextRound(){
		int nextRound = round+1;
		if(ROUNDS >= nextRound){
			round = nextRound;
			this.getArena().chooseRandomFloor();
			for(GamePlayer gPlayer : this.getPlayers()){
				gPlayer.getPlayer().getInventory().clear();
				if(gPlayer.getState() != GamePlayerState.SPECTATOR){
					this.getArena().teleportAboveFloor(gPlayer);
					Particles.HEART.display(0f,0f,0f,0f,1,gPlayer.getPlayer().getEyeLocation().add(0f,0.5f,0f),64);
				}
			}
		} else {
			this.setState(GameState.ENDING);
		}
	}

	public void clearFloor(){
		this.getArena().clearFloor();
		for(GamePlayer gPlayer : this.getPlayers()){
			gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_SNOW_BREAK,1f,1f);
			gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_SNOW_BREAK,1f,0.7f);
			gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ENDERDRAGON_FLAP,1f,1f);
		}
	}

	public class BlockPartyScoreboard extends GameScoreboard {
		public BlockPartyScoreboard(Game game){
			super(game,GameScoreboardType.GAME);
			this.setTitle("§e§lBlockParty");
			this.setLine(0,"");
			this.setLine(1,"§a§lRychlost");
			this.setLine(2,"§f7 sekund");
			this.setLine(3,"");
			this.setLine(4,"§ewww.realcraft.cz");
		}

		public BlockParty getGame(){
			return (BlockParty) super.getGame();
		}

		public void update(){
			this.setLine(2,"§f"+(this.getGame().getRoundSpeed()+1)+" "+StringUtil.inflect(this.getGame().getRoundSpeed()+1,new String[]{"sekunda","sekundy","sekund"}));
			super.update();
		}

		public void updateForPlayer(GamePlayer gPlayer){
			if(this.getGame().getState().isGame()) this.addPlayer(gPlayer);
			else this.removePlayer(gPlayer);
		}
	}

	public class BlockPartyBossBar extends GameBossBar {
		public BlockPartyBossBar(Game game){
			super(game,GameBossBarType.GAME);
			this.setColor(BarColor.PURPLE);
			this.setStyle(BarStyle.SOLID);
			this.update();
		}

		public BlockParty getGame(){
			return (BlockParty) super.getGame();
		}

		public void update(){
			this.setTitle(this.getBlockPartyTitle());
		}

		public void updateForPlayer(GamePlayer gPlayer){
			if(this.getGame().getState().isGame()) this.addPlayer(gPlayer);
			else this.removePlayer(gPlayer);
		}

		private String getBlockPartyTitle(){
			if(this.getGame().getRoundState() == BlockPartyState.WAITING){
				return "§f§lREADY";
			}
			else if(this.getGame().getRoundState() == BlockPartyState.COUNTDOWN){
				String prefix = "";
				if(this.getGame().getCountdown() == 0) prefix = this.getRandomBlockChatColor()+"\u2588 ";
				else if(this.getGame().getCountdown() == 1) prefix = this.getRandomBlockChatColor()+"\u2588\u2588 ";
				else if(this.getGame().getCountdown() == 2) prefix = this.getRandomBlockChatColor()+"\u2588\u2588\u2588 ";
				else if(this.getGame().getCountdown() == 3) prefix = this.getRandomBlockChatColor()+"\u2588\u2588\u2588\u2588 ";
				else if(this.getGame().getCountdown() == 4) prefix = this.getRandomBlockChatColor()+"\u2588\u2588\u2588\u2588\u2588 ";
				else if(this.getGame().getCountdown() == 5) prefix = this.getRandomBlockChatColor()+"\u2588\u2588\u2588\u2588\u2588\u2588 ";
				else if(this.getGame().getCountdown() == 6) prefix = this.getRandomBlockChatColor()+"\u2588\u2588\u2588\u2588\u2588\u2588\u2588 ";
				return "§l"+prefix+"§r§l"+this.getRandomBlockColor()+" "+prefix;
			}
			else if(this.getGame().getRoundState() == BlockPartyState.FALLING){
				return "§c\u2716§r §lSTOP§r §c\u2716";
			}
			return "";
		}

		@SuppressWarnings("deprecation")
		private String getRandomBlockChatColor(){
			Block block = this.getGame().getArena().getCurrentBlock();
			switch(block.getData()){
				case 0: return "§f";
				case 1: return "§6";
				case 2: return "§4";
				case 3: return "§b";
				case 4: return "§e";
				case 5: return "§a";
				case 6: return "§d";
				case 7: return "§8";
				case 8: return "§7";
				case 9: return "§3";
				case 10: return "§5";
				case 11: return "§1";
				case 12: return "§0";
				case 13: return "§2";
				case 14: return "§c";
				case 15: return "§0";
			}
			return "§f";
		}

		@SuppressWarnings("deprecation")
		private String getRandomBlockColor(){
			Block block = this.getGame().getArena().getCurrentBlock();
			switch(block.getData()){
				case 0: return "WHITE";
				case 1: return "ORANGE";
				case 2: return "MAGENTA";
				case 3: return "LIGHT BLUE";
				case 4: return "YELLOW";
				case 5: return "LIME";
				case 6: return "PING";
				case 7: return "GRAY";
				case 8: return "LIGHT GRAY";
				case 9: return "CYAN";
				case 10: return "PURPLE";
				case 11: return "BLUE";
				case 12: return "BROWN";
				case 13: return "GREEN";
				case 14: return "RED";
				case 15: return "BLACK";
			}
			return "UNKNOWN";
		}
	}

	public class BlockPartyPodium extends GamePodium {
		public BlockPartyPodium(Game game,GamePodiumType type){
			super(game,type);
		}

		@Override
		public void update(){
			ArrayList<GameStatsScore> scores = this.getGame().getStats().getScores(GamePodiumType.LEFT.getId());
			int index = 0;
			for(GamePodiumStand stand : this.getStands()){
				if(scores.size() <= index) continue;
				if(this.getType() == GamePodiumType.LEFT) stand.setData(scores.get(index).getName(),scores.get(index).getValue()+" vyher");
				else if(this.getType() == GamePodiumType.RIGHT) stand.setData(scores.get(index).getName(),scores.get(index).getValue()+" vyher");
				index ++;
			}
		}
	}
}