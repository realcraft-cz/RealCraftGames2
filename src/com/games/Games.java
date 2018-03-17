package com.games;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.bedwars.BedWars;
import com.blockparty.BlockParty;
import com.dominate.Dominate;
import com.earth2me.essentials.Essentials;
import com.games.utils.Glow;
import com.hidenseek.HidenSeek;
import com.paintball.Paintball;
import com.ragemode.RageMode;

import realcraft.bukkit.RealCraft;
import realcraft.bukkit.ServerType;
import realcraft.bukkit.database.DB;

public class Games extends JavaPlugin {

	private static Games instance;
	private static FileConfiguration config;
	private static String[] commands;
	private static Essentials essentials;

	private BedWars bedwars;
	private HidenSeek hidenseek;
	private BlockParty blockparty;
	private RageMode ragemode;
	private Paintball paintball;
	private Dominate dominate;

	public static Games getInstance(){
		return instance;
	}

	public static Essentials getEssentials(){
		if(essentials == null) essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
		return essentials;
	}

	public void onEnable(){
		instance = this;
		Glow.registerGlow();
		Bukkit.getScheduler().runTask(this,new Runnable(){
			@Override
			public void run(){
				Games.this.init();
			}
		});
	}

	public void onDisable(){
		if(bedwars != null) bedwars.onDisable();
		if(hidenseek != null) hidenseek.onDisable();
		if(blockparty != null) blockparty.onDisable();
		if(ragemode != null) ragemode.onDisable();
		if(paintball != null) paintball.onDisable();
		if(dominate != null) dominate.onDisable();
	}

	public void init(){
		DB.init();

		List<String> tmpcmds = this.getConfig().getStringList("commands");
		if(!tmpcmds.isEmpty()) commands = tmpcmds.toArray(new String[tmpcmds.size()]);

		if(RealCraft.getServerType() == ServerType.BEDWARS) bedwars = new BedWars();
		if(RealCraft.getServerType() == ServerType.HIDENSEEK) hidenseek = new HidenSeek();
		if(RealCraft.getServerType() == ServerType.BLOCKPARTY) blockparty = new BlockParty();
		if(RealCraft.getServerType() == ServerType.RAGEMODE) ragemode = new RageMode();
		if(RealCraft.getServerType() == ServerType.PAINTBALL) paintball = new Paintball();
		if(RealCraft.getServerType() == ServerType.DOMINATE) dominate = new Dominate();
	}

	public static String[] getCommands(){
		return commands;
	}

	public FileConfiguration getConfig(){
		if(config == null){
			File file = new File(Games.getInstance().getDataFolder()+"/config.yml");
			if(file.exists()){
				config = new YamlConfiguration();
				try {
					config.load(file);
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		return config;
	}

	public static void DEBUG(String message){
		System.out.println("[RealCraftGames] "+message);
	}
}