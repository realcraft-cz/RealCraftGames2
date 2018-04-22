package com.paintball.specials;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import com.games.player.GamePlayer;
import com.games.player.GamePlayerState;
import com.paintball.Paintball;

public class PaintballSpecialJump extends PaintballSpecial {

	public static final int JUMP_TIMEOUT = 500;

	private double force;
	private Location minLocation;
	private Location maxLocation;

	public PaintballSpecialJump(Paintball game,double force,Location minLocation,Location maxLocation){
		super(PaintballSpecialType.JUMP,game);
		this.force = force;
		this.minLocation = minLocation;
		this.maxLocation = maxLocation;
	}

	@Override
	public void clear(){
	}

	@EventHandler
	public void PlayerMoveEvent(PlayerMoveEvent event){
		if(event.getPlayer().getVelocity().getY() > 0.0001){
			GamePlayer gPlayer = this.getGame().getGamePlayer(event.getPlayer());
			if(gPlayer.getState() != GamePlayerState.SPECTATOR && this.getGame().getUser(gPlayer).getLastJump()+PaintballSpecialJump.JUMP_TIMEOUT < System.currentTimeMillis()){
				if(this.isPlayerAtJump(gPlayer)){
					gPlayer.getPlayer().setVelocity(gPlayer.getPlayer().getVelocity().setY(force));
					this.getGame().getUser(gPlayer).updateLastJump();
				}
			}
		}
	}

	private boolean isPlayerAtJump(GamePlayer gPlayer){
		Location location = gPlayer.getPlayer().getLocation();
		if(location.getBlockX() >= minLocation.getBlockX() && location.getBlockX() <= maxLocation.getBlockX()
		&& location.getBlockY() >= minLocation.getBlockY() && location.getBlockY() <= maxLocation.getBlockY()
		&& location.getBlockZ() >= minLocation.getBlockZ() && location.getBlockZ() <= maxLocation.getBlockZ()) return true;
		return false;
	}
}