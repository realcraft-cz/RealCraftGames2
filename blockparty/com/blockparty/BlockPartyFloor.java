package com.blockparty;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import realcraft.bukkit.utils.MaterialUtil;
import realcraft.bukkit.utils.RandomUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class BlockPartyFloor {

	public Clipboard floor = null;

	private boolean used = false;

	public BlockPartyFloor(File file){
		try {
			BuiltInClipboardFormat format = BuiltInClipboardFormat.MCEDIT_SCHEMATIC;
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ClipboardReader reader = format.getReader(bis);
			this.floor = reader.read();
			fis.close();
			bis.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public boolean isUsed(){
		return used;
	}

	public void setUsed(boolean used){
		this.used = used;
	}

	public BlockPartyBlock getRandomBlock(){
		ArrayList<Material> types = new ArrayList<>();
		for(int x = floor.getRegion().getMinimumPoint().getBlockX();x <= floor.getRegion().getMaximumPoint().getBlockX();x++){
			for(int y = floor.getRegion().getMinimumPoint().getBlockY();y <= floor.getRegion().getMaximumPoint().getBlockY();y++){
				for(int z = floor.getRegion().getMinimumPoint().getBlockZ();z <= floor.getRegion().getMaximumPoint().getBlockZ();z++){
					BaseBlock block = floor.getFullBlock(new BlockVector(x,y,z));
					Material type = BukkitAdapter.adapt(block.getBlockType());
					if(!types.contains(type) && MaterialUtil.isTerracotta(type)){
						types.add(type);
					}
				}
			}
		}
		return new BlockPartyBlock(types.get(RandomUtil.getRandomInteger(0,types.size()-1)));
	}

	public void paste(World world,Vector locMin){
		for(int x = floor.getRegion().getMinimumPoint().getBlockX();x <= floor.getRegion().getMaximumPoint().getBlockX();x++){
			for(int y = floor.getRegion().getMinimumPoint().getBlockY();y <= floor.getRegion().getMaximumPoint().getBlockY();y++){
				for(int z = floor.getRegion().getMinimumPoint().getBlockZ();z <= floor.getRegion().getMaximumPoint().getBlockZ();z++){
					BaseBlock block = floor.getFullBlock(new BlockVector(x,y,z));
					Location location = new Location(world,x-floor.getRegion().getMinimumPoint().getBlockX(),y-floor.getRegion().getMinimumPoint().getBlockY(),z-floor.getRegion().getMinimumPoint().getBlockZ());
					location.add(locMin.getBlockX(),locMin.getBlockY(),locMin.getBlockZ());
					location.getBlock().setType(BukkitAdapter.adapt(block.getBlockType()));
				}
			}
		}
	}
}