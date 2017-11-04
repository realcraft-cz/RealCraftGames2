package com.games.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.games.Games;
import com.games.arena.GameArena;
import com.games.player.GamePlayer;
import com.games.utils.LocationUtil;
import com.games.utils.Particles;
import com.games.utils.RandomUtil;
import com.games.utils.StringUtil;
import com.games.utils.Title;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

public class GameVoting {

	private Game game;
	private GameVoteboard centerBoard;
	private GameVoteboard leftBoard;
	private GameVoteboard rightBoard;

	private HashMap<GamePlayer,GameVoteboard> votes = new HashMap<GamePlayer,GameVoteboard>();

	public GameVoting(Game game){
		this.game = game;
	}

	public Game getGame(){
		return game;
	}

	private Location[] getCenterLocations(){
		Location[] locations = new Location[3];
		locations[0] = LocationUtil.getConfigLocation(Games.getInstance().getConfig(),"voting.center.center");
		locations[1] = LocationUtil.getConfigLocation(Games.getInstance().getConfig(),"voting.center.minLoc");
		locations[2] = LocationUtil.getConfigLocation(Games.getInstance().getConfig(),"voting.center.maxLoc");
		return locations;
	}

	private Location[] getLeftLocations(){
		Location[] locations = new Location[3];
		locations[0] = LocationUtil.getConfigLocation(Games.getInstance().getConfig(),"voting.left.center");
		locations[1] = LocationUtil.getConfigLocation(Games.getInstance().getConfig(),"voting.left.minLoc");
		locations[2] = LocationUtil.getConfigLocation(Games.getInstance().getConfig(),"voting.left.maxLoc");
		return locations;
	}

	private Location[] getRightLocations(){
		Location[] locations = new Location[3];
		locations[0] = LocationUtil.getConfigLocation(Games.getInstance().getConfig(),"voting.right.center");
		locations[1] = LocationUtil.getConfigLocation(Games.getInstance().getConfig(),"voting.right.minLoc");
		locations[2] = LocationUtil.getConfigLocation(Games.getInstance().getConfig(),"voting.right.maxLoc");
		return locations;
	}

	public void clickVoting(GamePlayer gPlayer,Location location){
		if(leftBoard != null && leftBoard.isInLocation(location)){
			if(!this.isPlayerVoted(gPlayer,leftBoard)){
				this.votePlayer(gPlayer,leftBoard);
			}
		}
		else if(rightBoard != null && rightBoard.isInLocation(location)){
			if(!this.isPlayerVoted(gPlayer,rightBoard)){
				this.votePlayer(gPlayer,rightBoard);
			}
		}
	}

	private boolean isPlayerVoted(GamePlayer gPlayer,GameVoteboard board){
		return (votes.containsKey(gPlayer) && votes.get(gPlayer) == board);
	}

