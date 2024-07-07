package com.games.arena;

import com.games.events.GameRegionLoadEvent;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import realcraft.bukkit.RealCraft;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

public class GameArenaRegion {

	private static final int X_MARGIN = 256;
	private static final int Z_OFFSET = 1000;

	private GameArena arena;
	private byte[] regionBytes;
	private Set<BlockVector2> regionChunks;
	private BlockVector3 regionMinPoint;
	private BlockVector3 regionMaxPoint;

	private Location baseLoc;
	private Location centerLoc;
	private Location minLoc;
	private Location maxLoc;

	public GameArenaRegion(GameArena arena){
		this.arena = arena;
	}

	public GameArena getArena(){
		return arena;
	}

	private int getXOffset(){
		return (arena.getId()*arena.getGame().getType().getDimension().getX())+(arena.getId()*X_MARGIN);
	}

	private int getZOffset(){
		return arena.getGame().getType().getId()*Z_OFFSET;
	}

	public World getWorld(){
		return arena.getWorld();
	}

	public Location getBaseLocation(){
		if(baseLoc == null) baseLoc = new Location(this.getWorld(),this.getXOffset(),0,this.getZOffset());
		return baseLoc;
	}

	public Location getCenterLocation(){
		if(centerLoc == null) centerLoc = this.getBaseLocation().clone().add(arena.getGame().getType().getDimension().getX()/2f,arena.getGame().getType().getDimension().getY()/2f,arena.getGame().getType().getDimension().getZ()/2f);
		return centerLoc;
	}

	public Location getMinLocation(){
		if(minLoc == null) minLoc = this.getBaseLocation().clone();
		return minLoc;
	}

	public Location getMaxLocation(){
		if(maxLoc == null) maxLoc = this.getBaseLocation().clone().add(arena.getGame().getType().getDimension().getX()-1,arena.getGame().getType().getDimension().getY()-1,arena.getGame().getType().getDimension().getZ()-1);
		return maxLoc;
	}

	public boolean isLocationInside(Location location){
		return (location.getBlockX() >= this.getMinLocation().getBlockX() && location.getBlockX() <= this.getMaxLocation().getBlockX()
				&& location.getBlockY() >= this.getMinLocation().getBlockY() && location.getBlockY() <= this.getMaxLocation().getBlockY()
				&& location.getBlockZ() >= this.getMinLocation().getBlockZ() && location.getBlockZ() <= this.getMaxLocation().getBlockZ());
	}

	public boolean isLocationInside(com.sk89q.worldedit.util.Location location){
		return (location.getBlockX() >= this.getMinLocation().getBlockX() && location.getBlockX() <= this.getMaxLocation().getBlockX()
				&& location.getBlockY() >= this.getMinLocation().getBlockY() && location.getBlockY() <= this.getMaxLocation().getBlockY()
				&& location.getBlockZ() >= this.getMinLocation().getBlockZ() && location.getBlockZ() <= this.getMaxLocation().getBlockZ());
	}

	public boolean isLocationInside(Vector vector){
		return (vector.getBlockX() >= this.getMinLocation().getBlockX() && vector.getBlockX() <= this.getMaxLocation().getBlockX()
				&& vector.getBlockY() >= this.getMinLocation().getBlockY() && vector.getBlockY() <= this.getMaxLocation().getBlockY()
				&& vector.getBlockZ() >= this.getMinLocation().getBlockZ() && vector.getBlockZ() <= this.getMaxLocation().getBlockZ());
	}

	public boolean isLocationInside(BlockVector2 vector){
		return (vector.x() >= this.getMinLocation().getBlockX() && vector.x() <= this.getMaxLocation().getBlockX()
				&& vector.z() >= this.getMinLocation().getBlockZ() && vector.z() <= this.getMaxLocation().getBlockZ());
	}

	public boolean isLocationInsideClipboard(Location location){
		return (location.getBlockX() >= regionMinPoint.x() && location.getBlockX() <= regionMaxPoint.x()
				&& location.getBlockY() >= regionMinPoint.y() && location.getBlockY() <= regionMaxPoint.y()
				&& location.getBlockZ() >= regionMinPoint.z() && location.getBlockZ() <= regionMaxPoint.z());
	}

