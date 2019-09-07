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
import com.games.utils.RandomUtil;
import com.games.utils.StringUtil;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import realcraft.bukkit.utils.LocationUtil;
import realcraft.bukkit.utils.MaterialUtil;
import realcraft.bukkit.utils.Particles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlockParty extends Game {

	private BlockPartyState state;
	private BlockPartyScoreboard scoreboard;
	private BlockPartyBossBar bossbar;
	private BlockPartyPickup pickup;
	private Material currentBlock;

	private BlockVector3 minLoc;
	private BlockVector3 maxLoc;
	private Location spectatorLocation;
	private Location gameLocation;

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
		BlockPartyArena arena = new BlockPartyArena(this,0);
		this.setArena(arena);
		this.addArena(arena);
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

	public Material getCurrentBlock(){
		return currentBlock;
	}

	public void chooseRandomBlock(){
		currentBlock = this.getArena().getCurrentFloor().getRandomBlock();
		this.loadRoundInventory(currentBlock);
	}

	public BlockVector3 getMinLoc(){
		if(minLoc == null){
			Location location = LocationUtil.getConfigLocation(this.getConfig(),"minLoc");
			minLoc = BlockVector3.at(location.getBlockX(),location.getBlockY(),location.getBlockZ());
		}
		return minLoc;
	}

	public BlockVector3 getMaxLoc(){
		if(maxLoc == null){
			Location location = LocationUtil.getConfigLocation(this.getConfig(),"maxLoc");
			maxLoc = BlockVector3.at(location.getBlockX(),location.getBlockY(),location.getBlockZ());
		}
		return maxLoc;
	}

	public Location getSpectatorLocation(){
		if(spectatorLocation == null) spectatorLocation = LocationUtil.getConfigLocation(this.getConfig(),"spectator");
		return spectatorLocation;
	}

	public Location getGameLocation(){
		if(gameLocation == null) gameLocation = LocationUtil.getConfigLocation(this.getConfig(),"gameSpawn");
		return gameLocation;
	}

	public GamePlayer getWinner(){
		GamePlayer winner = null;
		for(GamePlayer gPlayer : this.getPlayers()){
			if(gPlayer.getState() == GamePlayerState.SPECTATOR) continue;
			if(gPlayer.getPlayer().getLocation().getY() < 0) continue;
			if(winner == null) winner = gPlayer;
			else return null;
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
	public void loadRoundInventory(Material type){
		ItemStack item = new ItemStack(type);
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
		for(BlockPartyFloor floor : this.getArena().getFloors()){
			floor.setUsed(false);
		}
		this.chooseDefaultFloor();
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
			this.chooseRandomFloor();
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
					this.teleportAboveFloor(gPlayer);
					Particles.HEART.display(0f,0f,0f,0f,1,gPlayer.getPlayer().getEyeLocation().add(0f,0.5f,0f),64);
				}
			}
			if(round%2 != 0) this.placePickup();
		} else {
			this.setState(GameState.ENDING);
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
		return new HashMap<>();
	}

	public void chooseDefaultFloor(){
		this.clearFloor(true);
		this.getArena().setCurrentFloor(this.getArena().getFloors().get(0));
		this.getArena().resetRegion();
		this.getArena().getCurrentFloor().setUsed(true);
	}

	public void chooseRandomFloor(){
		this.clearFloor(true);
		this.getArena().setCurrentFloor(this.getRandomFloor());
		this.getArena().resetRegion();
		this.getArena().getCurrentFloor().setUsed(true);
	}

	private BlockPartyFloor getRandomFloor(){
		BlockPartyFloor floor = this.getArena().getFloors().get(RandomUtil.getRandomInteger(0,this.getArena().getFloors().size()-1));
		if(floor.isUsed()) floor = this.getRandomFloor();
		return floor;
	}

	public void clearFloor(){
		this.clearFloor(false);
		for(GamePlayer gPlayer : this.getPlayers()){
			gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_SNOW_BREAK,1f,1f);
			gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_SNOW_BREAK,1f,0.5f);
			gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ENDER_DRAGON_FLAP,1f,0.8f);
		}
	}

	private void clearFloor(boolean force){
		if(pickup != null) pickup.remove();
		for(int y=this.getMinLoc().getBlockY();y<=this.getMaxLoc().getBlockY();y++){
			for(int x=this.getMinLoc().getBlockX();x<=this.getMaxLoc().getBlockX();x++){
				for(int z=this.getMinLoc().getBlockZ();z<=this.getMaxLoc().getBlockZ();z++){
					Block block = this.getArena().getWorld().getBlockAt(x,y,z);
					if(force || block.getType() != this.getCurrentBlock()){
						block.setType(Material.AIR);
					}
					List<Entity> entities = (List<Entity>) this.getArena().getWorld().getNearbyEntities(this.getGameLocation(),20,10,20);
					for(Entity entity : entities){
						if(!(entity instanceof Item)) continue;
						entity.remove();
					}
				}
			}
		}
	}

	public void teleportAboveFloor(GamePlayer gPlayer){
		Location location = gPlayer.getPlayer().getLocation().clone();
		if(location.getBlockY() >= 0){
			int maxY = 0;
			for(Vector2D vector : POS_VOLUMES){
				for(int y=this.getMaxLoc().getBlockY();y>=this.getMinLoc().getBlockY();y--){
					if(!location.getWorld().getBlockAt(location.getBlockX()+vector.x,y,location.getBlockZ()+vector.z).isEmpty()){
						if(maxY < y) maxY = y;
					}
				}
			}
			if(maxY != 0){
				if(location.getBlockY() <= maxY){
					location.setY(maxY+1.1);
					gPlayer.getPlayer().teleport(location);
				}
			}
		}
	}

	public Location getRandomPickupLocation(){
		int randX = RandomUtil.getRandomInteger(this.getMinLoc().getBlockX(),this.getMaxLoc().getBlockX());
		int randZ = RandomUtil.getRandomInteger(this.getMinLoc().getBlockZ(),this.getMaxLoc().getBlockZ());
		int randY = 0;
		for(int y=this.getMaxLoc().getBlockY();y>=this.getMinLoc().getBlockY();y--){
			if(this.getArena().getWorld().getBlockAt(randX,y,randZ).isEmpty() && !this.getArena().getWorld().getBlockAt(randX,y-1,randZ).isEmpty()){
				randY = y;
				break;
			}
		}
		if(randY == 0) return this.getRandomPickupLocation();
		return new Location(this.getArena().getWorld(),randX,randY,randZ);
	}

	public Location getStartLocation(int index,int max){
		Location location = this.getGameLocation().clone();
		double angle = index*(2*Math.PI)/max;
		Vector3 vector = Vector3.at(Math.cos(angle),0,Math.sin(angle)).multiply(4.0);
		location.add(vector.getX(),vector.getY(),vector.getZ());
		location.setDirection(this.getGameLocation().getDirection());
		location = this.setLocationLookingAt(location,this.getGameLocation());
		return location;
	}

	private Location setLocationLookingAt(Location loc,Location lookat){
		loc = loc.clone();

		double dx = lookat.getX() - loc.getX();
		double dy = lookat.getY() - loc.getY();
		double dz = lookat.getZ() - loc.getZ();

		if(dx != 0){
			if(dx < 0){
				loc.setYaw((float) (1.5 * Math.PI));
			} else {
				loc.setYaw((float) (0.5 * Math.PI));
			}
			loc.setYaw(loc.getYaw() - (float) Math.atan(dz / dx));
		} else if(dz < 0){
			loc.setYaw((float) Math.PI);
		}

		double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
		loc.setPitch((float) -Math.atan(dy / dxz));
		loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
		loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);

		return loc;
	}

	private static final Vector2D[] POS_VOLUMES = new Vector2D[]{
			new Vector2D(0,0),
			new Vector2D(0,1),
			new Vector2D(1,0),
			new Vector2D(0,-1),
			new Vector2D(-1,0),
	};

	public static class Vector2D {
		public int x;
		public int z;

		public Vector2D(int x,int z){
			this.x = x;
			this.z = z;
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
			Material type = this.getGame().getCurrentBlock();
			switch(MaterialUtil.getDyeColor(type)){
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
			Material type = this.getGame().getCurrentBlock();
			switch(MaterialUtil.getDyeColor(type)){
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