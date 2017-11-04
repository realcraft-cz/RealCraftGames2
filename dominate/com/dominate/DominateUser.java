package com.dominate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import com.blockparty.BlockParty;
import com.dominate.DominateKit.DominateKitType;
import com.dominate.DominateTeam.DominateTeamType;
import com.dominate.skills.DominateSkill;
import com.dominate.skills.DominateSkill.DominateSkillType;
import com.dominate.skills.DominateSkillArrowBlindness;
import com.dominate.skills.DominateSkillArrowExplosive;
import com.dominate.skills.DominateSkillArrowFire;
import com.dominate.skills.DominateSkillArrowGroup;
import com.dominate.skills.DominateSkillFire;
import com.dominate.skills.DominateSkillFrostWalk;
import com.dominate.skills.DominateSkillIceTrap;
import com.dominate.skills.DominateSkillJump;
import com.dominate.skills.DominateSkillRecall;
import com.dominate.skills.DominateSkillSmashDown;
import com.dominate.skills.DominateSkillSoup;
import com.dominate.skills.DominateSkillWaterBottle;
import com.dominate.skills.DominateSkillWeb;
import com.games.game.Game;
import com.games.game.GameBossBar;
import com.games.player.GamePlayer;

public class DominateUser {

	private Dominate game;
	private DominateKitType kit;
	private GamePlayer gPlayer;
	private DominateBossBar bossbar;
	private HashMap<DominateSkillType,DominateSkill> skills = new HashMap<DominateSkillType,DominateSkill>();
	private long lastInSpawn;

	public DominateUser(Dominate game,GamePlayer gPlayer){
		this.game = game;
		this.gPlayer = gPlayer;
		this.bossbar = new DominateBossBar(game);
		this.kit = DominateKitType.ARCHER;
		this.initSkills();
	}

	public Dominate getGame(){
		return game;
	}

	public GamePlayer getGamePlayer(){
		return gPlayer;
	}

	public DominateKitType getKit(){
		return kit;
	}

	public void setKit(DominateKitType kit,boolean lobby){
		this.kit = kit;
		if(!lobby){
			this.setKitInventory();
			this.loadSkills();
		}
	}

	public long getLastInSpawn(){
		return lastInSpawn;
	}

	public void updateLastInSpawn(){
		lastInSpawn = System.currentTimeMillis();
	}

	public void updateBossBar(){
		ArrayList<DominatePoint> points = new ArrayList<DominatePoint>(game.getArena().getPoints());
		Collections.sort(points,new Comparator<DominatePoint>(){
			@Override
			public int compare(DominatePoint point1,DominatePoint point2){
				int compare = Integer.compare(point1.getPlayerRadarYaw(gPlayer),point2.getPlayerRadarYaw(gPlayer));
				if(compare > 0) return 1;
				else if(compare < 0) return -1;
				return 0;
			}
		});

		String letters = "";
		int [] indexes = new int[points.size()];
		for(int i=0;i<indexes.length;i++){
			int index = points.get(i).getPlayerRadarIndex(gPlayer);
			indexes[i] = index;
		}

		for(int i=0;i<indexes.length;i++){
			for(int a=0;a<indexes.length;a++){
				if(i != a && indexes[a] == indexes[i]){
					if(indexes[a]+1 < DominateUtils.RADAR_RANGE) indexes[a] ++;
				}
			}
		}
		for(int i=indexes.length-1;i>=0;i--){
			for(int a=indexes.length-1;a>=0;a--){
				if(i != a && indexes[a] == indexes[i]){
					if(indexes[a]-1 >= 0) indexes[a] --;
				}
			}
		}

		int [] columns = new int[DominateUtils.RADAR_RANGE];
		boolean found = false;
		for(int i=0;i<columns.length;i++){
			found = false;
			for(int a=0;a<indexes.length;a++){
				if(indexes[a] == i){
					found = true;
					letters += points.get(a).getNameColor(false)+points.get(a).getName().substring(0,1);
				}
			}
			if(!found) letters += " ";
		}
		String message = "§d§l[§r"+letters+"§d§l]";
		bossbar.setTitle(message);
	}

	public void showBossBar(){
		bossbar.showForPlayer(gPlayer);
	}

	public void hideBossBar(){
		bossbar.hideForPlayer(gPlayer);
	}

