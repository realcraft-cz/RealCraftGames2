package com.games.arena;

import com.games.Games;
import com.games.events.GameRegionLoadEvent;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import realcraft.bukkit.utils.LocationUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

public class GameArenaRegion {

	private GameArena arena;
	private Schema schema;

	public GameArenaRegion(GameArena arena){
		this.arena = arena;

		File file = new File(Games.getInstance().getDataFolder()+"/"+arena.getGame().getType().getName()+"/"+arena.getName()+"/"+"region.schem");
		if(file.exists()) schema = new Schema(file,LocationUtil.getConfigLocation(arena.getConfig(),"custom.locMin"));
	}

	public GameArena getArena(){
		return arena;
	}

	public boolean isAvailable(){
		return (schema != null);
	}

	public void reset(){
		if(schema != null){
			schema.clearEntities();
			schema.pasteBlocks();
			schema.pasteEntities();
		}
	}

	public void clearEntites(){
		if(schema != null) schema.clearEntities();
	}

	public class Schema {
		WorldEdit WE;

		private Clipboard schema = null;
		private Location location;

		private boolean build = false;
		private HashMap<Vector,BaseBlock> blocks = new HashMap<>();
		private EditSession editSession = null;

		private static final int SLEEP_TIME = 20;

		public Schema(File file,Location location){
			WE = WorldEdit.getInstance();
			this.location = location;
			try {
				BuiltInClipboardFormat format = BuiltInClipboardFormat.SPONGE_SCHEMATIC;
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);
				ClipboardReader reader = format.getReader(bis);
				this.schema = reader.read();
				fis.close();
				bis.close();
			} catch (IOException e){
				e.printStackTrace();
			}
			editSession = WE.getEditSessionFactory().getEditSession(new BukkitWorld(location.getWorld()),-1);
			editSession.disableQueue();
		}

		public void pasteBlocks(){
			new SchemaStages();
		}

		public void pasteEntities(){
			for(Entity entity : schema.getEntities()){
				Vector pos = entity.getLocation().toVector().add(-schema.getRegion().getMinimumPoint().getBlockX(),-schema.getRegion().getMinimumPoint().getBlockY(),-schema.getRegion().getMinimumPoint().getBlockZ());
				pos = pos.add(location.getBlockX(),location.getBlockY(),location.getBlockZ());
				com.sk89q.worldedit.util.Location location = new com.sk89q.worldedit.util.Location(entity.getLocation().getExtent(),pos);
				editSession.createEntity(location,entity.getState());
			}
		}

		public void clearEntities(){
			for(Vector2D coords : schema.getRegion().getChunks()){
				coords = coords.add(-(schema.getRegion().getMinimumPoint().getBlockX() >> 4),-(schema.getRegion().getMinimumPoint().getBlockZ() >> 4));
				coords = coords.add((location.getBlockX() >> 4),(location.getBlockZ() >> 4));
				Chunk chunk = location.getWorld().getChunkAt(coords.getBlockX(),coords.getBlockZ());
				if(!chunk.isLoaded()) chunk.load();
				for(org.bukkit.entity.Entity entity : chunk.getEntities()){
					if(!(entity instanceof Player)){
						entity.remove();
					}
				}
			}
		}

		public class SchemaStages extends Thread {
			public SchemaStages(){
				this.start();
			}

			@Override
			public void run(){
				try {
					startStage(1);
					startStage(2);
					startStage(3);
					Bukkit.getScheduler().runTask(Games.getInstance(),new Runnable(){
						@Override
						public void run(){
							for(Chunk chunk : location.getWorld().getLoadedChunks()) chunk.unload();
							arena.setLoaded(true);
							Bukkit.getServer().getPluginManager().callEvent(new GameRegionLoadEvent(arena.getGame(),arena));
						}
					});
				} catch (InterruptedException | SecurityException | IllegalArgumentException e){
					e.printStackTrace();
				}
			}

			private void startStage(int stage) throws InterruptedException, SecurityException, IllegalArgumentException {
				int maxBlocksPerRun = 16 * 16 * schema.getDimensions().getBlockY();
				for(int x=schema.getRegion().getMinimumPoint().getBlockX();x<=schema.getRegion().getMaximumPoint().getBlockX();x++){
					for(int y=schema.getRegion().getMinimumPoint().getBlockY();y<=schema.getRegion().getMaximumPoint().getBlockY();y++){
						for(int z=schema.getRegion().getMinimumPoint().getBlockZ();z<=schema.getRegion().getMaximumPoint().getBlockZ();z++){
							BlockVector pt = new BlockVector(x,y,z);
							BaseBlock block = schema.getFullBlock(pt);
							boolean place = false;
							if(stage == 1 && !shouldPlaceLast(BukkitAdapter.adapt(block.getBlockType())) && !shouldPlaceFinal(BukkitAdapter.adapt(block.getBlockType()))) place = true;
							else if(stage == 2 && shouldPlaceLast(BukkitAdapter.adapt(block.getBlockType()))) place = true;
							else if(stage == 3 && shouldPlaceFinal(BukkitAdapter.adapt(block.getBlockType()))) place = true;
							if(place){
								Vector pos = pt.add(-schema.getRegion().getMinimumPoint().getBlockX(),-schema.getRegion().getMinimumPoint().getBlockY(),-schema.getRegion().getMinimumPoint().getBlockZ());
								pos = pos.add(location.getBlockX(),location.getBlockY(),location.getBlockZ());
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
				Bukkit.getScheduler().callSyncMethod(Games.getInstance(),new Callable<Void>(){
					@Override
					public Void call(){
						for(Entry<Vector,BaseBlock> map : blocks.entrySet()){
							if(!map.getValue().hasNbtData()){
								location.getWorld().getBlockAt(map.getKey().getBlockX(),map.getKey().getBlockY(),map.getKey().getBlockZ()).setType(BukkitAdapter.adapt(map.getValue().getBlockType()),false);
								location.getWorld().getBlockAt(map.getKey().getBlockX(),map.getKey().getBlockY(),map.getKey().getBlockZ()).setBlockData(BukkitAdapter.adapt(map.getValue()),false);
							} else {
								try {
									editSession.setBlock(map.getKey(),map.getValue());
								} catch (MaxChangedBlocksException e){
									e.printStackTrace();
								}
							}
						}
						blocks.clear();
						build = false;
						return null;
					}
				});
			}
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
		shouldPlaceLast.add(Material.GRASS);
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
		shouldPlaceFinal.add(Material.SIGN);
		shouldPlaceFinal.add(Material.WALL_SIGN);
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
	}

	public static boolean shouldPlaceLast(Material type){
		return shouldPlaceLast.contains(type);
	}

	public static boolean shouldPlaceFinal(Material type){
		return shouldPlaceFinal.contains(type);
	}
}