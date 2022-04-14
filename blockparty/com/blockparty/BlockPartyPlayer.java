package com.blockparty;

import com.games.player.GamePlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import realcraft.bukkit.lobby.LobbyMenu;

public class BlockPartyPlayer extends GamePlayer {

	public BlockPartyPlayer(Player player, BlockParty game){
		super(player, game);
	}

	@Override
	public void toggleSpectator() {
		player.getInventory().clear();
		player.setGameMode(GameMode.SURVIVAL);
		player.setAllowFlight(false);
		player.setFlying(false);
		player.getInventory().setItem(0, LobbyMenu.getItem());
	}
}
