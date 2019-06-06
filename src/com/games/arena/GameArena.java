package com.games.arena;

import com.games.arena.data.GameArenaData;
import com.games.arena.data.GameArenaDataInteger;
import com.games.arena.data.GameArenaDataLocation;
import com.games.arena.data.GameArenaDataString;
import com.games.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import realcraft.bukkit.database.DB;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

import static realcraft.bukkit.mapmanager.MapManager.MAPS;

public abstract class GameArena {

	private int id;
	private Game game;
	private String name;
	private World world;

	private GameArenaImage image = new GameArenaImage(this);
	private GameArenaRegion region = new GameArenaRegion(this);

	private GameArenaDataInteger time = new GameArenaDataInteger(this,"time");
	private GameArenaDataBiome biome = new GameArenaDataBiome(this,"biome");
	private GameArenaDataEnvironment environment = new GameArenaDataEnvironment(this,"environment");
	private GameArenaDataLocation spectator = new GameArenaDataLocation(this,"spectator");

	private boolean loaded = false;

	public GameArena(Game game,int id){
		this.game = game;
		this.id = id;
	}

	public Game getGame(){
		return game;
	}

	public int getId(){
		return id;
	}

	public String getName(){
		return name;
	}

	public World getWorld(){
		if(world == null) world = Bukkit.getWorld("world_"+this.getId());
		return world;
	}

	public void setWorld(World world){
		this.world = world;
	}

	public GameArenaImage getImage(){
		return image;
	}

	public GameArenaRegion getRegion(){
		return region;
	}

	public int getTime(){
		return time.getValue();
	}

	public Biome getBiome(){
		return biome.getBiome();
	}

	public Environment getEnvironment(){
		return environment.getEnvironment();
	}

	public Location getSpectator(){
		return spectator.getLocation();
	}

	public boolean isLoaded(){
		return loaded;
	}

	public void setLoaded(boolean loaded){
		this.loaded = loaded;
	}

	public void load(){
		ResultSet rs = DB.query("SELECT * FROM "+MAPS+" WHERE map_id = '"+this.getId()+"'");
		try {
			if(rs.next()){
				name = rs.getString("map_name");
				this._loadData(new GameArenaData(this,rs.getString("map_data")));
				this.loadRegion(rs.getBlob("map_region"));
				this.loadImage(rs.getBlob("map_image"));
			}
			rs.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}

	private void _loadData(GameArenaData data){
		time.loadData(data);
		biome.loadData(data);
		environment.loadData(data);
		this.getRegion().initWorld();
		spectator.loadData(data);
		this.loadData(data);
	}

	private void loadRegion(Blob blob) throws SQLException {
		if(blob != null){
			byte[] bytes = blob.getBytes(1,(int)blob.length());
			blob.free();
			this.getRegion().load(bytes);
		} else {
			this.setLoaded(true);
		}
	}

	private void loadImage(Blob blob) throws SQLException {
		if(blob != null){
			byte[] bytes = blob.getBytes(1,(int)blob.length());
			blob.free();
			this.getImage().load(bytes);
		} else {
			this.getImage().setDefaultImage();
		}
	}

	public abstract void loadData(GameArenaData data);
	public abstract void resetRegion();

	public class GameArenaDataBiome extends GameArenaDataString {

		private Biome biome;

		public GameArenaDataBiome(GameArena arena,String name){
			super(arena,name);
		}

		public Biome getBiome(){
			return biome;
		}

		@Override
		public void loadData(GameArenaData data){
			super.loadData(data);
			this.biome = Biome.valueOf(this.getValue());
		}
	}

	public class GameArenaDataEnvironment extends GameArenaDataString {

		private Environment environment;

		public GameArenaDataEnvironment(GameArena arena,String name){
			super(arena,name);
		}

		public Environment getEnvironment(){
			return environment;
		}

		@Override
		public void loadData(GameArenaData data){
			super.loadData(data);
			this.environment = Environment.valueOf(this.getValue());
		}
	}
}