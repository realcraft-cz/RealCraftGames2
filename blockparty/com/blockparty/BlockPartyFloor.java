package com.blockparty;

import com.games.utils.RandomUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
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
		for(int x = clipboard.getRegion().getMinimumPoint().x();x <= clipboard.getRegion().getMaximumPoint().x();x++){
			for(int y = clipboard.getRegion().getMinimumPoint().y();y <= clipboard.getRegion().getMaximumPoint().y();y++){
				for(int z = clipboard.getRegion().getMinimumPoint().z();z <= clipboard.getRegion().getMaximumPoint().z();z++){
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
					ClipboardFormat format = ClipboardFormats.findByInputStream(() -> new ByteArrayInputStream(bytes));
					if (format == null) {
						throw new RuntimeException("Unsupported schematic format for map_id " + this.id);
					}
					ClipboardReader reader = format.getReader(new ByteArrayInputStream(bytes));
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
				} catch (RuntimeException e) {
					System.out.println(e.getMessage());
				} catch (Exception e){
					e.printStackTrace();
				}
			}
			rs.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}

	public void reset(){
		for(int x = clipboard.getRegion().getMinimumPoint().x();x <= clipboard.getRegion().getMaximumPoint().x();x++){
			for(int y = clipboard.getRegion().getMinimumPoint().y();y <= clipboard.getRegion().getMaximumPoint().y();y++){
				for(int z = clipboard.getRegion().getMinimumPoint().z();z <= clipboard.getRegion().getMaximumPoint().z();z++){
					BaseBlock block = clipboard.getFullBlock(BlockVector3.at(x,y,z));
					Location location = new Location(arena.getWorld(),x-clipboard.getRegion().getMinimumPoint().x(),y-clipboard.getRegion().getMinimumPoint().y(),z-clipboard.getRegion().getMinimumPoint().z());
					location.add(arena.getGame().getMinLoc().x(),arena.getGame().getMinLoc().y(),arena.getGame().getMinLoc().z());
					location.getBlock().setType(BukkitAdapter.adapt(block.getBlockType()));
				}
			}
		}
	}
}