	public void runSpawn(){
		gPlayer.getPlayer().setFoodLevel(20);
		gPlayer.getPlayer().setSaturation(20);
		if(game.getTeams().getPlayerTeam(gPlayer).getType() == DominateTeamType.RED){
			if(game.getTeams().getTeam(DominateTeamType.RED).isLocationInSpawn(gPlayer.getPlayer().getLocation())){
				this.updateLastInSpawn();
			}
			else if(game.getTeams().getTeam(DominateTeamType.BLUE).isLocationInSpawn(gPlayer.getPlayer().getLocation())){
				gPlayer.getPlayer().damage(2.0);
			}
		}
		else if(game.getTeams().getPlayerTeam(gPlayer).getType() == DominateTeamType.BLUE){
			if(game.getTeams().getTeam(DominateTeamType.BLUE).isLocationInSpawn(gPlayer.getPlayer().getLocation())){
				this.updateLastInSpawn();
			}
			else if(game.getTeams().getTeam(DominateTeamType.RED).isLocationInSpawn(gPlayer.getPlayer().getLocation())){
				gPlayer.getPlayer().damage(2.0);
			}
		}
	}

	public void respawn(){
		gPlayer.getPlayer().setFlying(false);
		gPlayer.getPlayer().setAllowFlight(false);
		gPlayer.getPlayer().getInventory().setHeldItemSlot(0);
		gPlayer.getPlayer().setPlayerTime(game.getArena().getTime(),false);
		this.setKitInventory();
		this.showBossBar();
		this.loadSkills();
	}

	public void setKitInventory(){
		gPlayer.getPlayer().setAllowFlight(false);
		gPlayer.getPlayer().getInventory().clear();
		this.getKit().setPlayerArmor(gPlayer);
		this.getKit().setPlayerWeapons(gPlayer);
	}

	public void clear(){
		this.clearSkills();
		this.hideBossBar();
	}

	public DominateSkill getSkill(DominateSkillType type){
		return skills.get(type);
	}

	public void initSkills(){
		DominateSkill skill;
		skill = new DominateSkillArrowBlindness(game,this);
		skills.put(skill.getType(),skill);
		skill = new DominateSkillArrowExplosive(game,this);
		skills.put(skill.getType(),skill);
		skill = new DominateSkillArrowFire(game,this);
		skills.put(skill.getType(),skill);
		skill = new DominateSkillArrowGroup(game,this);
		skills.put(skill.getType(),skill);
		skill = new DominateSkillFire(game,this);
		skills.put(skill.getType(),skill);
		skill = new DominateSkillFrostWalk(game,this);
		skills.put(skill.getType(),skill);
		skill = new DominateSkillIceTrap(game,this);
		skills.put(skill.getType(),skill);
		skill = new DominateSkillJump(game,this);
		skills.put(skill.getType(),skill);
		skill = new DominateSkillRecall(game,this);
		skills.put(skill.getType(),skill);
		skill = new DominateSkillSmashDown(game,this);
		skills.put(skill.getType(),skill);
		skill = new DominateSkillWaterBottle(game,this);
		skills.put(skill.getType(),skill);
		skill = new DominateSkillWeb(game,this);
		skills.put(skill.getType(),skill);
		skill = new DominateSkillSoup(game,this);
		skills.put(skill.getType(),skill);
	}

	public void loadSkills(){
		for(DominateSkill skill : skills.values()){
			skill.setEnabled(false);
		}
		for(DominateSkill skill : skills.values()){
			if(this.getKit().getSkills().contains(skill.getType())) skill.setEnabled(true);
		}
	}

	public void clearSkills(){
		for(DominateSkill skill : skills.values()){
			skill.setEnabled(false);
		}
	}

	public class DominateBossBar extends GameBossBar {
		public DominateBossBar(Game game){
			super(game,GameBossBarType.GAME);
			this.setColor(BarColor.PINK);
			this.setStyle(BarStyle.SOLID);
		}

		public BlockParty getGame(){
			return (BlockParty) super.getGame();
		}

		public void update(){
		}

		public void showForPlayer(GamePlayer gPlayer){
			this.addPlayer(gPlayer);
		}

		public void hideForPlayer(GamePlayer gPlayer){
			this.removePlayer(gPlayer);
		}
	}
}