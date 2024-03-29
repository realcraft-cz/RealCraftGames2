package com.blockparty;

import com.games.utils.RandomUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import realcraft.bukkit.database.DB;
import realcraft.bukkit.utils.MaterialUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static realcraft.bukkit.mapmanager.MapManager.MAPS;

public class BlockPartyFloor {

	private int id;
	private BlockPartyArena arena;

	private boolean used = false;
	private Clipboard clipboard;

	public BlockPartyFloor(BlockPartyArena arena,int id){
		this.arena = arena;
		this.id = id;
		this.loadRegion();
	}

	public boolean isUsed(){
		return used;
	}

	public void setUsed(boolean used){
		this.used = used;
	}

	public Material getRandomBlock(){
		ArrayList<Material> types = new ArrayList<>();
		for(int x = clipboard.getRegion().getMinimumPoint().getBlockX();x <= clipboard.getRegion().getMaximumPoint().getBlockX();x++){
			for(int y = clipboard.getRegion().getMinimumPoint().getBlockY();y <= clipboard.getRegion().getMaximumPoint().getBlockY();y++){
				for(int z = clipboard.getRegion().getMinimumPoint().getBlockZ();z <= clipboard.getRegion().getMaximumPoint().getBlockZ();z++){
					Material type = BukkitAdapter.adapt(clipboard.getFullBlock(BlockVector3.at(x,y,z)).getBlockType());
					if(!types.contains(type) && MaterialUtil.isTerracotta(type)){
						types.add(type);
					}
				}
			}
		}
		return types.get(RandomUtil.getRandomInteger(0,types.size()-1));
	}

	private void loadRegion(){
		ResultSet rs = DB.query("SELECT * FROM "+MAPS+" WHERE map_id = '"+this.id+"'");
		try {
			if(rs.next()){
				Blob blob = rs.getBlob("map_region");
				byte[] bytes = blob.getBytes(1,(int)blob.length());
				blob.free();
				try {
					BuiltInClipboardFormat format = BuiltInClipboardFormat.SPONGE_SCHEMATIC;
					ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
					ClipboardReader reader = format.getReader(bais);
					Clipboard clipboard = reader.read();

					AffineTransform transform = new AffineTransform();
					transform = transform.rotateY(-90);
					transform = transform.rotateX(0);
					transform = transform.rotateZ(0);

					BlockTransformExtent extent = new BlockTransformExtent(clipboard, transform);
					Clipboard target = new BlockArrayClipboard(clipboard.getRegion());
					target.setOrigin(clipboard.getOrigin());
					ForwardExtentCopy copy = new ForwardExtentCopy(extent, clipboard.getRegion(), clipboard.getOrigin().add(15, 0, 15), target, clipboard.getOrigin().add(15, 0, 15));
					copy.setTransform(transform);
					Operations.completeBlindly(copy);

					this.clipboard = target;
				} catch (IOException e){
					e.printStackTrace();
				}
			}
			rs.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}

	public void reset(){
		for(int x = clipboard.getRegion().getMinimumPoint().getBlockX();x <= clipboard.getRegion().getMaximumPoint().getBlockX();x++){
			for(int y = clipboard.getRegion().getMinimumPoint().getBlockY();y <= clipboard.getRegion().getMaximumPoint().getBlockY();y++){
				for(int z = clipboard.getRegion().getMinimumPoint().getBlockZ();z <= clipboard.getRegion().getMaximumPoint().getBlockZ();z++){
					BaseBlock block = clipboard.getFullBlock(BlockVector3.at(x,y,z));
					Location location = new Location(arena.getWorld(),x-clipboard.getRegion().getMinimumPoint().getBlockX(),y-clipboard.getRegion().getMinimumPoint().getBlockY(),z-clipboard.getRegion().getMinimumPoint().getBlockZ());
					location.add(arena.getGame().getMinLoc().getBlockX(),arena.getGame().getMinLoc().getBlockY(),arena.getGame().getMinLoc().getBlockZ());
					location.getBlock().setType(BukkitAdapter.adapt(block.getBlockType()));
				}
			}
		}
	}
}