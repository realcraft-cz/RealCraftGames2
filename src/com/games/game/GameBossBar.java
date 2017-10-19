package com.games.game;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import com.games.player.GamePlayer;

public abstract class GameBossBar {

	private Game game;
	private GameBossBarType type;

	private String title;
	private BarColor color = BarColor.BLUE;
	private BarStyle style = BarStyle.SOLID;
	private float progress = 1.0f;
	private BossBar bossbar;

	public GameBossBar(Game game,GameBossBarType type){
		this.game = game;
		this.type = type;
		this.bossbar = Bukkit.createBossBar("",color,style);
	}

	public Game getGame(){
		return game;
	}

	public GameBossBarType getType(){
		return type;
	}

	public BossBar getBossBar(){
		return bossbar;
	}

	public String getTitle(){
		return title;
	}

	public void setTitle(String title){
		this.title = title;
		bossbar.setTitle(title);
	}

	public float getProgress(){
		return progress;
	}

	public void setProgress(float progress){
		this.progress = progress;
		bossbar.setProgress(progress);
	}

	public BarColor getColor(){
		return color;
	}

	public void setColor(BarColor color){
		this.color = color;
		bossbar.setColor(color);
	}

	public BarStyle getStyle(){
		return style;
	}

	public void setStyle(BarStyle style){
		this.style = style;
		bossbar.setStyle(style);
	}

	public void addPlayer(GamePlayer gPlayer){
		if(!bossbar.getPlayers().contains(gPlayer.getPlayer())) bossbar.addPlayer(gPlayer.getPlayer());
	}

	public void removePlayer(GamePlayer gPlayer){
		bossbar.removePlayer(gPlayer.getPlayer());
	}

	public enum GameBossBarType {
		LOBBY, GAME;

		public static GameBossBarType getByName(String name){
			return GameBossBarType.valueOf(name.toUpperCase());
		}

		public String toString(){
			return this.name().toLowerCase();
		}
	}
}