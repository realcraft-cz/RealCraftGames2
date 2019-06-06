package com.dominate;

import com.dominate.DominateTeam.DominateTeamType;
import com.games.player.GamePlayer;
import com.games.utils.FireworkUtil;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import realcraft.bukkit.utils.MaterialUtil;
import realcraft.bukkit.utils.Particles;

import java.util.Random;

public class DominatePoint {

	private Dominate game;
	private DominateArena arena;
	private DominateTeamType team = DominateTeamType.NONE;
	private String name;
	private Location location;
	private Location[] corners = null;
	private Location[] glasses = null;
	private Vector[] bounds = null;

	private int progress = 0;
	private boolean halfsecond = false;

	private Random random = new Random();

	public DominatePoint(Dominate game,DominateArena arena,String name,Location location){
		this.game = game;
		this.arena = arena;
		this.name = name;
		this.location = location;
		this.updateBlocks();
	}

	public Dominate getGame(){
		return game;
	}

	public DominateArena getArena(){
		return arena;
	}

	public DominateTeamType getTeam(){
		return team;
	}

	public String getName(){
		return name;
	}

	public Location getLocation(){
		return location;
	}

	public int getProgress(){
		return progress;
	}

	public Location[] getCorners(){
		if(corners == null){
			corners = new Location[4];
			corners[0] = this.getLocation().clone().add(3,5,3);
			corners[1] = this.getLocation().clone().add(3,5,-3);
			corners[2] = this.getLocation().clone().add(-3,5,3);
			corners[3] = this.getLocation().clone().add(-3,5,-3);
		}
		return corners;
	}

	public Location[] getGlasses(){
		if(glasses == null){
			glasses = new Location[25];
			for(int x=0;x<5;x++){
				for(int y=0;y<5;y++){
					glasses[(x == 2 && y == 2 ? 24 : this.getFreeGlassesIndex())] = this.getLocation().clone().add(-2,1,-2).add(x,0,y);
				}
			}
		}
		return glasses;
	}

	private int getFreeGlassesIndex(){
		int index = random.nextInt(glasses.length);
		if(glasses[index] != null || index == 24) return this.getFreeGlassesIndex();
		return index;
	}

	public Vector[] getBounds(){
		if(bounds == null){
			bounds = new Vector[2];
			bounds[0] = this.getLocation().clone().add(-3,1,-3).toVector();
			bounds[1] = this.getLocation().clone().add(3,5,3).toVector();
		}
		return bounds;
	}

	public int getPlayerRadarYaw(GamePlayer gPlayer){
		return DominateUtils.yawToLocation(gPlayer.getPlayer().getLocation(),this.getLocation());
	}

	public int getPlayerRadarIndex(GamePlayer gPlayer){
		return DominateUtils.yawToRadarIndex(this.getPlayerRadarYaw(gPlayer));
	}

	public boolean isPlayerInside(Player player){
		return player.getLocation().toVector().isInAABB(this.getBounds()[0],this.getBounds()[1]);
	}

	public DominateTeamType getTeamInside(){
		DominateTeamType type = null;
		int red = this.getTeamAmountInside(DominateTeamType.RED);
		int blue = this.getTeamAmountInside(DominateTeamType.BLUE);
		if(red > 0 && blue == 0) type = DominateTeamType.RED;
		else if(blue > 0 && red == 0) type = DominateTeamType.BLUE;
		else if(red == 0 && blue == 0) type = DominateTeamType.NONE;
		return type;
	}

	public int getTeamAmountInside(DominateTeamType type){
		int amount = 0;
		for(GamePlayer gPlayer : game.getTeams().getTeam(type).getPlayers()){
			if(!gPlayer.getPlayer().isDead() && this.isPlayerInside(gPlayer.getPlayer())){
				amount ++;
			}
		}
		return amount;
	}

	public DominateTeamType getCapturingTeam(){
		if(progress > 0) return DominateTeamType.RED;
		else if(progress < 0) return DominateTeamType.BLUE;
		return DominateTeamType.NONE;
	}

	public String getNameColor(){
		return this.getNameColor(true);
	}

	public String getNameColor(boolean bold){
		DominateTeamType type = this.getTeamInside();
		if(type != null && type != DominateTeamType.NONE){
			if(progress == 25 || progress == -25) return ""+this.getTeam().getScoreboardColor();
			else if(halfsecond) return ""+type.getScoreboardColor()+(bold ? ChatColor.BOLD : "");
			else if(this.getTeam() != DominateTeamType.NONE) return ""+this.getTeam().getScoreboardColor();
			else return ""+DominateTeamType.NONE.getScoreboardColor();
		}
		return ""+team.getScoreboardColor();
	}

