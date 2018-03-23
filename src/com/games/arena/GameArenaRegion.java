package com.games.arena;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.games.Games;
import com.games.events.GameRegionLoadEvent;
import com.games.utils.LocationUtil;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.world.registry.WorldData;

public class GameArenaRegion {

	private GameArena arena;
	private Schema schema;

	public GameArenaRegion(GameArena arena){
		this.arena = arena;

		File file = new File(Games.getInstance().getDataFolder()+"/"+arena.getGame().getType().getName()+"/"+arena.getName()+"/"+"region.schematic");
		if(file.exists()) schema = new Schema(file,LocationUtil.getConfigLocation(arena.getConfig(),"custom.locMin"));
	}

	public GameArena getArena(){
		return arena;
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
		WorldEditPlugin we;

		private Clipboard schema = null;
		private Location location = null;
		private com.sk89q.worldedit.world.World world = null;

		private boolean build = false;
		private HashMap<Vector,BaseBlock> blocks = new HashMap<>();
		private EditSession editSession = null;

		private static final int SLEEP_TIME = 20;

		public Schema(File file,Location location){
			we = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
			this.location = location;
			for(com.sk89q.worldedit.world.World lws : we.getWorldEdit().getServer().getWorlds()){
				if(lws.getName().equals(location.getWorld().getName())){
					this.world = lws;
					break;
				}
			}
			try {
				ClipboardFormat format = ClipboardFormat.SCHEMATIC;
				FileInputStream fis = new FileInputStream(file);
	            BufferedInputStream bis = new BufferedInputStream(fis);
	            ClipboardReader reader = format.getReader(bis);
	            WorldData worldData = world.getWorldData();
	            this.schema = reader.read(worldData);
	            fis.close();
	            bis.close();
			} catch (IOException e){
				e.printStackTrace();
			}
			editSession = we.getWorldEdit().getEditSessionFactory().getEditSession(world,-1);
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
							Bukkit.getServer().getPluginManager().callEvent(new GameRegionLoadEvent(arena.getGame(),arena));
						}
					});
				} catch (InterruptedException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e){
					e.printStackTrace();
				}
			}

			private void startStage(int stage) throws InterruptedException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
				int maxBlocksPerRun = 16 * 16 * schema.getDimensions().getBlockY();
				for(int x=schema.getRegion().getMinimumPoint().getBlockX();x<=schema.getRegion().getMaximumPoint().getBlockX();x++){
					for(int y=schema.getRegion().getMinimumPoint().getBlockY();y<=schema.getRegion().getMaximumPoint().getBlockY();y++){
						for(int z=schema.getRegion().getMinimumPoint().getBlockZ();z<=schema.getRegion().getMaximumPoint().getBlockZ();z++){
							BlockVector pt = new BlockVector(x,y,z);
							BaseBlock block = schema.getBlock(pt);
							boolean place = false;
							if(stage == 1 && !shouldPlaceLast(block.getType()) && !shouldPlaceFinal(block.getType())) place = true;
							else if(stage == 2 && shouldPlaceLast(block.getType())) place = true;
							else if(stage == 3 && shouldPlaceFinal(block.getType())) place = true;
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
					@SuppressWarnings("deprecation")
					@Override
					public Void call(){
						for(Entry<Vector,BaseBlock> map : blocks.entrySet()){
							location.getWorld().getBlockAt(map.getKey().getBlockX(),map.getKey().getBlockY(),map.getKey().getBlockZ()).setTypeIdAndData(map.getValue().getId(),(byte)map.getValue().getData(),false);
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
        shouldPlaceLast.add(Material.SAPLING);
        shouldPlaceLast.add(Material.BED);
        shouldPlaceLast.add(Material.POWERED_RAIL);
        shouldPlaceLast.add(Material.DETECTOR_RAIL);
        shouldPlaceLast.add(Material.LONG_GRASS);
        shouldPlaceLast.add(Material.DEAD_BUSH);
        shouldPlaceLast.add(Material.PISTON_EXTENSION);
        shouldPlaceLast.add(Material.YELLOW_FLOWER);
        shouldPlaceLast.add(Material.RED_ROSE);
        shouldPlaceLast.add(Material.BROWN_MUSHROOM);
        shouldPlaceLast.add(Material.RED_MUSHROOM);
        shouldPlaceLast.add(Material.TORCH);
        shouldPlaceLast.add(Material.FIRE);
        shouldPlaceLast.add(Material.REDSTONE_WIRE);
        shouldPlaceLast.add(Material.CROPS);
        shouldPlaceLast.add(Material.LADDER);
        shouldPlaceLast.add(Material.RAILS);
        shouldPlaceLast.add(Material.LEVER);
        shouldPlaceLast.add(Material.STONE_PLATE);
        shouldPlaceLast.add(Material.WOOD_PLATE);
        shouldPlaceLast.add(Material.REDSTONE_TORCH_OFF);
        shouldPlaceLast.add(Material.REDSTONE_TORCH_ON);
        shouldPlaceLast.add(Material.STONE_BUTTON);
        shouldPlaceLast.add(Material.SNOW);
        shouldPlaceLast.add(Material.PORTAL);
        shouldPlaceLast.add(Material.DIODE_BLOCK_OFF);
        shouldPlaceLast.add(Material.DIODE_BLOCK_ON);
        shouldPlaceLast.add(Material.TRAP_DOOR);
        shouldPlaceLast.add(Material.VINE);
        shouldPlaceLast.add(Material.WATER_LILY);
        shouldPlaceLast.add(Material.NETHER_STALK);
        shouldPlaceLast.add(Material.PISTON_BASE);
        shouldPlaceLast.add(Material.PISTON_STICKY_BASE);
        shouldPlaceLast.add(Material.PISTON_EXTENSION);
        shouldPlaceLast.add(Material.PISTON_MOVING_PIECE);
        shouldPlaceLast.add(Material.COCOA);
        shouldPlaceLast.add(Material.TRIPWIRE_HOOK);
        shouldPlaceLast.add(Material.TRIPWIRE);
        shouldPlaceLast.add(Material.FLOWER_POT);
        shouldPlaceLast.add(Material.CARROT);
        shouldPlaceLast.add(Material.POTATO);
        shouldPlaceLast.add(Material.WOOD_BUTTON);
        shouldPlaceLast.add(Material.ANVIL);
        shouldPlaceLast.add(Material.IRON_PLATE);
        shouldPlaceLast.add(Material.GOLD_PLATE);
        shouldPlaceLast.add(Material.REDSTONE_COMPARATOR_OFF);
        shouldPlaceLast.add(Material.REDSTONE_COMPARATOR_ON);
        shouldPlaceLast.add(Material.ACTIVATOR_RAIL);
        shouldPlaceLast.add(Material.IRON_TRAPDOOR);
        shouldPlaceLast.add(Material.CARPET);
        shouldPlaceLast.add(Material.DOUBLE_PLANT);
        shouldPlaceLast.add(Material.DAYLIGHT_DETECTOR_INVERTED);
    }

    private static final HashSet<Material> shouldPlaceFinal = new HashSet<Material>();
    static {
        shouldPlaceFinal.add(Material.SIGN_POST);
        shouldPlaceFinal.add(Material.WOODEN_DOOR);
        shouldPlaceFinal.add(Material.ACACIA_DOOR);
        shouldPlaceFinal.add(Material.BIRCH_DOOR);
        shouldPlaceFinal.add(Material.JUNGLE_DOOR);
        shouldPlaceFinal.add(Material.DARK_OAK_DOOR);
        shouldPlaceFinal.add(Material.SPRUCE_DOOR);
        shouldPlaceFinal.add(Material.WALL_SIGN);
        shouldPlaceFinal.add(Material.IRON_DOOR);
        shouldPlaceFinal.add(Material.CACTUS);
        shouldPlaceFinal.add(Material.SUGAR_CANE);
        shouldPlaceFinal.add(Material.CAKE_BLOCK);
        shouldPlaceFinal.add(Material.PISTON_EXTENSION);
        shouldPlaceFinal.add(Material.PISTON_MOVING_PIECE);
        shouldPlaceFinal.add(Material.STANDING_BANNER);
        shouldPlaceFinal.add(Material.WALL_BANNER);
    }

    //https://github.com/sk89q/WorldEdit/blob/master/worldedit-core/src/main/java/com/sk89q/worldedit/extent/reorder/MultiStageReorder.java

    @SuppressWarnings("deprecation")
	public static boolean shouldPlaceLast(int id){
    	Material material = Material.getMaterial(id);
		return shouldPlaceLast.contains(material);
    }

	@SuppressWarnings("deprecation")
	public static boolean shouldPlaceFinal(int id){
		Material material = Material.getMaterial(id);
		return shouldPlaceFinal.contains(material);
    }
}