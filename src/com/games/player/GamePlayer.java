package com.games.player;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import com.games.Games;
import com.games.arena.GameArena;
import com.games.game.Game;
import com.games.utils.BorderUtil;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.realcraft.RealCraft;
import com.realcraft.ServerType;

public class GamePlayer {

	private Player player;
	private Game game;
	private GameArena arena;
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

	public GameArena getArena(){
		return arena;
	}

	public void setArena(GameArena arena){
		this.arena = arena;
	}

	public GamePlayerSettings getSettings(){
		return settings;
	}

	public GamePlayerState getState(){
		return state;
	}

	public void setState(GamePlayerState state){
		this.state = state;
	}

	public void teleportToLobby(){
		player.teleport(game.getLobbyLocation());
	}

	public void teleportToSpectatorLocation(){
		player.teleport(arena.getSpectatorLocation());
	}

	public void toggleSpectator(){
		this.updateSpectatorInventory();
		player.setAllowFlight(true);
		player.setFlying(true);
		player.setGameMode(GameMode.SPECTATOR);
		Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				player.setAllowFlight(true);
				player.setFlying(true);
				player.setGameMode(GameMode.SPECTATOR);
				BorderUtil.setBorder(player,arena.getSpectatorLocation(),arena.getConfig().getInt("spectator.radius")*2);
			}
		},2);
	}

	public void updateSpectatorInventory(){
		player.getInventory().clear();
		ItemStack itemStack;
		ItemMeta meta;

		itemStack = new ItemStack(Material.SLIME_BALL,1);
		meta = itemStack.getItemMeta();
		meta.setDisplayName("§c§lOpustit hru");
		itemStack.setItemMeta(meta);
		player.getInventory().setItem(8,itemStack);

		player.setFlySpeed(0.2f);
	}

	public void resetPlayer(){
		this.setState(GamePlayerState.DEFAULT);
		player.setGameMode(GameMode.SURVIVAL);
		player.setAllowFlight(false);
		player.setFlying(false);
		player.setHealth(20);
		player.setFoodLevel(20);
		player.setSaturation(20);
		player.setWalkSpeed(0.2f);
		player.setFlySpeed(0.1f);
		player.setLevel(0);
		player.setExp(0);
		player.setTotalExperience(0);
		player.resetPlayerTime();
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