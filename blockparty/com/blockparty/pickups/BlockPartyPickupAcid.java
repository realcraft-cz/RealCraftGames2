package com.blockparty.pickups;

import com.blockparty.BlockParty;
import com.blockparty.BlockPartyState;
import com.games.Games;
import com.games.player.GamePlayer;
import com.games.utils.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import realcraft.bukkit.utils.Particles;

import java.util.ArrayList;
import java.util.List;

public class BlockPartyPickupAcid extends BlockPartyPickup {

	private int radius = 0;

	public BlockPartyPickupAcid(BlockParty game){
		super(BlockPartyPickupType.ACID,game);
	}

	@Override
	public void activate(GamePlayer gPlayer){
		radius = 0;
		Particles.EXPLOSION_LARGE.display(0,0,0,0,1,this.getLocation(),128);
		this.run();
	}

	@Override
	public void clear(){
	}

	@Override
	public void run(){
		if(this.getGame().getRoundState() != BlockPartyState.FALLING){
			List<Block> blocks = getNearbyBlocks(this.getLocation(),radius++,false,false);
			int index = 0;
			int add = 0;
			for(final Block block : blocks){
				Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
					@Override
					public void run(){
						block.setType(Material.AIR);
						Particles.CLOUD.display(0.1f,0f,0.1f,0.1f,2,block.getLocation().clone().add(0,1,0),64);
						if(RandomUtil.getRandomBoolean()){
							Particles.LAVA.display(0f,0f,0f,0f,1,block.getLocation().clone().add(0,1,0),64);
							block.getWorld().playSound(block.getLocation(),Sound.BLOCK_FIRE_EXTINGUISH,1f,(float)RandomUtil.getRandomDouble(1.0,2.0));
						}
					}
				},index);
				if(add++ == 2){
					index ++;
					add = 0;
				}
			}
			if(index != 0 && radius < 7) Bukkit.getScheduler().runTaskLater(Games.getInstance(),this,index+1);
		}
	}

	private List<Block> getNearbyBlocks(Location location,double radius,boolean hollow,boolean air){
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

                    if(air || (location.clone().add(x, y, z).getBlock().getType() != Material.AIR && this.getGame().getArena().getRegion().isLocationInside(location.clone().add(x, y, z)))) blocks.add(location.clone().add(x, y, z).getBlock());
                    if(air || (location.clone().add(-x, y, z).getBlock().getType() != Material.AIR && this.getGame().getArena().getRegion().isLocationInside(location.clone().add(-x, y, z)))) blocks.add(location.clone().add(-x, y, z).getBlock());
                    if(air || (location.clone().add(x, -y, z).getBlock().getType() != Material.AIR && this.getGame().getArena().getRegion().isLocationInside(location.clone().add(x, -y, z)))) blocks.add(location.clone().add(x, -y, z).getBlock());
                    if(air || (location.clone().add(x, y, -z).getBlock().getType() != Material.AIR && this.getGame().getArena().getRegion().isLocationInside(location.clone().add(x, y, -z)))) blocks.add(location.clone().add(x, y, -z).getBlock());
                    if(air || (location.clone().add(-x, -y, z).getBlock().getType() != Material.AIR && this.getGame().getArena().getRegion().isLocationInside(location.clone().add(-x, -y, z)))) blocks.add(location.clone().add(-x, -y, z).getBlock());
                    if(air || (location.clone().add(x, -y, -z).getBlock().getType() != Material.AIR && this.getGame().getArena().getRegion().isLocationInside(location.clone().add(x, -y, -z)))) blocks.add(location.clone().add(x, -y, -z).getBlock());
                    if(air || (location.clone().add(-x, y, -z).getBlock().getType() != Material.AIR && this.getGame().getArena().getRegion().isLocationInside(location.clone().add(-x, y, -z)))) blocks.add(location.clone().add(-x, y, -z).getBlock());
                    if(air || (location.clone().add(-x, -y, -z).getBlock().getType() != Material.AIR && this.getGame().getArena().getRegion().isLocationInside(location.clone().add(-x, -y, -z)))) blocks.add(location.clone().add(-x, -y, -z).getBlock());
                }
            }
        }
		return blocks;
	}

	private static double lengthSq(double x,double y,double z){
        return (x * x) + (y * y) + (z * z);
    }
}