	private void votePlayer(GamePlayer gPlayer,GameVoteboard board){
		votes.put(gPlayer,board);
		gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1f,1f);
		Title.sendActionBar(gPlayer.getPlayer(),"§fHlasujes pro §6§l"+board.getArena().getName());
		board.runEffectForPlayer(gPlayer);
		if(leftBoard != null) leftBoard.updateVotes();
		if(rightBoard != null) rightBoard.updateVotes();
	}

	public void removePlayer(GamePlayer gPlayer){
		votes.remove(gPlayer);
		if(leftBoard != null) leftBoard.updateVotes();
		if(rightBoard != null) rightBoard.updateVotes();
	}

	public void runEffects(){
		if(leftBoard != null && rightBoard != null){
			for(GamePlayer gPlayer : game.getPlayers()){
				if(this.isPlayerVoted(gPlayer,leftBoard)){
					leftBoard.runEffectForPlayer(gPlayer);
					Title.sendActionBar(gPlayer.getPlayer(),"§fHlasujes pro §6§l"+leftBoard.getArena().getName());
				}
				else if(this.isPlayerVoted(gPlayer,rightBoard)){
					rightBoard.runEffectForPlayer(gPlayer);
					Title.sendActionBar(gPlayer.getPlayer(),"§fHlasujes pro §6§l"+rightBoard.getArena().getName());
				}
				else Title.sendActionBar(gPlayer.getPlayer(),"§fHlasuj kliknutim na mapu");
			}
		}
	}

	public void resetVoting(){
		votes = new HashMap<GamePlayer,GameVoteboard>();
		Location[] locations;
		if(game.getArenas().size() > 1){
			if(leftBoard == null){
				locations = this.getLeftLocations();
				leftBoard = new GameVoteboard(locations[0],locations[1],locations[2]);
			}
			if(rightBoard == null){
				locations = this.getRightLocations();
				rightBoard = new GameVoteboard(locations[0],locations[1],locations[2]);
			}
			leftBoard.setArena(null);
			rightBoard.setArena(null);
			leftBoard.setArena(this.getRandomArena());
			rightBoard.setArena(this.getRandomArena());
		} else {
			if(centerBoard == null){
				locations = this.getCenterLocations();
				centerBoard = new GameVoteboard(locations[0],locations[1],locations[2]);
			}
			centerBoard.setArena(game.getArenas().get(0),false);
		}
	}

	public GameArena getWinningArena(){
		if(leftBoard != null && rightBoard != null){
			int leftVotes = leftBoard.getVotes();
			int rightVotes = rightBoard.getVotes();
			if(leftVotes > rightVotes) return leftBoard.getArena();
			else if(rightVotes > leftVotes) return rightBoard.getArena();
			else return (RandomUtil.getRandomBoolean() ? leftBoard.getArena() : rightBoard.getArena());
		}
		else if(centerBoard != null) return centerBoard.getArena();
		return null;
	}

	private GameArena getRandomArena(){
		return this.getRandomArena(1);
	}

	private GameArena getRandomArena(int step){
		GameArena arena = game.getArenas().get(RandomUtil.getRandomInteger(0,game.getArenas().size()-1));
		if((leftBoard.getArena() == arena || rightBoard.getArena() == arena || game.getArena() == arena) && step < 100) arena = this.getRandomArena(step+1);
		if(step >= 100) Games.DEBUG("getRandomArena step >= 100");
		return arena;
	}

	private class GameVoteboard {

		private GameArena arena;

		private Location center;
		private Location location1;
		private Location location2;

		private ArrayList<Location> effectLocations = new ArrayList<Location>();

		private Hologram hologram;

		public GameVoteboard(Location center,Location location1,Location location2){
			this.center = center;
			this.location1 = location1;
			this.location2 = location2;
			this.hologram = HologramsAPI.createHologram(Games.getInstance(),this.center.clone().add(0.0,1.0,0.0));
			this.hologram.insertTextLine(0,"§l0 hlasu");
			this.hologram.getVisibilityManager().setVisibleByDefault(false);

			double minZ = Math.min(location1.getBlockZ(),location2.getBlockZ());
			double maxZ = Math.max(location1.getBlockZ(),location2.getBlockZ())+1;
			double minY = Math.min(location1.getBlockY(),location2.getBlockY());
			double maxY = Math.max(location1.getBlockY(),location2.getBlockY())+1;
			for(double z=minZ;z<=maxZ+0.01;z+=0.2){
				for(double y=minY;y<=maxY+0.01;y+=0.2){
					if(Math.abs(y-minY) < 0.000001 || Math.abs(y-maxY) < 0.000001 || Math.abs(z-minZ) < 0.000001 || Math.abs(z-maxZ) < 0.000001){
						effectLocations.add(new Location(this.center.getWorld(),location1.getX()+(1/8f),y,z));
					}
				}
			}
			Collections.shuffle(effectLocations);
		}

		public GameArena getArena(){
			return arena;
		}

		public void setArena(GameArena arena){
			this.setArena(arena,true);
		}

		public void setArena(GameArena arena,boolean hologram){
			this.arena = arena;
			this.drawArena();
			if(game.getArenas().size() > 1) this.hologram.getVisibilityManager().setVisibleByDefault(true);
			this.updateVotes();
		}

		public void updateVotes(){
			int votes = this.getVotes();
			hologram.removeLine(0);
			hologram.insertTextLine(0,"§l"+votes+" "+StringUtil.inflect(votes,new String[]{"hlas","hlasy","hlasu"}));
		}

		public int getVotes(){
			int votes = 0;
			for(GameVoteboard board : GameVoting.this.votes.values()){
				if(board == this) votes ++;
			}
			return votes;
		}

		public boolean isInLocation(Location location){
			return location.toVector().isInAABB(Vector.getMinimum(location1.toVector(),location2.toVector()),Vector.getMaximum(location1.toVector(),location2.toVector()));
		}

		public void runEffectForPlayer(GamePlayer gPlayer){
			for(int i=0;i<50;i++){
				final int index = i;
				Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
					public void run(){
						Particles.SPELL_WITCH.display(0.0f,0.0f,0.0f,0.0f,1,effectLocations.get(index),gPlayer.getPlayer());
					}
				},Math.round(i/(50/20f)));
			}
		}

		private void drawArena(){
			if(arena != null){
				ItemStack[] images = arena.getImage().getImages();
				if(images == null) return;
				int index = 0;
				boolean xDiff = (location1.getBlockX() < location2.getBlockX());
				boolean zDiff = (location1.getBlockZ() < location2.getBlockZ());
				int y = (location1.getBlockY() > location2.getBlockY() ? location1.getBlockY() : location2.getBlockY());
				while(y >= location1.getBlockZ()){
					int x = location1.getBlockX();
					while(xDiff ? x <= location2.getBlockX() : x >= location2.getBlockX()){
						int z = location1.getBlockZ();
						while(zDiff ? z <= location2.getBlockZ() : z >= location2.getBlockZ()){
							Location location = new Location(location1.getWorld(),x,y,z);
							ItemFrame frame = this.getItemFrameAt(location);
							if(frame != null){
								if(index < images.length){
									frame.setItem(images[index]);
								}
							}
							index ++;
							z += (zDiff ? 1 : -1);
						}
						x += (xDiff ? 1 : -1);
					}
					y --;
				}
			}
		}

		private ItemFrame getItemFrameAt(Location location){
			Entity entities[] = location.getChunk().getEntities();
			for(Entity entity : entities){
				if(entity instanceof ItemFrame && LocationUtil.isSimilar(location,entity.getLocation())) return (ItemFrame) entity;
			}
			return null;
		}
	}
}