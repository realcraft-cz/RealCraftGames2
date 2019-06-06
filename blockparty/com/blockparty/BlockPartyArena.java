package com.blockparty;

import com.games.Games;
import com.games.arena.GameArena;
import com.games.arena.data.GameArenaData;
import com.games.game.GameType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import realcraft.bukkit.database.DB;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class BlockPartyArena extends GameArena {

	private World world;
	private ArrayList<BlockPartyFloor> floors = new ArrayList<>();

	private BlockPartyFloor currentFloor;

	public BlockPartyArena(BlockParty game,int id){
		super(game,id);
	}

	public BlockParty getGame(){
		return (BlockParty) super.getGame();
	}

	public World getWorld(){
		if(world == null) world = Bukkit.getWorld("world_blockparty");
		return world;
	}

	public Location getSpectator(){
		return this.getGame().getSpectatorLocation();
	}

	public ArrayList<BlockPartyFloor> getFloors(){
		return floors;
	}

	public BlockPartyFloor getCurrentFloor(){
		return currentFloor;
	}

	public void setCurrentFloor(BlockPartyFloor floor){
		this.currentFloor = floor;
	}

	@Override
	public void resetRegion(){
		currentFloor.reset();
		this.getGame().setLoaded(true);
		Games.DEBUG("loaded true");
	}

	@Override
	public void loadData(GameArenaData data){
	}

	@Override
	public void load(){
		ResultSet rs = DB.query("SELECT map_id FROM "+BlockParty.MAPS+" WHERE map_type = '"+GameType.BLOCKPARTY.getId()+"' AND map_state = '1'");
		try {
			while(rs.next()){
				int id = rs.getInt("map_id");
				floors.add(new BlockPartyFloor(this,id));
			}
			rs.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
		currentFloor = floors.get(0);
		File file = new File(Games.getInstance().getDataFolder()+"/"+GameType.BLOCKPARTY.getName()+"/"+"image.png");
		if(file.exists()){
			try {
				byte[] bytes = Files.readAllBytes(file.toPath());
				this.getImage().load(bytes);
			} catch (IOException e){
				e.printStackTrace();
			}
		}
	}
}