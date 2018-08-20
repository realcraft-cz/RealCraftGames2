package com.blockparty;

import com.blockparty.pickups.*;
import com.blockparty.pickups.BlockPartyPickup.BlockPartyPickupType;
import com.games.Games;
import com.games.game.*;
import com.games.game.GamePodium.GamePodiumType;
import com.games.game.GameSpectator.SpectatorMenuItem;
import com.games.game.GameStats.GameStatsScore;
import com.games.game.GameStats.GameStatsType;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.StringUtil;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.inventory.ItemStack;
import realcraft.bukkit.utils.MaterialUtil;
import realcraft.bukkit.utils.Particles;

import java.util.ArrayList;
import java.util.HashMap;

public class BlockParty extends Game {

	private BlockPartyState state;
	private BlockPartyScoreboard scoreboard;
	private BlockPartyBossBar bossbar;
	private BlockPartyPickup pickup;

	private static final int ROUNDS = 20;
	public static final int MINY = -20;
	private int round;
	private int countdown;

	public BlockParty(){
		super(GameType.BLOCKPARTY);
		if(this.isMaintenance()) return;
		GameFlag.SPECTATOR = false;
		GameFlag.PICKUP = true;
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

	public BlockPartyPickup getPickup(){
		return pickup;
	}

	public GamePlayer getWinner(){
		GamePlayer winner = null;
		for(GamePlayer gPlayer : this.getPlayers()){
			if(gPlayer.getState() == GamePlayerState.SPECTATOR) continue;
			if(gPlayer.getPlayer().getLocation().getY() < 0) continue;
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
				gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_PLING,1,0.61f);
				gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS,1,0.61f);
			}
			else if(countdown == 2){
				gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_PLING,1,0.8f);
				gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS,1,0.8f);
			}
			else if(countdown == 3){
				gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_PLING,1,1);
				gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS,1,1);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void loadRoundInventory(BlockPartyBlock block){
		ItemStack item = new ItemStack(block.getType());
		for(GamePlayer gPlayer : this.getPlayers()){
			gPlayer.getPlayer().getInventory().remove(Material.WHITE_TERRACOTTA);
			gPlayer.getPlayer().getInventory().remove(Material.ORANGE_TERRACOTTA);
			gPlayer.getPlayer().getInventory().remove(Material.MAGENTA_TERRACOTTA);
			gPlayer.getPlayer().getInventory().remove(Material.LIGHT_BLUE_TERRACOTTA);
			gPlayer.getPlayer().getInventory().remove(Material.YELLOW_TERRACOTTA);
			gPlayer.getPlayer().getInventory().remove(Material.LIME_TERRACOTTA);
			gPlayer.getPlayer().getInventory().remove(Material.PINK_TERRACOTTA);
			gPlayer.getPlayer().getInventory().remove(Material.GRAY_TERRACOTTA);
			gPlayer.getPlayer().getInventory().remove(Material.LIGHT_GRAY_TERRACOTTA);
			gPlayer.getPlayer().getInventory().remove(Material.CYAN_TERRACOTTA);
			gPlayer.getPlayer().getInventory().remove(Material.PURPLE_TERRACOTTA);
			gPlayer.getPlayer().getInventory().remove(Material.BLUE_TERRACOTTA);
			gPlayer.getPlayer().getInventory().remove(Material.BROWN_TERRACOTTA);
			gPlayer.getPlayer().getInventory().remove(Material.GREEN_TERRACOTTA);
			gPlayer.getPlayer().getInventory().remove(Material.RED_TERRACOTTA);
			gPlayer.getPlayer().getInventory().remove(Material.BLACK_TERRACOTTA);
			gPlayer.getPlayer().setFoodLevel(20);
			gPlayer.getPlayer().setSaturation(20);
			for(int i=3;i<6;i++) gPlayer.getPlayer().getInventory().setItem(i,item);
		}
	}

	public void reset(){
		round = 0;
		countdown = 6;
		state = BlockPartyState.WAITING;
		this.getArena().reset();
		this.getArena().chooseDefaultFloor();
		this.getArena().getWorld().setStorm(false);
		this.getArena().getWorld().setThundering(false);
	}

	public void nextRound(){
		this.getArena().getWorld().setStorm(false);
		this.getArena().getWorld().setThundering(false);
		if(pickup != null) pickup.clear();
		int nextRound = round+1;
		if(ROUNDS >= nextRound){
			round = nextRound;
			this.getArena().chooseRandomFloor();
			for(GamePlayer gPlayer : this.getPlayers()){
				gPlayer.getPlayer().getInventory().remove(Material.WHITE_TERRACOTTA);
				gPlayer.getPlayer().getInventory().remove(Material.ORANGE_TERRACOTTA);
				gPlayer.getPlayer().getInventory().remove(Material.MAGENTA_TERRACOTTA);
				gPlayer.getPlayer().getInventory().remove(Material.LIGHT_BLUE_TERRACOTTA);
				gPlayer.getPlayer().getInventory().remove(Material.YELLOW_TERRACOTTA);
				gPlayer.getPlayer().getInventory().remove(Material.LIME_TERRACOTTA);
				gPlayer.getPlayer().getInventory().remove(Material.PINK_TERRACOTTA);
				gPlayer.getPlayer().getInventory().remove(Material.GRAY_TERRACOTTA);
				gPlayer.getPlayer().getInventory().remove(Material.LIGHT_GRAY_TERRACOTTA);
				gPlayer.getPlayer().getInventory().remove(Material.CYAN_TERRACOTTA);
				gPlayer.getPlayer().getInventory().remove(Material.PURPLE_TERRACOTTA);
				gPlayer.getPlayer().getInventory().remove(Material.BLUE_TERRACOTTA);
				gPlayer.getPlayer().getInventory().remove(Material.BROWN_TERRACOTTA);
				gPlayer.getPlayer().getInventory().remove(Material.GREEN_TERRACOTTA);
				gPlayer.getPlayer().getInventory().remove(Material.RED_TERRACOTTA);
				gPlayer.getPlayer().getInventory().remove(Material.BLACK_TERRACOTTA);
				gPlayer.getPlayer().getInventory().setHeldItemSlot(4);
				if(gPlayer.getState() != GamePlayerState.SPECTATOR){
					this.getArena().teleportAboveFloor(gPlayer);
					Particles.HEART.display(0f,0f,0f,0f,1,gPlayer.getPlayer().getEyeLocation().add(0f,0.5f,0f),64);
				}
			}
			if(round%2 != 0) this.placePickup();
		} else {
			this.setState(GameState.ENDING);
		}
	}

	public void clearFloor(){
		if(pickup != null) pickup.remove();
		this.getArena().clearFloor();
		for(GamePlayer gPlayer : this.getPlayers()){
			gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_SNOW_BREAK,1f,1f);
			gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_SNOW_BREAK,1f,0.5f);
			gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ENDER_DRAGON_FLAP,1f,0.8f);
		}
	}

	private void placePickup(){
		pickup = this.getRandomPickup();
		Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				pickup.place();
			}
		},20);
	}

	private BlockPartyPickup getRandomPickup(){
		switch(BlockPartyPickupType.getRandomType()){
			case JUMP: return new BlockPartyPickupJump(this);
			case BLINDNESS: return new BlockPartyPickupBlindness(this);
			case COLORBLINDNESS: return new BlockPartyPickupColorBlindness(this);
			case SNOWBALLS: return new BlockPartyPickupSnowballs(this);
			case SHOVELS: return new BlockPartyPickupShovels(this);
			case SILVERFISH: return new BlockPartyPickupSilverfish(this);
			case BABYZOMBIE: return new BlockPartyPickupBabyzombie(this);
			case ACID: return new BlockPartyPickupAcid(this);
			case THUNDERSTORM: return new BlockPartyPickupThunderstorm(this);
		}
		return null;
	}

	public HashMap<Integer,SpectatorMenuItem> getSpectatorMenuItems(){
		return new HashMap<Integer,SpectatorMenuItem>();
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

		private String getRandomBlockChatColor(){
			BlockPartyBlock block = this.getGame().getArena().getCurrentBlock();
			switch(MaterialUtil.getDyeColor(block.getType())){
				case WHITE: return "§f";
				case ORANGE: return "§6";
				case RED: return "§4";
				case LIGHT_BLUE: return "§b";
				case YELLOW: return "§e";
				case LIME: return "§a";
				case MAGENTA: return "§d";
				case GRAY: return "§8";
				case LIGHT_GRAY: return "§7";
				case CYAN: return "§3";
				case PURPLE: return "§5";
				case BLUE: return "§1";
				case BROWN: return "§0";
				case GREEN: return "§2";
				case PINK: return "§c";
				case BLACK: return "§0";
			}
			return "§f";
		}

		private String getRandomBlockColor(){
			BlockPartyBlock block = this.getGame().getArena().getCurrentBlock();
			switch(MaterialUtil.getDyeColor(block.getType())){
				case WHITE: return "WHITE";
				case ORANGE: return "ORANGE";
				case RED: return "MAGENTA";
				case LIGHT_BLUE: return "LIGHT BLUE";
				case YELLOW: return "YELLOW";
				case LIME: return "LIME";
				case MAGENTA: return "PINK";
				case GRAY: return "GRAY";
				case LIGHT_GRAY: return "LIGHT GRAY";
				case CYAN: return "CYAN";
				case PURPLE: return "PURPLE";
				case BLUE: return "BLUE";
				case BROWN: return "BROWN";
				case GREEN: return "GREEN";
				case PINK: return "RED";
				case BLACK: return "BLACK";
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
}