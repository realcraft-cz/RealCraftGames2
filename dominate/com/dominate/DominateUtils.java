package com.dominate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DominateUtils {

	public static final int RADAR_RANGE = 42;
	private static Random random = new Random();

	public static boolean getRandomBoolean(){
		return random.nextBoolean();
	}

	public static int getRandomInteger(int min,int max){
		return random.nextInt((max - min) + 1) + min;
	}

	public static double getRandomDouble(double min,double max){
		return min+Math.random()*(max-min);
	}

	public static void removeItems(Inventory inventory,Material type,int amount){
        if (amount <= 0) return;
        int size = inventory.getSize();
        for (int slot = 0; slot < size; slot++) {
            ItemStack is = inventory.getItem(slot);
            if (is == null) continue;
            if (type == is.getType()) {
                int newAmount = is.getAmount() - amount;
                if (newAmount > 0) {
                    is.setAmount(newAmount);
                    break;
                } else {
                    inventory.clear(slot);
                    amount = -newAmount;
                    if (amount == 0) break;
                }
            }
        }
    }

	public static float absAngle(float angle){
		if(angle < 0) angle += 360;
		return angle;
	}

	public static int distance(int alpha, int beta){
		int phi = Math.abs(beta - alpha) % 360;
		int distance = phi > 180 ? 360 - phi : phi;
		int sign = (alpha - beta >= 0 && alpha - beta <= 180) || (alpha - beta <=-180 && alpha - beta >= -360) ? 1 : -1;
		return distance*sign;
	}

	public static int yawToLocation(Location source,Location destination){
		float yawSource = DominateUtils.absAngle(source.getYaw());
		double dX = source.getX() - destination.getX();
		double dZ = source.getZ() - destination.getZ();
		double yawDest = Math.atan2(dZ,dX);
		yawDest = ((yawDest > 0 ? yawDest : (2*Math.PI + yawDest)) * 360 / (2*Math.PI))+90;
		if(yawDest > 360) yawDest = yawDest-360;
		return DominateUtils.distance((int)yawDest,(int)yawSource);
	}

	public static int yawToRadarIndex(int yaw){
		yaw = (int)(yaw/(110.0f/RADAR_RANGE));
		if(yaw >= RADAR_RANGE/2) return RADAR_RANGE-1;
		else if(yaw < -RADAR_RANGE/2) return 0;
		return yaw+(RADAR_RANGE/2);
	}

	public static List<Block> getNearbyBlocks(Location location,double radius){
		return DominateUtils.getNearbyBlocks(location,radius,false);
	}

	public static List<Block> getNearbyBlocks(Location location,double radius,boolean hollow){
		List<Block> blocks = new ArrayList<Block>();

		double radiusX = radius+0.5;
        double radiusY = radius+0.5;
        double radiusZ = radius+0.5;

        final double invRadiusX = 1 / radiusX;
        final double invRadiusY = 1 / radiusY;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusY = (int) Math.ceil(radiusY);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);

        double nextXn = 0;
        forX: for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextYn = 0;
            forY: for (int y = 0; y <= ceilRadiusY; ++y) {
                final double yn = nextYn;
                nextYn = (y + 1) * invRadiusY;
                double nextZn = 0;
                forZ: for (int z = 0; z <= ceilRadiusZ; ++z) {
                    final double zn = nextZn;
                    nextZn = (z + 1) * invRadiusZ;

                    double distanceSq = lengthSq(xn, yn, zn);
                    if (distanceSq > 1) {
                        if (z == 0) {
                            if (y == 0) {
                                break forX;
                            }
                            break forY;
                        }
                        break forZ;
                    }

                    if (hollow && lengthSq(nextXn, yn, zn) <= 1 && lengthSq(xn, nextYn, zn) <= 1 && lengthSq(xn, yn, nextZn) <= 1){
                        continue;
                    }

                    blocks.add(location.clone().add(x, y, z).getBlock());
                    blocks.add(location.clone().add(-x, y, z).getBlock());
                    blocks.add(location.clone().add(x, -y, z).getBlock());
                    blocks.add(location.clone().add(x, y, -z).getBlock());
                    blocks.add(location.clone().add(-x, -y, z).getBlock());
                    blocks.add(location.clone().add(x, -y, -z).getBlock());
                    blocks.add(location.clone().add(-x, y, -z).getBlock());
                    blocks.add(location.clone().add(-x, -y, -z).getBlock());
                }
            }
        }
		return blocks;
	}

	public static ArrayList<Location> makeSphere(Location pos, Material material, double radius){
		ArrayList<Location> blocks = new ArrayList<Location>();

        double radiusX = radius+0.5;
        double radiusY = radius+0.5;
        double radiusZ = radius+0.5;

        final double invRadiusX = 1 / radiusX;
        final double invRadiusY = 1 / radiusY;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusY = (int) Math.ceil(radiusY);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);

        double nextXn = 0;
        forX: for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextYn = 0;
            forY: for (int y = 0; y <= ceilRadiusY; ++y) {
                final double yn = nextYn;
                nextYn = (y + 1) * invRadiusY;
                double nextZn = 0;
                forZ: for (int z = 0; z <= ceilRadiusZ; ++z) {
                    final double zn = nextZn;
                    nextZn = (z + 1) * invRadiusZ;

                    double distanceSq = lengthSq(xn, yn, zn);
                    if (distanceSq > 1) {
                        if (z == 0) {
                            if (y == 0) {
                                break forX;
                            }
                            break forY;
                        }
                        break forZ;
                    }

                    if (lengthSq(nextXn, yn, zn) <= 1 && lengthSq(xn, nextYn, zn) <= 1 && lengthSq(xn, yn, nextZn) <= 1){
                        continue;
                    }

                    if(setBlockFromAir(pos.clone().add(x, y, z),material)) blocks.add(pos.clone().add(x, y, z));
                    if(setBlockFromAir(pos.clone().add(-x, y, z),material)) blocks.add(pos.clone().add(-x, y, z));
                    if(setBlockFromAir(pos.clone().add(x, -y, z),material)) blocks.add(pos.clone().add(x, -y, z));
                    if(setBlockFromAir(pos.clone().add(x, y, -z),material)) blocks.add(pos.clone().add(x, y, -z));
                    if(setBlockFromAir(pos.clone().add(-x, -y, z),material)) blocks.add(pos.clone().add(-x, -y, z));
                    if(setBlockFromAir(pos.clone().add(x, -y, -z),material)) blocks.add(pos.clone().add(x, -y, -z));
                    if(setBlockFromAir(pos.clone().add(-x, y, -z),material)) blocks.add(pos.clone().add(-x, y, -z));
                    if(setBlockFromAir(pos.clone().add(-x, -y, -z),material)) blocks.add(pos.clone().add(-x, -y, -z));
                }
            }
        }
        return blocks;
    }

	public static ArrayList<Location> makeCylinder(Location pos, Material material, double radius){
		ArrayList<Location> blocks = new ArrayList<Location>();

        double radiusX = radius+0.5;
        double radiusZ = radius+0.5;

        int height = 1;
        if (height == 0) {
            return blocks;
        } else if (height < 0) {
            height = -height;
            pos = pos.subtract(0, height, 0);
        }

        if (pos.getBlockY() < 0){
            pos.setY(0);
        }

        final double invRadiusX = 1 / radiusX;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);

        double nextXn = 0;
        forX: for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextZn = 0;
            forZ: for (int z = 0; z <= ceilRadiusZ; ++z) {
                final double zn = nextZn;
                nextZn = (z + 1) * invRadiusZ;

                double distanceSq = lengthSq(xn, zn);
                if (distanceSq > 1) {
                    if (z == 0) {
                        break forX;
                    }
                    break forZ;
                }

                for (int y = 0; y < height; ++y) {
                	if(setBlockOnWater(pos.clone().add(x, y, z),material)) blocks.add(pos.clone().add(x, y, z));
                	if(setBlockOnWater(pos.clone().add(-x, y, z),material)) blocks.add(pos.clone().add(-x, y, z));
                	if(setBlockOnWater(pos.clone().add(x, y, -z),material)) blocks.add(pos.clone().add(x, y, -z));
                	if(setBlockOnWater(pos.clone().add(-x, y, -z),material)) blocks.add(pos.clone().add(-x, y, -z));
                }
            }
        }
        return blocks;
    }

	public static boolean setBlockFromAir(Location location,Material material){
		if(location.getBlock().getType() == Material.AIR){
			location.getBlock().setType(material);
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public static boolean setBlockOnWater(Location location,Material material){
		if(location.getBlock().getType() == Material.STATIONARY_WATER && location.getBlock().getData() == (byte)0){
			if(location.getBlock().getRelative(BlockFace.UP).getType() == Material.AIR){
				location.getBlock().setType(material);
				return true;
			}
		}
		return false;
	}

	private static double lengthSq(double x,double y,double z){
        return (x * x) + (y * y) + (z * z);
    }

	private static double lengthSq(double x,double z){
        return (x * x) + (z * z);
    }
}