package com.hidenseek;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.games.Games;
import com.games.game.Game;
import com.games.game.GameFlag;
import com.games.game.GamePodium;
import com.games.game.GamePodium.GamePodiumType;
import com.games.game.GameScoreboard;
import com.games.game.GameStats.GameStatsScore;
import com.games.game.GameType;
import com.games.player.GamePlayer;
import com.games.utils.FormatUtil;
import com.games.utils.StringUtil;
import com.hidenseek.HidenSeekTeam.HidenSeekTeamType;

public class HidenSeek extends Game {

	private HidenSeekScoreboard scoreboard;
	private HidenSeekTeams teams;
	private HashMap<GamePlayer,HidenSeekUser> users = new HashMap<GamePlayer,HidenSeekUser>();
	private int cycleTime;

	public HidenSeek(){
		super(GameType.HIDENSEEK);
		if(this.isMaintenance()) return;
		GameFlag.USE_DOOR = true;
		new HidenSeekListeners(this);
		this.scoreboard = new HidenSeekScoreboard(this);
		this.teams = new HidenSeekTeams(this);
		new HidenSeekPodium(this,GamePodiumType.LEFT);
		new HidenSeekPodium(this,GamePodiumType.RIGHT);
		this.loadArenas();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				if(HidenSeek.this.getState().isGame()){
					cycleTime ++;
					for(GamePlayer gPlayer : HidenSeek.this.getTeams().getTeam(HidenSeekTeamType.HIDERS).getPlayers()){
						HidenSeek.this.getUser(gPlayer).run();
					}
					for(GamePlayer gPlayer : HidenSeek.this.getTeams().getTeam(HidenSeekTeamType.SEEKERS).getPlayers()){
						HidenSeek.this.getUser(gPlayer).updateWeaponDamage();
					}
					if(cycleTime%2 == 0){
						for(GamePlayer gPlayer : HidenSeek.this.getTeams().getTeam(HidenSeekTeamType.SEEKERS).getPlayers()){
							HidenSeek.this.getUser(gPlayer).runTracker();
							HidenSeek.this.getUser(gPlayer).runWeaponDamage();
						}
					}
					if(cycleTime == 10 || cycleTime == 20){
						for(GamePlayer gPlayer : HidenSeek.this.getTeams().getTeam(HidenSeekTeamType.HIDERS).getPlayers()){
							HidenSeek.this.getUser(gPlayer).runDisguiseCountdown();
						}
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
						new HidenSeekArena(this,file.getName());
					}
				}
			}
		}
	}

	public HidenSeekArena getArena(){
		return (HidenSeekArena) super.getArena();
	}

	public HidenSeekScoreboard getScoreboard(){
		return scoreboard;
	}

	public HidenSeekTeams getTeams(){
		return teams;
	}

	public ArrayList<HidenSeekUser> getUsers(){
		return new ArrayList<HidenSeekUser>(users.values());
	}

	public HidenSeekUser getUser(GamePlayer gPlayer){
		if(!users.containsKey(gPlayer)) users.put(gPlayer,new HidenSeekUser(this,gPlayer));
		return users.get(gPlayer);
	}

	public void loadGameInventory(GamePlayer gPlayer){
		ItemStack itemStack;
		ItemMeta meta;

		if(this.getTeams().getPlayerTeam(gPlayer).getType() == HidenSeekTeamType.HIDERS){
			if(this.getGameTimeDefault()-this.getGameTime() >= 30){
				itemStack = new ItemStack(Material.STICK,1);
				meta = itemStack.getItemMeta();
				meta.addEnchant(Enchantment.KNOCKBACK,2,false);
				itemStack.setItemMeta(meta);
				gPlayer.getPlayer().getInventory().setItem(0,itemStack);
			}

			itemStack = new ItemStack(Material.SLIME_BALL,1);
			meta = itemStack.getItemMeta();
			meta.setDisplayName("§a§lZmena bloku §r§7(klikni na blok)");
			itemStack.setItemMeta(meta);
			gPlayer.getPlayer().getInventory().setItem(3,itemStack);

			itemStack = new ItemStack(Material.SUGAR,1);
			meta = itemStack.getItemMeta();
			meta.setDisplayName("§3§lMeow");
			itemStack.setItemMeta(meta);
			gPlayer.getPlayer().getInventory().setItem(4,itemStack);

			itemStack = new ItemStack(Material.FIREWORK,5);
			meta = itemStack.getItemMeta();
			meta.setDisplayName("§e§lOhnostroj §r§7(+20 coins)");
			itemStack.setItemMeta(meta);
			gPlayer.getPlayer().getInventory().setItem(5,itemStack);

			gPlayer.getPlayer().getInventory().setHeldItemSlot(0);
		}
		else if(this.getTeams().getPlayerTeam(gPlayer).getType() == HidenSeekTeamType.SEEKERS){
			if(this.getGameTime() <= 60){
				itemStack = new ItemStack(Material.COMPASS,1);
				meta = itemStack.getItemMeta();
				meta.setDisplayName("§b§lLokalizator");
				itemStack.setItemMeta(meta);
				gPlayer.getPlayer().getInventory().setItem(1,itemStack);
			}

			itemStack = new ItemStack(Material.IRON_AXE,1);
			gPlayer.getPlayer().getInventory().setItem(0,itemStack);

			itemStack = new ItemStack(Material.IRON_HELMET,1);
			gPlayer.getPlayer().getInventory().setHelmet(itemStack);

	        itemStack = new ItemStack(Material.IRON_CHESTPLATE,1);
	        gPlayer.getPlayer().getInventory().setChestplate(itemStack);

	        itemStack = new ItemStack(Material.IRON_LEGGINGS,1);
	        gPlayer.getPlayer().getInventory().setLeggings(itemStack);

	        itemStack = new ItemStack(Material.IRON_BOOTS,1);
	        gPlayer.getPlayer().getInventory().setBoots(itemStack);

	        gPlayer.getPlayer().getInventory().setHeldItemSlot(0);
	        gPlayer.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
		}
	}

	public class HidenSeekScoreboard extends GameScoreboard {
		private Team teamHiders;
		private Team teamSeekers;

		public HidenSeekScoreboard(Game game){
			super(game,GameScoreboardType.GAME);
			this.setTitle("");
			this.setLine(0,"");
			this.setLine(1,"§b§lHiders");
			this.setLine(2,"§f0 hracu ");
			this.setLine(3,"");
			this.setLine(4,"§c§lSeekers");
			this.setLine(5,"§f0 hracu");
			this.setLine(6,"");
			this.setLine(7,"§ewww.realcraft.cz");
			if(teamHiders == null) teamHiders = this.getScoreboard().registerNewTeam("0teamHiders");
			if(teamSeekers == null) teamSeekers = this.getScoreboard().registerNewTeam("1teamSeekers");
			teamHiders.setCanSeeFriendlyInvisibles(true);
			teamHiders.setOption(Option.COLLISION_RULE,OptionStatus.NEVER);
			teamSeekers.setOption(Option.COLLISION_RULE,OptionStatus.NEVER);
			teamHiders.setAllowFriendlyFire(false);
			teamSeekers.setAllowFriendlyFire(false);
			teamHiders.setColor(HidenSeekTeamType.HIDERS.getChatColor());
			teamSeekers.setColor(HidenSeekTeamType.SEEKERS.getChatColor());
			teamHiders.setPrefix(HidenSeekTeamType.HIDERS.getChatColor().toString());
			teamSeekers.setPrefix(HidenSeekTeamType.SEEKERS.getChatColor().toString());
		}

		public HidenSeek getGame(){
			return (HidenSeek) super.getGame();
		}

		public void update(){
			this.setTitle(this.getGame().getType().getColor()+"§l"+this.getGame().getArena().getName()+"§r - "+FormatUtil.timeFormat(this.getGame().getGameTime()));
			this.setLine(2,"§f"+this.getGame().getTeams().getTeam(HidenSeekTeamType.HIDERS).getPlayers().size()+" "+StringUtil.inflect(this.getGame().getTeams().getTeam(HidenSeekTeamType.HIDERS).getPlayers().size(),new String[]{"hrac","hraci","hracu"})+" ");
			this.setLine(5,"§f"+this.getGame().getTeams().getTeam(HidenSeekTeamType.SEEKERS).getPlayers().size()+" "+StringUtil.inflect(this.getGame().getTeams().getTeam(HidenSeekTeamType.SEEKERS).getPlayers().size(),new String[]{"hrac","hraci","hracu"}));
			super.update();
		}

		public void updateForPlayer(GamePlayer gPlayer){
			if(this.getGame().getState().isGame()){
				this.addPlayer(gPlayer);
				if(this.getGame().getTeams().getPlayerTeam(gPlayer) != null){
					teamHiders.removeEntry(gPlayer.getPlayer().getName());
					teamSeekers.removeEntry(gPlayer.getPlayer().getName());
					if(this.getGame().getTeams().getPlayerTeam(gPlayer).getType() == HidenSeekTeamType.HIDERS) teamHiders.addEntry(gPlayer.getPlayer().getName());
					else if(this.getGame().getTeams().getPlayerTeam(gPlayer).getType() == HidenSeekTeamType.SEEKERS) teamSeekers.addEntry(gPlayer.getPlayer().getName());
				}
			} else {
				this.removePlayer(gPlayer);
				teamHiders.removeEntry(gPlayer.getPlayer().getName());
				teamSeekers.removeEntry(gPlayer.getPlayer().getName());
			}
		}
	}

	public boolean isBlockValid(Block block){
		return this.isBlockValid(block.getType());
	}

	public boolean isBlockValid(Material material){
		switch(material){
			case ANVIL: return true;
			case BOOKSHELF: return true;
			case CAKE_BLOCK: return true;
			case COAL_BLOCK: return true;
			case COAL_ORE: return true;
			case DIAMOND_BLOCK: return true;
			case DIAMOND_ORE: return true;
			case DISPENSER: return true;
			case DROPPER: return true;
			case EMERALD_BLOCK: return true;
			case EMERALD_ORE: return true;
			case FURNACE: return true;
			case GOLD_BLOCK: return true;
			case GOLD_ORE: return true;
			case HAY_BLOCK: return true;
			case IRON_BLOCK: return true;
			case IRON_ORE: return true;
			case JACK_O_LANTERN: return true;
			case JUKEBOX: return true;
			case LAPIS_BLOCK: return true;
			case LAPIS_ORE: return true;
			case MELON_BLOCK: return true;
			case MYCEL: return true;
			case NOTE_BLOCK: return true;
			case PUMPKIN: return true;
			case REDSTONE_LAMP_OFF: return true;
			case REDSTONE_LAMP_ON: return true;
			case REDSTONE_ORE: return true;
			case SLIME_BLOCK: return true;
			case SPONGE: return true;
			case TNT: return true;
			case WOOL: return true;
			case WORKBENCH: return true;
			default: return false;
		}
	}

	public boolean isEntityValid(EntityType entity){
		switch(entity){
			case COW: return true;
			case PIG: return true;
			case SHEEP: return true;
			case CHICKEN: return true;
			case RABBIT: return true;
			default: return false;
		}
	}

	public class HidenSeekPodium extends GamePodium {
		public HidenSeekPodium(Game game,GamePodiumType type){
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