package com.blockparty;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
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

	public BlockPartyBlock getRandomBlock(World world,Vector locMin){
		HashMap<Integer,BaseBlock> types = new HashMap<Integer,BaseBlock>();
		for(int y=0;y<size.getBlockY();y++){
			for(int x=0;x<size.getBlockX();x++){
				for(int z=0;z<size.getBlockZ();z++){
					if(!types.containsKey(floor.getBlock(new Vector(x,y,z)).getData()) && floor.getBlock(new Vector(x,y,z)).getId() == Material.STAINED_CLAY.getId()){
						types.put(floor.getBlock(new Vector(x,y,z)).getData(),floor.getBlock(new Vector(x,y,z)));
					}
				}
			}
		}
		BaseBlock block = (BaseBlock)types.values().toArray()[new Random().nextInt(types.size())];
		return new BlockPartyBlock(Material.getMaterial(block.getType()),(byte)block.getData());
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