	public void createWorld() {
		if (this.getWorld() != null) {
			return;
		}

		WorldCreator creator = new WorldCreator("world_" + arena.getId());
		creator.type(WorldType.FLAT);
		creator.environment(arena.getEnvironment());
		creator.generator(new CustomGenerator(arena.getBiome()));

		World world = Bukkit.getServer().createWorld(creator);
		if (world == null) {
			throw new RuntimeException("World world_" + arena.getId() + " failed to create");
		}

		world.setKeepSpawnInMemory(false);
		world.setDifficulty(Difficulty.HARD);
		world.setPVP(true);
		world.setAutoSave(true);
		world.setFullTime(arena.getTime());
		world.setMonsterSpawnLimit(0);
		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE,false);
		world.setGameRule(GameRule.DO_WEATHER_CYCLE,false);
		world.setGameRule(GameRule.DO_MOB_SPAWNING,false);
		world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS,false);

		arena.setWorld(world);
	}

	protected void _removeWorld() {
		if (this.getWorld() == null) {
			return;
		}

		if (!Bukkit.getServer().unloadWorld(this.getWorld(), false)) {
			throw new RuntimeException("World world_" + arena.getId() + " failed to unload");
		}

		arena.setWorld(null);
	}

	public void reset(){
		this.reset(true);
	}

	public void reset(boolean async){
		new SchemaStages(this._getClipboard(),this.getBaseLocation());
	}

	public void load(byte[] bytes){
		regionBytes = bytes;
	}

	protected Clipboard _getClipboard() {
		try {
			ClipboardFormat format = ClipboardFormats.findByInputStream(() -> new ByteArrayInputStream(regionBytes));
			if (format == null) {
				throw new RuntimeException("Unsupported schematic format for map_id " + this.getArena().getId());
			}
			ClipboardReader reader = format.getReader(new ByteArrayInputStream(regionBytes));
			Clipboard clipboard = reader.read();

			this._initClipboardPoints(clipboard);
			this._initClipboardChunks(clipboard);

			return clipboard;
		} catch (IOException e){
			e.printStackTrace();
		}

		return null;
	}

	protected void _initClipboardPoints(Clipboard clipboard) {
		regionMinPoint = clipboard.getRegion().getMinimumPoint();
		regionMaxPoint = clipboard.getRegion().getMaximumPoint();
	}

	protected void _initClipboardChunks(Clipboard clipboard) {
		regionChunks = clipboard.getRegion().getChunks();
	}

	public boolean isEntityValidToClear(Entity entity) {
		return !(entity instanceof Player);
	}

	public void clearEntities(){
		if (regionChunks == null) {
			return;
		}

		for(BlockVector2 coords : regionChunks){
			coords = coords.add(-(regionMinPoint.x() >> 4),-(regionMinPoint.z() >> 4));
			coords = coords.add((this.getBaseLocation().getBlockX() >> 4),(this.getBaseLocation().getBlockZ() >> 4));
			coords = coords.add(BlockVector2.at((regionMinPoint.x() >> 4) - (this.getBaseLocation().getBlockX() >> 4),(regionMinPoint.z() >> 4) - (this.getBaseLocation().getBlockZ() >> 4)));
			Chunk chunk = this.getBaseLocation().getWorld().getChunkAt(coords.x(),coords.z());
			if(!chunk.isLoaded()) chunk.load();
			for(org.bukkit.entity.Entity entity : chunk.getEntities()){
				if (this.isEntityValidToClear(entity)) {
					entity.remove();
				}
			}
		}
	}

	public class SchemaStages extends Thread {

		private static final int SLEEP_TIME = 20;

		private Clipboard clipboard;
		private Location location;
		private EditSession editSession;

		private boolean build = false;
		private HashMap<BlockVector3,BaseBlock> blocks = new HashMap<>();

		public SchemaStages(Clipboard clipboard,Location location){
			this.clipboard = clipboard;
			this.location = location;
			this.editSession = WorldEdit.getInstance().newEditSession(new BukkitWorld(location.getWorld()));
			this.start();
		}

		@Override
		public void run(){
			try {
				startStage(1);
				startStage(2);
				startStage(3);
				Bukkit.getScheduler().runTask(RealCraft.getInstance(),new Runnable(){
					@Override
					public void run(){
						editSession.close();
						for(Chunk chunk : location.getWorld().getLoadedChunks()) chunk.unload();
						arena.setLoaded(true);
						Bukkit.getServer().getPluginManager().callEvent(new GameRegionLoadEvent(GameArenaRegion.this.getArena().getGame(),GameArenaRegion.this.getArena()));
					}
				});
			} catch (InterruptedException | SecurityException | IllegalArgumentException e){
				editSession.close();
				e.printStackTrace();
			}
		}

		private void startStage(int stage) throws InterruptedException, SecurityException, IllegalArgumentException {
			int maxBlocksPerRun = 16 * 16 * clipboard.getDimensions().y();
			for(int x=clipboard.getRegion().getMinimumPoint().x();x<=clipboard.getRegion().getMaximumPoint().x();x++){
				for(int y=clipboard.getRegion().getMinimumPoint().y();y<=clipboard.getRegion().getMaximumPoint().y();y++){
					for(int z=clipboard.getRegion().getMinimumPoint().z();z<=clipboard.getRegion().getMaximumPoint().z();z++){
						BlockVector3 pt = BlockVector3.at(x,y,z);
						BaseBlock block = clipboard.getFullBlock(pt);
						boolean place = false;
						if(stage == 1 && !shouldPlaceLast(BukkitAdapter.adapt(block.getBlockType())) && !shouldPlaceFinal(BukkitAdapter.adapt(block.getBlockType()))) place = true;
						else if(stage == 2 && shouldPlaceLast(BukkitAdapter.adapt(block.getBlockType()))) place = true;
						else if(stage == 3 && shouldPlaceFinal(BukkitAdapter.adapt(block.getBlockType()))) place = true;
						if(place){
							BlockVector3 pos = pt.add(-clipboard.getRegion().getMinimumPoint().x(),-clipboard.getRegion().getMinimumPoint().y(),-clipboard.getRegion().getMinimumPoint().z());
							pos = pos.add(location.getBlockX(),location.getBlockY(),location.getBlockZ());
							pos = pos.add(clipboard.getRegion().getMinimumPoint().subtract(BlockVector3.at(location.getBlockX(),location.getBlockY(),location.getBlockZ())));
							blocks.put(pos,block);
							if(blocks.size() >= maxBlocksPerRun){
								nextPaste();
								while(build){
									sleep(SLEEP_TIME);
								}
							}
						}
					}
				}
			}
			nextPaste();
			while(build){
				sleep(SLEEP_TIME);
			}
		}

		private void nextPaste(){
			build = true;
			Bukkit.getScheduler().callSyncMethod(RealCraft.getInstance(),new Callable<Void>(){
				@Override
				public Void call(){
					for(java.util.Map.Entry<BlockVector3,BaseBlock> map : blocks.entrySet()){
						if(!map.getValue().hasNbtData()){
							location.getWorld().getBlockAt(map.getKey().x(),map.getKey().y(),map.getKey().z()).setType(BukkitAdapter.adapt(map.getValue().getBlockType()),false);
							location.getWorld().getBlockAt(map.getKey().x(),map.getKey().y(),map.getKey().z()).setBlockData(BukkitAdapter.adapt(map.getValue()),false);
						} else {
							editSession.smartSetBlock(map.getKey(),map.getValue());
						}
					}
					Operations.completeBlindly(editSession.commit());
					blocks.clear();
					build = false;
					return null;
				}
			});
		}
	}

	private static final Set<Material> shouldPlaceLast = new HashSet<Material>();
	static {
		shouldPlaceLast.add(Material.ACACIA_SAPLING);
		shouldPlaceLast.add(Material.BIRCH_SAPLING);
		shouldPlaceLast.add(Material.DARK_OAK_SAPLING);
		shouldPlaceLast.add(Material.JUNGLE_SAPLING);
		shouldPlaceLast.add(Material.OAK_SAPLING);
		shouldPlaceLast.add(Material.SPRUCE_SAPLING);
		shouldPlaceLast.add(Material.BLACK_BED);
		shouldPlaceLast.add(Material.BLUE_BED);
		shouldPlaceLast.add(Material.BROWN_BED);
		shouldPlaceLast.add(Material.CYAN_BED);
		shouldPlaceLast.add(Material.GRAY_BED);
		shouldPlaceLast.add(Material.GREEN_BED);
		shouldPlaceLast.add(Material.LIGHT_BLUE_BED);
		shouldPlaceLast.add(Material.LIGHT_GRAY_BED);
		shouldPlaceLast.add(Material.LIME_BED);
		shouldPlaceLast.add(Material.MAGENTA_BED);
		shouldPlaceLast.add(Material.ORANGE_BED);
		shouldPlaceLast.add(Material.PINK_BED);
		shouldPlaceLast.add(Material.PURPLE_BED);
		shouldPlaceLast.add(Material.RED_BED);
		shouldPlaceLast.add(Material.WHITE_BED);
		shouldPlaceLast.add(Material.YELLOW_BED);
		shouldPlaceLast.add(Material.SHORT_GRASS);
		shouldPlaceLast.add(Material.TALL_GRASS);
		shouldPlaceLast.add(Material.DEAD_BUSH);
		shouldPlaceLast.add(Material.SUNFLOWER);
		shouldPlaceLast.add(Material.BROWN_MUSHROOM);
		shouldPlaceLast.add(Material.RED_MUSHROOM);
		shouldPlaceLast.add(Material.TORCH);
		shouldPlaceLast.add(Material.FIRE);
		shouldPlaceLast.add(Material.REDSTONE_WIRE);
		shouldPlaceLast.add(Material.COMPARATOR);
		shouldPlaceLast.add(Material.WHEAT);
		shouldPlaceLast.add(Material.LADDER);
		shouldPlaceLast.add(Material.RAIL);
		shouldPlaceLast.add(Material.ACTIVATOR_RAIL);
		shouldPlaceLast.add(Material.DETECTOR_RAIL);
		shouldPlaceLast.add(Material.POWERED_RAIL);
		shouldPlaceLast.add(Material.LEVER);
		shouldPlaceLast.add(Material.ACACIA_PRESSURE_PLATE);
		shouldPlaceLast.add(Material.BIRCH_PRESSURE_PLATE);
		shouldPlaceLast.add(Material.DARK_OAK_PRESSURE_PLATE);
		shouldPlaceLast.add(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
		shouldPlaceLast.add(Material.JUNGLE_PRESSURE_PLATE);
		shouldPlaceLast.add(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
		shouldPlaceLast.add(Material.OAK_PRESSURE_PLATE);
		shouldPlaceLast.add(Material.SPRUCE_PRESSURE_PLATE);
		shouldPlaceLast.add(Material.STONE_PRESSURE_PLATE);
		shouldPlaceLast.add(Material.REDSTONE_TORCH);
		shouldPlaceLast.add(Material.REDSTONE_WALL_TORCH);
		shouldPlaceLast.add(Material.SNOW);
		shouldPlaceLast.add(Material.END_PORTAL);
		shouldPlaceLast.add(Material.NETHER_PORTAL);
		shouldPlaceLast.add(Material.REPEATER);
		shouldPlaceLast.add(Material.ACACIA_TRAPDOOR);
		shouldPlaceLast.add(Material.BIRCH_TRAPDOOR);
		shouldPlaceLast.add(Material.DARK_OAK_TRAPDOOR);
		shouldPlaceLast.add(Material.IRON_TRAPDOOR);
		shouldPlaceLast.add(Material.JUNGLE_TRAPDOOR);
		shouldPlaceLast.add(Material.OAK_TRAPDOOR);
		shouldPlaceLast.add(Material.SPRUCE_TRAPDOOR);
		shouldPlaceLast.add(Material.VINE);
		shouldPlaceLast.add(Material.LILY_PAD);
		shouldPlaceLast.add(Material.NETHER_WART);
		shouldPlaceLast.add(Material.PISTON);
		shouldPlaceLast.add(Material.PISTON_HEAD);
		shouldPlaceLast.add(Material.MOVING_PISTON);
		shouldPlaceLast.add(Material.STICKY_PISTON);
		shouldPlaceLast.add(Material.COCOA);
		shouldPlaceLast.add(Material.TRIPWIRE_HOOK);
		shouldPlaceLast.add(Material.TRIPWIRE);
		shouldPlaceLast.add(Material.FLOWER_POT);
		shouldPlaceLast.add(Material.CARROT);
		shouldPlaceLast.add(Material.POTATO);
		shouldPlaceLast.add(Material.ACACIA_BUTTON);
		shouldPlaceLast.add(Material.BIRCH_BUTTON);
		shouldPlaceLast.add(Material.DARK_OAK_BUTTON);
		shouldPlaceLast.add(Material.JUNGLE_BUTTON);
		shouldPlaceLast.add(Material.OAK_BUTTON);
		shouldPlaceLast.add(Material.SPRUCE_BUTTON);
		shouldPlaceLast.add(Material.STONE_BUTTON);
		shouldPlaceLast.add(Material.ANVIL);
		shouldPlaceLast.add(Material.BLACK_CARPET);
		shouldPlaceLast.add(Material.BLUE_CARPET);
		shouldPlaceLast.add(Material.BROWN_CARPET);
		shouldPlaceLast.add(Material.CYAN_CARPET);
		shouldPlaceLast.add(Material.GRAY_CARPET);
		shouldPlaceLast.add(Material.GREEN_CARPET);
		shouldPlaceLast.add(Material.LIGHT_BLUE_CARPET);
		shouldPlaceLast.add(Material.LIGHT_GRAY_CARPET);
		shouldPlaceLast.add(Material.LIME_CARPET);
		shouldPlaceLast.add(Material.MAGENTA_CARPET);
		shouldPlaceLast.add(Material.ORANGE_CARPET);
		shouldPlaceLast.add(Material.PINK_CARPET);
		shouldPlaceLast.add(Material.PURPLE_CARPET);
		shouldPlaceLast.add(Material.RED_CARPET);
		shouldPlaceLast.add(Material.WHITE_CARPET);
		shouldPlaceLast.add(Material.YELLOW_CARPET);
		shouldPlaceLast.add(Material.CHORUS_PLANT);
		shouldPlaceLast.add(Material.KELP_PLANT);
	}

	private static final HashSet<Material> shouldPlaceFinal = new HashSet<Material>();
	static {
		shouldPlaceFinal.add(Material.ACACIA_SIGN);
		shouldPlaceFinal.add(Material.BIRCH_SIGN);
		shouldPlaceFinal.add(Material.DARK_OAK_SIGN);
		shouldPlaceFinal.add(Material.JUNGLE_SIGN);
		shouldPlaceFinal.add(Material.OAK_SIGN);
		shouldPlaceFinal.add(Material.SPRUCE_SIGN);
		shouldPlaceFinal.add(Material.ACACIA_WALL_SIGN);
		shouldPlaceFinal.add(Material.BIRCH_WALL_SIGN);
		shouldPlaceFinal.add(Material.DARK_OAK_WALL_SIGN);
		shouldPlaceFinal.add(Material.JUNGLE_WALL_SIGN);
		shouldPlaceFinal.add(Material.OAK_WALL_SIGN);
		shouldPlaceFinal.add(Material.SPRUCE_WALL_SIGN);
		shouldPlaceFinal.add(Material.PISTON_HEAD);
		shouldPlaceFinal.add(Material.MOVING_PISTON);
		shouldPlaceFinal.add(Material.ACACIA_DOOR);
		shouldPlaceFinal.add(Material.BIRCH_DOOR);
		shouldPlaceFinal.add(Material.DARK_OAK_DOOR);
		shouldPlaceFinal.add(Material.IRON_DOOR);
		shouldPlaceFinal.add(Material.JUNGLE_DOOR);
		shouldPlaceFinal.add(Material.OAK_DOOR);
		shouldPlaceFinal.add(Material.SPRUCE_DOOR);
		shouldPlaceFinal.add(Material.CACTUS);
		shouldPlaceFinal.add(Material.SUGAR_CANE);
		shouldPlaceFinal.add(Material.CAKE);
		shouldPlaceFinal.add(Material.WHITE_BANNER);
		shouldPlaceFinal.add(Material.ORANGE_BANNER);
		shouldPlaceFinal.add(Material.MAGENTA_BANNER);
		shouldPlaceFinal.add(Material.LIGHT_BLUE_BANNER);
		shouldPlaceFinal.add(Material.YELLOW_BANNER);
		shouldPlaceFinal.add(Material.LIME_BANNER);
		shouldPlaceFinal.add(Material.PINK_BANNER);
		shouldPlaceFinal.add(Material.GRAY_BANNER);
		shouldPlaceFinal.add(Material.LIGHT_GRAY_BANNER);
		shouldPlaceFinal.add(Material.CYAN_BANNER);
		shouldPlaceFinal.add(Material.PURPLE_BANNER);
		shouldPlaceFinal.add(Material.BLUE_BANNER);
		shouldPlaceFinal.add(Material.BROWN_BANNER);
		shouldPlaceFinal.add(Material.GREEN_BANNER);
		shouldPlaceFinal.add(Material.RED_BANNER);
		shouldPlaceFinal.add(Material.BLACK_BANNER);
		shouldPlaceFinal.add(Material.WHITE_WALL_BANNER);
		shouldPlaceFinal.add(Material.ORANGE_WALL_BANNER);
		shouldPlaceFinal.add(Material.MAGENTA_WALL_BANNER);
		shouldPlaceFinal.add(Material.LIGHT_BLUE_WALL_BANNER);
		shouldPlaceFinal.add(Material.YELLOW_WALL_BANNER);
		shouldPlaceFinal.add(Material.LIME_WALL_BANNER);
		shouldPlaceFinal.add(Material.PINK_WALL_BANNER);
		shouldPlaceFinal.add(Material.GRAY_WALL_BANNER);
		shouldPlaceFinal.add(Material.LIGHT_GRAY_WALL_BANNER);
		shouldPlaceFinal.add(Material.CYAN_WALL_BANNER);
		shouldPlaceFinal.add(Material.PURPLE_WALL_BANNER);
		shouldPlaceFinal.add(Material.BLUE_WALL_BANNER);
		shouldPlaceFinal.add(Material.BROWN_WALL_BANNER);
		shouldPlaceFinal.add(Material.GREEN_WALL_BANNER);
		shouldPlaceFinal.add(Material.RED_WALL_BANNER);
		shouldPlaceFinal.add(Material.BLACK_WALL_BANNER);
		shouldPlaceFinal.add(Material.ITEM_FRAME);
	}

	public static boolean shouldPlaceLast(Material type){
		return shouldPlaceLast.contains(type);
	}

	public static boolean shouldPlaceFinal(Material type){
		return shouldPlaceFinal.contains(type);
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
		public ChunkData generateChunkData(World world,Random random,int cx,int cz,BiomeGrid biomeGrid){
			ChunkData data = this.createChunkData(world);

			if(cx == 0 && cz == 0) {
				data.setBlock(0, 64, 0, Material.BEDROCK);
			}

			/*for (int x = 0;x < 16;x++) {
				for (int y = world.getMinHeight();y < world.getMaxHeight();y++) {
					for (int z = 0;z < 16;z++) {

						BlockVector3 pt = BlockVector3.at((cx << 4) + x, y, (cz << 4) + z);
						if (!clipboard.getRegion().contains(pt)) {
							continue;
						}

						data.setBlock(x, y, z, BukkitAdapter.adapt(clipboard.getFullBlock(pt)));
					}
				}
			}*/

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