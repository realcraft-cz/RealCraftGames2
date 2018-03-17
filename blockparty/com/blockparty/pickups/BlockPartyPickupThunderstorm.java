package com.blockparty.pickups;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;

import com.blockparty.BlockParty;
import com.games.Games;
import com.games.player.GamePlayer;
import com.games.utils.Particles;
import com.games.utils.RandomUtil;

public class BlockPartyPickupThunderstorm extends BlockPartyPickup {

	public BlockPartyPickupThunderstorm(BlockParty game){
		super(BlockPartyPickupType.THUNDERSTORM,game);
	}

	@Override
	public void activate(GamePlayer gPlayer){
		this.getGame().getArena().getWorld().setStorm(true);
		Bukkit.getScheduler().runTaskLater(Games.getInstance(),this,30);
	}

	@Override
	public void clear(){
		this.getGame().getArena().getWorld().setStorm(false);
	}

	@Override
	public void run(){
		if(this.getGame().getArena().getWorld().hasStorm()){
			int index = 0;
			boolean add = false;
			Location location = this.getRandomLocation();
			if(location != null){
				location.getWorld().strikeLightning(location);
				Particles.LAVA.display(1f,1f,1f,0f,4,location,64);
				location.getWorld().playSound(location,Sound.BLOCK_FIRE_EXTINGUISH,1f,1f);
				List<Block> blocks = this.getNearbyBlocks(location,1,false,false);
				for(final Block block : blocks){
					Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
						@Override
						public void run(){
							block.setType(Material.AIR);
						}
					},index);
					if(add) index ++;
					add = !add;
				}
			}
			Bukkit.getScheduler().runTaskLater(Games.getInstance(),this,RandomUtil.getRandomInteger(5,15));
		}
	}

	private Location getRandomLocation(){
		int randX = RandomUtil.getRandomInteger(this.getGame().getArena().getLocMin().getBlockX(),this.getGame().getArena().getLocMax().getBlockX());
		int randZ = RandomUtil.getRandomInteger(this.getGame().getArena().getLocMin().getBlockZ(),this.getGame().getArena().getLocMax().getBlockZ());
		int randY = 0;
		for(int y=this.getGame().getArena().getLocMax().getBlockY();y>=this.getGame().getArena().getLocMin().getBlockY();y--){
			if(this.getGame().getArena().getWorld().getBlockAt(randX,y,randZ).isEmpty() && !this.getGame().getArena().getWorld().getBlockAt(randX,y-1,randZ).isEmpty()){
				randY = y;
				break;
			}
		}
		if(randY == 0) return null;
		return new Location(this.getGame().getArena().getWorld(),randX,randY,randZ);
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

                    if(air || location.clone().add(x, y, z).getBlock().getType() != Material.AIR && this.getGame().getArena().isBlockInArena(location.clone().add(x, y, z))) blocks.add(location.clone().add(x, y, z).getBlock());
                    if(air || location.clone().add(-x, y, z).getBlock().getType() != Material.AIR && this.getGame().getArena().isBlockInArena(location.clone().add(-x, y, z))) blocks.add(location.clone().add(-x, y, z).getBlock());
                    if(air || location.clone().add(x, -y, z).getBlock().getType() != Material.AIR && this.getGame().getArena().isBlockInArena(location.clone().add(x, -y, z))) blocks.add(location.clone().add(x, -y, z).getBlock());
                    if(air || location.clone().add(x, y, -z).getBlock().getType() != Material.AIR && this.getGame().getArena().isBlockInArena(location.clone().add(x, y, -z))) blocks.add(location.clone().add(x, y, -z).getBlock());
                    if(air || location.clone().add(-x, -y, z).getBlock().getType() != Material.AIR && this.getGame().getArena().isBlockInArena(location.clone().add(-x, -y, z))) blocks.add(location.clone().add(-x, -y, z).getBlock());
                    if(air || location.clone().add(x, -y, -z).getBlock().getType() != Material.AIR && this.getGame().getArena().isBlockInArena(location.clone().add(x, -y, -z))) blocks.add(location.clone().add(x, -y, -z).getBlock());
                    if(air || location.clone().add(-x, y, -z).getBlock().getType() != Material.AIR && this.getGame().getArena().isBlockInArena(location.clone().add(-x, y, -z))) blocks.add(location.clone().add(-x, y, -z).getBlock());
                    if(air || location.clone().add(-x, -y, -z).getBlock().getType() != Material.AIR && this.getGame().getArena().isBlockInArena(location.clone().add(-x, -y, -z))) blocks.add(location.clone().add(-x, -y, -z).getBlock());
                }
            }
        }
		return blocks;
	}

	private static double lengthSq(double x,double y,double z){
        return (x * x) + (y * y) + (z * z);
    }
}