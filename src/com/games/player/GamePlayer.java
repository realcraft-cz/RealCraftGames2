package com.games.player;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.games.Games;
import com.games.events.GamePlayerStateChangeEvent;
import com.games.game.Game;
import com.games.game.GameFlag;
import com.games.utils.BorderUtil;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import realcraft.bukkit.RealCraft;
import realcraft.bukkit.ServerType;

public class GamePlayer {

	private Player player;
	private Game game;
	private GamePlayerState state;
	private GamePlayerSettings settings;

	public GamePlayer(Player player,Game game){
		this.player = player;
		this.game = game;
		this.settings = new GamePlayerSettings();
		this.state = GamePlayerState.DEFAULT;
	}

	public Player getPlayer(){
		return player;
	}

	public void setPlayer(Player player){
		this.player = player;
	}

	public Game getGame(){
		return game;
	}

	public GamePlayerSettings getSettings(){
		return settings;
	}

	public GamePlayerState getState(){
		return state;
	}

	public void setState(GamePlayerState state){
		GamePlayerState oldState = this.state;
		this.state = state;
		Bukkit.getServer().getPluginManager().callEvent(new GamePlayerStateChangeEvent(game,this,oldState));
	}

	public void teleportToLobby(){
		player.teleport(game.getLobbyLocation());
	}

	public void teleportToSpectatorLocation(){
		player.teleport(game.getArena().getSpectatorLocation());
	}

	/*public void toggleSpectator(){
		player.getInventory().clear();
		player.setAllowFlight(true);
		player.setFlying(true);
		player.setGameMode(GameMode.SPECTATOR);
		player.setFlySpeed(0.2f);
		Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				player.setAllowFlight(true);
				player.setFlying(true);
				player.setGameMode(GameMode.SPECTATOR);
				BorderUtil.setBorder(player,game.getArena().getSpectatorLocation(),game.getArena().getSpectatorRadius()*2);
			}
		},2);
	}*/

	public void toggleSpectator(){
		player.getInventory().clear();
		player.setGameMode(GameMode.ADVENTURE);
		player.setAllowFlight(true);
		player.setFlying(true);
		player.setFlySpeed(0.2f);
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,Integer.MAX_VALUE,Integer.MAX_VALUE,false,false));
		player.setCollidable(false);
		if(GameFlag.SPECTATOR) player.getInventory().setItem(0,game.getSpectator().getItem());
		for(GamePlayer gPlayer2 : game.getGamePlayers()){
			gPlayer2.getPlayer().hidePlayer(player);
		}
		for(GamePlayer gPlayer2 : game.getPlayers()){
			if(gPlayer2.getState() == GamePlayerState.SPECTATOR) player.showPlayer(gPlayer2.getPlayer());
		}
		Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				player.setGameMode(GameMode.ADVENTURE);
				player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,Integer.MAX_VALUE,Integer.MAX_VALUE,false,false));
				BorderUtil.setBorder(player,game.getArena().getSpectatorLocation(),game.getArena().getSpectatorRadius()*2);
			}
		},2);
	}

	public void resetPlayer(){
		this.setState(GamePlayerState.DEFAULT);
		player.setGameMode(GameMode.SURVIVAL);
		player.setAllowFlight(false);
		player.setFlying(false);
		player.setCollidable(true);
		player.setHealth(20);
		player.setFoodLevel(20);
		player.setSaturation(20);
		player.setWalkSpeed(0.2f);
		player.setFlySpeed(0.1f);
		player.setLevel(0);
		player.setExp(0);
		player.setTotalExperience(0);
		player.resetPlayerTime();
		player.closeInventory();
		player.getInventory().clear();
		player.getInventory().setHeldItemSlot(0);
		player.getInventory().setHelmet(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);
		player.getInventory().setBoots(null);
		for(PotionEffect effect : player.getActivePotionEffects()){
			player.removePotionEffect(effect.getType());
		}
		for(GamePlayer player2 : game.getPlayers()){
			player2.getPlayer().showPlayer(player);
		}
	}

	public void connectToServer(ServerType server){
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(server.toString());
		player.sendPluginMessage(RealCraft.getInstance(),"BungeeCord",out.toByteArray());
	}
}