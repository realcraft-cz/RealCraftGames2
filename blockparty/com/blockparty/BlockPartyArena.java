package com.blockparty;

import com.games.arena.GameArena;
import com.games.arena.data.GameArenaData;
import com.games.game.GameType;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import realcraft.bukkit.database.DB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockPartyArena extends GameArena {

	private World world;
	private ArrayList<BlockPartyFloor> floors = new ArrayList<>();

	private BlockPartyFloor currentFloor;

	public BlockPartyArena(BlockParty game,int id){
		super(game,id);
	}

	public BlockParty getGame(){
		return (BlockParty) super.getGame();
	}

	public World getWorld(){
		if(world == null) world = Bukkit.getWorld("world");
		return world;
	}

	public void initWorld(){
		if(this.getWorld() == null){
			WorldCreator creator = new WorldCreator("world");
			creator.type(WorldType.FLAT);
			creator.environment(World.Environment.NORMAL);
			creator.generator(new CustomGenerator(Biome.THE_VOID));
			World world = Bukkit.getServer().createWorld(creator);
			this.setWorld(world);
		}
		this.getWorld().setDifficulty(Difficulty.HARD);
		this.getWorld().setPVP(true);
		this.getWorld().setAutoSave(true);
		this.getWorld().setFullTime(23000);
		this.getWorld().setGameRule(GameRule.DO_DAYLIGHT_CYCLE,false);
		this.getWorld().setGameRule(GameRule.DO_WEATHER_CYCLE,false);
		this.getWorld().setGameRule(GameRule.DO_MOB_SPAWNING,false);
		this.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS,false);
	}

	public Location getSpectator(){
		return this.getGame().getSpectatorLocation();
	}

	public ArrayList<BlockPartyFloor> getFloors(){
		return floors;
	}

	public BlockPartyFloor getCurrentFloor(){
		return currentFloor;
	}

	public void setCurrentFloor(BlockPartyFloor floor){
		this.currentFloor = floor;
	}

	@Override
	public void resetRegion(){
		currentFloor.reset();
		this.getGame().setLoaded(true);
	}

	@Override
	public void loadData(GameArenaData data){
	}

	@Override
	public void load(){
		ResultSet rs = DB.query("SELECT map_id FROM "+BlockParty.MAPS+" WHERE map_type = '"+GameType.BLOCKPARTY.getId()+"' AND map_state = '1'");
		try {
			while(rs.next()){
				int id = rs.getInt("map_id");
				floors.add(new BlockPartyFloor(this,id));
			}
			rs.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
		currentFloor = floors.get(0);
		/*File file = new File(Games.getInstance().getDataFolder()+"/"+GameType.BLOCKPARTY.getName()+"/"+"image.png");
		if(file.exists()){
			try {
				byte[] bytes = Files.readAllBytes(file.toPath());
				this.getImage().load(bytes);
			} catch (IOException e){
				e.printStackTrace();
			}
		}*/
	}

	public boolean isBlockInArena(Location location){
		BlockVector3 vec = BlockVector3.at(location.getBlockX(),location.getBlockY(),location.getBlockZ());
		return vec.containedWithin(this.getGame().getMinLoc(),this.getGame().getMaxLoc());
	}

	public class CustomGenerator extends ChunkGenerator {

		private Biome biome;

		public CustomGenerator(Biome biome){
			this.biome = biome;
		}

		@Override
		public boolean canSpawn(World world,int x,int z){
			return true;
		}

		@Override
		public ChunkData generateChunkData(World world, Random random, int cx, int cz, BiomeGrid biomeGrid){
			ChunkData data = this.createChunkData(world);
			if(cx == 0 && cz == 0) data.setBlock(0,64,0,Material.BEDROCK);
			return data;
		}

		@Override
		public @Nullable BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
			return new BiomeProvider() {
				@Override
				public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int i, int i1, int i2) {
					return biome;
				}

				@Override
				public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
					return new ArrayList<>(List.of(biome));
				}
			};
		}

		@Override
		public Location getFixedSpawnLocation(World world,Random random){
			return new Location(world,0,66,0);
		}
	}
}