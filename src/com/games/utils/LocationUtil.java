package com.games.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class LocationUtil {

	public static final Set<Material> HOLLOW_MATERIALS = new HashSet<>();
	public static final int RADIUS = 3;
	public static final Vector3D[] VOLUME;

	static {
		HOLLOW_MATERIALS.add(Material.AIR);
		HOLLOW_MATERIALS.add(Material.SAPLING);
		HOLLOW_MATERIALS.add(Material.POWERED_RAIL);
		HOLLOW_MATERIALS.add(Material.DETECTOR_RAIL);
		HOLLOW_MATERIALS.add(Material.LONG_GRASS);
		HOLLOW_MATERIALS.add(Material.DEAD_BUSH);
		HOLLOW_MATERIALS.add(Material.YELLOW_FLOWER);
		HOLLOW_MATERIALS.add(Material.RED_ROSE);
		HOLLOW_MATERIALS.add(Material.BROWN_MUSHROOM);
		HOLLOW_MATERIALS.add(Material.RED_MUSHROOM);
		HOLLOW_MATERIALS.add(Material.TORCH);
		HOLLOW_MATERIALS.add(Material.FIRE);
		HOLLOW_MATERIALS.add(Material.REDSTONE_WIRE);
		HOLLOW_MATERIALS.add(Material.CROPS);
		HOLLOW_MATERIALS.add(Material.LADDER);
		HOLLOW_MATERIALS.add(Material.RAILS);
		HOLLOW_MATERIALS.add(Material.LEVER);
		HOLLOW_MATERIALS.add(Material.REDSTONE_TORCH_OFF);
		HOLLOW_MATERIALS.add(Material.REDSTONE_TORCH_ON);
		HOLLOW_MATERIALS.add(Material.STONE_BUTTON);
		HOLLOW_MATERIALS.add(Material.SNOW);
		HOLLOW_MATERIALS.add(Material.SUGAR_CANE_BLOCK);
		HOLLOW_MATERIALS.add(Material.PORTAL);
		HOLLOW_MATERIALS.add(Material.DIODE_BLOCK_OFF);
		HOLLOW_MATERIALS.add(Material.DIODE_BLOCK_ON);
		HOLLOW_MATERIALS.add(Material.PUMPKIN_STEM);
		HOLLOW_MATERIALS.add(Material.MELON_STEM);
		HOLLOW_MATERIALS.add(Material.VINE);
		HOLLOW_MATERIALS.add(Material.WATER_LILY);
		HOLLOW_MATERIALS.add(Material.NETHER_WARTS);
		HOLLOW_MATERIALS.add(Material.ENDER_PORTAL);
		HOLLOW_MATERIALS.add(Material.COCOA);
		HOLLOW_MATERIALS.add(Material.TRIPWIRE_HOOK);
		HOLLOW_MATERIALS.add(Material.TRIPWIRE);
		HOLLOW_MATERIALS.add(Material.FLOWER_POT);
		HOLLOW_MATERIALS.add(Material.CARROT);
		HOLLOW_MATERIALS.add(Material.POTATO);
		HOLLOW_MATERIALS.add(Material.WOOD_BUTTON);
		HOLLOW_MATERIALS.add(Material.SKULL);
		HOLLOW_MATERIALS.add(Material.REDSTONE_COMPARATOR_OFF);
		HOLLOW_MATERIALS.add(Material.REDSTONE_COMPARATOR_ON);
		HOLLOW_MATERIALS.add(Material.ACTIVATOR_RAIL);
		HOLLOW_MATERIALS.add(Material.CARPET);
		HOLLOW_MATERIALS.add(Material.DOUBLE_PLANT);
		HOLLOW_MATERIALS.add(Material.SEEDS);
		HOLLOW_MATERIALS.add(Material.SIGN_POST);
		HOLLOW_MATERIALS.add(Material.WOODEN_DOOR);
		HOLLOW_MATERIALS.add(Material.WALL_SIGN);
		HOLLOW_MATERIALS.add(Material.STONE_PLATE);
		HOLLOW_MATERIALS.add(Material.IRON_DOOR_BLOCK);
		HOLLOW_MATERIALS.add(Material.WOOD_PLATE);
		HOLLOW_MATERIALS.add(Material.FENCE_GATE);
	}

	public static class Vector3D {
		public int x;
		public int y;
		public int z;

		public Vector3D(int x,int y,int z){
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	static {
		List<Vector3D> pos = new ArrayList<Vector3D>();
		for (int x = -RADIUS; x <= RADIUS; x++) {
			for (int y = -RADIUS; y <= RADIUS; y++) {
				for (int z = -RADIUS; z <= RADIUS; z++) {
					pos.add(new Vector3D(x, y, z));
				}
			}
		}
		Collections.sort(pos, new Comparator<Vector3D>() {
			@Override
			public int compare(Vector3D a, Vector3D b) {
				return (a.x * a.x + a.y * a.y + a.z * a.z) - (b.x * b.x + b.y * b.y + b.z * b.z);
			}
		});
		VOLUME = pos.toArray(new Vector3D[0]);
	}

	public static Location getConfigLocation(FileConfiguration config,String path){
		double x = (config.getDouble(path+".x"));
		double y = (config.getDouble(path+".y"));
		double z = (config.getDouble(path+".z"));
		float yaw = (float)(config.getDouble(path+".yaw",0));
		float pitch = (float)(config.getDouble(path+".pitch",0));
		World world = Bukkit.getServer().getWorld(config.getString(path+".world"));
		return new Location(world,x,y,z,yaw,pitch);
	}

	public static Location getConfigLocation(ConfigurationSection section,String path){
		double x = (section.getDouble(path+".x"));
		double y = (section.getDouble(path+".y"));
		double z = (section.getDouble(path+".z"));
		float yaw = (float)(section.getDouble(path+".yaw",0));
		float pitch = (float)(section.getDouble(path+".pitch",0));
		World world = Bukkit.getServer().getWorld(section.getString(path+".world"));
		return new Location(world,x,y,z,yaw,pitch);
	}

	public static float normalAngle(float angle) {
        while (angle <= -180) angle += 360;
        while (angle > 180) angle -= 360;
        return angle;
    }

	public static boolean isPlayerLookingAt(Player player,Location target){
		return isPlayerLookingAt(player,target,0.9);
	}

	public static boolean isPlayerLookingAt(Player player,Location target,double range){
		Location eyeLocation = player.getEyeLocation();
		Vector toEntity = target.toVector().subtract(eyeLocation.toVector());
		return (toEntity.normalize().dot(eyeLocation.getDirection()) > range);
	}

	public static boolean isSimilar(Location location1,Location location2){
		return (location1.getWorld() == location2.getWorld() && location1.getBlockX() == location2.getBlockX() && location1.getBlockY() == location2.getBlockY() && location1.getBlockZ() == location2.getBlockZ());
	}

	public static BlockFace yawToFace (float yaw) {
        return yawToFace(yaw, false);
    }
    public static BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
        yaw = normalAngle(yaw);
        if (useSubCardinalDirections) {
            switch ((int) yaw) {
            case 0 : return BlockFace.NORTH;
            case 45 : return BlockFace.NORTH_EAST;
            case 90 : return BlockFace.EAST;
            case 135 : return BlockFace.SOUTH_EAST;
            case 180 : return BlockFace.SOUTH;
            case 225 : return BlockFace.SOUTH_WEST;
            case 270 : return BlockFace.WEST;
            case 315 : return BlockFace.NORTH_WEST;
            }
            //Let's apply angle differences
            if (yaw >= -22.5 && yaw < 22.5) {
                return BlockFace.NORTH;
            } else if (yaw >= 22.5 && yaw < 67.5) {
                return BlockFace.NORTH_EAST;
            } else if (yaw >= 67.5 && yaw < 112.5) {
                return BlockFace.EAST;
            } else if (yaw >= 112.5 && yaw < 157.5) {
                return BlockFace.SOUTH_EAST;
            } else if (yaw >= -67.5 && yaw < -22.5) {
                return BlockFace.NORTH_WEST;
            } else if (yaw >= -112.5 && yaw < -67.5) {
                return BlockFace.WEST;
            } else if (yaw >= -157.5 && yaw < -112.5) {
                return BlockFace.SOUTH_WEST;
            } else {
                return BlockFace.SOUTH;
            }
        } else {
            switch ((int) yaw) {
            case 0 : return BlockFace.NORTH;
            case 90 : return BlockFace.EAST;
            case 180 : return BlockFace.SOUTH;
            case 270 : return BlockFace.WEST;
            }
            //Let's apply angle differences
            if (yaw >= -45 && yaw < 45) {
                return BlockFace.NORTH;
            } else if (yaw >= 45 && yaw < 135) {
                return BlockFace.EAST;
            } else if (yaw >= -135 && yaw < -45) {
                return BlockFace.WEST;
            } else {
                return BlockFace.SOUTH;
            }
        }
    }

    public static float faceToYaw(BlockFace face){
    	return faceToYaw(face,false);
    }

    public static float faceToYaw(BlockFace face, boolean useSubCardinalDirections){
    	if(useSubCardinalDirections){
	    	switch(face){
		    	case NORTH: return 0f;
		    	case NORTH_EAST: return 45f;
		    	case EAST: return 90f;
		    	case SOUTH_EAST: return 135f;
		    	case SOUTH: return 180f;
		    	case SOUTH_WEST: return 225f;
		    	case WEST: return 270f;
		    	case NORTH_WEST: return 315f;
				default: return 0f;
	    	}
    	} else {
    		switch(face){
		    	case NORTH: return 0f;
		    	case EAST: return 90f;
		    	case SOUTH: return 180f;
		    	case WEST: return 270f;
				default: return 0f;
	    	}
    	}
    }

    public static Location getSafeDestination(final Location loc){
    	final World world = loc.getWorld();
    	int x = loc.getBlockX();
    	int y = (int) Math.round(loc.getY());
    	int z = loc.getBlockZ();
    	final int origX = x;
    	final int origY = y;
    	final int origZ = z;
    	while (isBlockAboveAir(world, x, y, z)) {
    		y -= 1;
    		if (y < 0) {
    			y = origY;
    			break;
    		}
    	}
    	if (isBlockUnsafe(world, x, y, z)) {
    		x = Math.round(loc.getX()) == origX ? x - 1 : x + 1;
    		z = Math.round(loc.getZ()) == origZ ? z - 1 : z + 1;
    	}
    	int i = 0;
    	while (isBlockUnsafe(world, x, y, z)) {
    		i++;
    		if (i >= VOLUME.length) {
    			x = origX;
    			y = origY + RADIUS;
    			z = origZ;
    			break;
    		}
    		x = origX + VOLUME[i].x;
    		y = origY + VOLUME[i].y;
    		z = origZ + VOLUME[i].z;
    	}
    	while (isBlockUnsafe(world, x, y, z)) {
    		y += 1;
    		if (y >= world.getMaxHeight()) {
    			x += 1;
    			break;
    		}
    	}
    	while (isBlockUnsafe(world, x, y, z)) {
    		y -= 1;
    		if (y <= 1) {
    			x += 1;
    			y = world.getHighestBlockYAt(x, z);
    		}
    	}
    	return new Location(world, x + 0.5, y, z + 0.5, loc.getYaw(), loc.getPitch());
    }
    public static boolean isBlockUnsafe(final World world, final int x, final int y, final int z) {
    	if (isBlockDamaging(world, x, y, z)) {
    		return true;
    	}
    	return isBlockAboveAir(world, x, y, z);
    }
    static boolean isBlockAboveAir(final World world, final int x, final int y, final int z) {
    	if (y > world.getMaxHeight()) {
    		return true;
    	}
    	return HOLLOW_MATERIALS.contains(world.getBlockAt(x, y - 1, z).getType());
    }
    public static boolean isBlockDamaging(final World world, final int x, final int y, final int z) {
    	final Block below = world.getBlockAt(x, y - 1, z);
    	if (below.getType() == Material.LAVA || below.getType() == Material.STATIONARY_LAVA) {
    		return true;
    	}
    	if (below.getType() == Material.FIRE) {
    		return true;
    	}
    	if (below.getType() == Material.BED_BLOCK) {
    		return true;
    	}
    	if (world.getBlockAt(x, y, z).getType() == Material.PORTAL) {
    		return true;
    	}
    	return (!HOLLOW_MATERIALS.contains(world.getBlockAt(x, y, z).getType())) || (!HOLLOW_MATERIALS.contains(world.getBlockAt(x, y + 1, z).getType()));
    }

    public static Block getBedNeighbor(Block head){
		if (head.getRelative(BlockFace.EAST).getType() == Material.BED_BLOCK){
			return head.getRelative(BlockFace.EAST);
		} else if (head.getRelative(BlockFace.WEST).getType() == Material.BED_BLOCK){
			return head.getRelative(BlockFace.WEST);
		} else if (head.getRelative(BlockFace.SOUTH).getType() == Material.BED_BLOCK){
			return head.getRelative(BlockFace.SOUTH);
		} else {
			return head.getRelative(BlockFace.NORTH);
		}
	}
}