package com.hidenseek;

import com.games.Games;
import com.games.game.*;
import com.games.game.GamePodium.GamePodiumType;
import com.games.game.GameSpectator.SpectatorMenuItem;
import com.games.game.GameSpectator.SpectatorMenuItemPlayer;
import com.games.game.GameStats.GameStatsScore;
import com.games.game.GameStats.GameStatsType;
import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.games.utils.FormatUtil;
import com.games.utils.StringUtil;
import com.hidenseek.HidenSeekTeam.HidenSeekTeamType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import realcraft.bukkit.database.DB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

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
					if(cycleTime >= 20) cycleTime = 0;
				}
			}
		},1,1);
	}

	public void loadArenas(){
		ResultSet rs = DB.query("SELECT * FROM "+MAPS+" WHERE map_type = '"+this.getType().getId()+"' AND map_state = '1'");
		try {
			while(rs.next()){
				int id = rs.getInt("map_id");
				this.addArena(new HidenSeekArena(this,id));
			}
			rs.close();
		} catch (SQLException e){
			e.printStackTrace();
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

			itemStack = new ItemStack(Material.FIREWORK_ROCKET,5);
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

			gPlayer.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(100);

			itemStack = new ItemStack(Material.IRON_AXE,1);
			gPlayer.getPlayer().getInventory().setItem(0,itemStack);

			itemStack = new ItemStack(Material.CHAINMAIL_CHESTPLATE,1);
			gPlayer.getPlayer().getInventory().setChestplate(itemStack);

	        gPlayer.getPlayer().getInventory().setHeldItemSlot(0);
	        gPlayer.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
		}
	}

	public HashMap<Integer,SpectatorMenuItem> getSpectatorMenuItems(){
		HashMap<Integer,SpectatorMenuItem> items = new HashMap<Integer,SpectatorMenuItem>();
		int row = 0;
		int column = 0;
		HidenSeekTeam team = this.getTeams().getTeam(HidenSeekTeamType.SEEKERS);
		for(GamePlayer gPlayer : team.getPlayers()){
			int index = (row*9)+(column++);
			items.put(index,new SpectatorMenuItemPlayer(index,team.getType().getChatColor()+gPlayer.getPlayer().getName(),gPlayer));
			if(column == 8){
				column = 0;
				row ++;
			}
		}
		return items;
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
				else if(gPlayer.getState() == GamePlayerState.SPECTATOR){
					this.addSpectator(gPlayer);
				}
			} else {
				this.removeSpectator(gPlayer);
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
			case ANVIL:
			case BARREL:
			case BLAST_FURNACE:
			case BOOKSHELF:
			case CAKE:
			case COAL_BLOCK:
			case COAL_ORE:
			case DIAMOND_BLOCK:
			case DIAMOND_ORE:
			case DISPENSER:
			case DROPPER:
			case EMERALD_BLOCK:
			case EMERALD_ORE:
			case FURNACE:
			case GOLD_BLOCK:
			case GOLD_ORE:
			case HAY_BLOCK:
			case IRON_BLOCK:
			case IRON_ORE:
			case JACK_O_LANTERN:
			case JUKEBOX:
			case LAPIS_BLOCK:
			case LAPIS_ORE:
			case LECTERN:
			case MELON:
			case NOTE_BLOCK:
			case PUMPKIN:
			case CARVED_PUMPKIN:
			case REDSTONE_LAMP:
			case REDSTONE_ORE:
			case SLIME_BLOCK:
			case SMOKER:
			case SPONGE:
			case TNT:
			case CRAFTING_TABLE:
			case WHITE_WOOL:
			case ORANGE_WOOL:
			case MAGENTA_WOOL:
			case LIGHT_BLUE_WOOL:
			case YELLOW_WOOL:
			case LIME_WOOL:
			case PINK_WOOL:
			case GRAY_WOOL:
			case LIGHT_GRAY_WOOL:
			case CYAN_WOOL:
			case PURPLE_WOOL:
			case BLUE_WOOL:
			case BROWN_WOOL:
			case GREEN_WOOL:
			case RED_WOOL:
			case BLACK_WOOL:
				return true;
		}

		return false;
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