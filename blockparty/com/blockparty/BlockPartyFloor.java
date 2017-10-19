package com.blockparty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

@SuppressWarnings("deprecation")
public class BlockPartyFloor {

	private Vector size;
	public CuboidClipboard floor = null;

	private boolean used = false;

	public BlockPartyFloor(File file){
		try {
			floor = MCEditSchematicFormat.getFormat(file).load(file);
			size = floor.getSize();
		} catch (com.sk89q.worldedit.data.DataException | IOException e){
			e.printStackTrace();
		}
	}

	public boolean isUsed(){
		return used;
	}

	public void setUsed(boolean used){
		this.used = used;
	}

	public Block getRandomBlock(World world,Vector locMin){
		ArrayList<Integer> types = new ArrayList<Integer>();
		for(int y=0;y<size.getBlockY();y++){
			for(int x=0;x<size.getBlockX();x++){
				for(int z=0;z<size.getBlockZ();z++){
					if(!types.contains(floor.getBlock(new Vector(x,y,z)).getData()) && floor.getBlock(new Vector(x,y,z)).getId() == Material.STAINED_CLAY.getId()) types.add(floor.getBlock(new Vector(x,y,z)).getData());
				}
			}
		}
		Block block = null;
		int randomdata = types.get(new Random().nextInt(types.size()));
		for(int y=0;y<size.getBlockY();y++){
			for(int x=0;x<size.getBlockX();x++){
				for(int z=0;z<size.getBlockZ();z++){
					if(world.getBlockAt(locMin.getBlockX() + x,locMin.getBlockY() + y,locMin.getBlockZ() + z).getType() == Material.STAINED_CLAY && world.getBlockAt(locMin.getBlockX() + x,locMin.getBlockY() + y,locMin.getBlockZ() + z).getData() == randomdata){
						block = world.getBlockAt(locMin.getBlockX() + x,locMin.getBlockY() + y,locMin.getBlockZ() + z);
					}
				}
			}
		}
		return block;
	}

	public void paste(World world,Vector locMin){
		for(int y=0;y<size.getBlockY();y++){
			for(int x=0;x<size.getBlockX();x++){
				for(int z=0;z<size.getBlockZ();z++){
					Block block = world.getBlockAt(locMin.getBlockX() + x,locMin.getBlockY() + y,locMin.getBlockZ() + z);
					block.setType(Material.getMaterial(floor.getBlock(new Vector(x,y,z)).getId()),false);
					block.setData((byte)floor.getBlock(new Vector(x,y,z)).getData(),false);
				}
			}
		}
	}
}