	public void updateBlocks(){
		if(this.getLocation().getBlock().getType() != Material.BEACON) this.getLocation().getBlock().setType(Material.BEACON);
		if(progress == 25 || progress == -25){
			for(Location location : this.getCorners()){
				location.getBlock().setType(MaterialUtil.getWool(this.getTeam().getDyeColor()));
			}
			for(Location location : this.getGlasses()){
				location.getBlock().setType(MaterialUtil.getStainedGlass(this.getTeam().getDyeColor()));
				if(location.getBlock().getRelative(BlockFace.DOWN).getType() != Material.BEACON){
					location.getBlock().getRelative(BlockFace.DOWN).setType(MaterialUtil.getWool(this.getTeam().getDyeColor()));
				}
			}
		} else {
			for(Location location : this.getCorners()){
				location.getBlock().setType(MaterialUtil.getWool(team.getDyeColor()));
			}
			for(int i=0;i<this.getGlasses().length;i++){
				if(i+1 <= Math.abs(progress)){
					this.getGlasses()[i].getBlock().setType(MaterialUtil.getStainedGlass(this.getCapturingTeam().getDyeColor()));
					if(this.getGlasses()[i].getBlock().getRelative(BlockFace.DOWN).getType() != Material.BEACON){
						this.getGlasses()[i].getBlock().getRelative(BlockFace.DOWN).setType(MaterialUtil.getWool(this.getCapturingTeam().getDyeColor()));
					}
				} else {
					this.getGlasses()[i].getBlock().setType(MaterialUtil.getStainedGlass(DominateTeamType.NONE.getDyeColor()));
					if(this.getGlasses()[i].getBlock().getRelative(BlockFace.DOWN).getType() != Material.BEACON){
						this.getGlasses()[i].getBlock().getRelative(BlockFace.DOWN).setType(MaterialUtil.getWool(DominateTeamType.NONE.getDyeColor()));
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void runEffect(DominateTeamType team){
		for(Location location : this.getCorners()){
			Particles.BLOCK_CRACK.display(Bukkit.createBlockData(MaterialUtil.getWool(team.getDyeColor())),0.3f,0.2f,0.3f,0.0f,64,location.clone().add(0,0.2,0),64);
		}
	}

	private void runFirework(){
		FireworkUtil.spawnFirework(this.getLocation().clone().add(0,2,0),FireworkEffect.Type.BALL_LARGE,team.getColor(),false,false);
	}

	public void run(){
		halfsecond = !halfsecond;
		DominateTeamType type = this.getTeamInside();
		if(type == null) return;
		if(type != DominateTeamType.NONE && ((type == DominateTeamType.RED && progress < 25) || (type == DominateTeamType.BLUE && progress > -25))){
			int amount = this.getTeamAmountInside(type);
			progress += type.getProgressValue()*amount;
			if(progress >= 25){
				progress = 25;
				team = DominateTeamType.RED;
				game.getTeams().getTeam(team).capturePoint(this);
				this.runFirework();
			}
			else if(progress <= -25){
				progress = -25;
				team = DominateTeamType.BLUE;
				game.getTeams().getTeam(team).capturePoint(this);
				this.runFirework();
			}
			else if(progress >= 0 && team == DominateTeamType.BLUE){
				team = DominateTeamType.NONE;
			}
			else if(progress <= 0 && team == DominateTeamType.RED){
				team = DominateTeamType.NONE;
			}
			this.updateBlocks();
			this.runEffect(type);
			for(Location location : this.getCorners()) location.getWorld().playSound(this.getLocation().clone().add(0,2,0),Sound.BLOCK_STONE_BREAK,1f,1f);
		}
		else if(type == DominateTeamType.NONE){
			if(team == DominateTeamType.NONE && progress != 25 && progress != -25 && progress != 0){
				if(progress != 0) progress = (progress > 0 ? progress-1 : progress+1);
				if(progress == 0) team = DominateTeamType.NONE;
				this.updateBlocks();
				this.runEffect(DominateTeamType.NONE);
				for(Location location : this.getCorners()) location.getWorld().playSound(this.getLocation().clone().add(0,2,0),Sound.BLOCK_WOOL_BREAK,0.5f,1f);
			}
			else if(team != DominateTeamType.NONE && progress != 25 && progress != -25){
				if(progress != 0) progress = (progress > 0 ? progress+1 : progress-1);
				this.updateBlocks();
				this.runEffect(type);
				for(Location location : this.getCorners()) location.getWorld().playSound(this.getLocation().clone().add(0,2,0),Sound.BLOCK_WOOL_BREAK,0.5f,1f);
			}
		}
		if(team != DominateTeamType.NONE){
			game.getTeams().getTeam(team).addPoint();
		}
	}

	public void reset(){
		team = DominateTeamType.NONE;
		progress = 0;
		this.updateBlocks();
	}
}