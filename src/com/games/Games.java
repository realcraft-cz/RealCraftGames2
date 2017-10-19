package com.games;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.blockparty.BlockParty;
import com.games.utils.Glow;
import com.ragemode.RageMode;
import com.realcraft.RealCraft;
import com.realcraft.ServerType;
import com.realcraft.database.DB;

public class Games extends JavaPlugin {

	private static Games instance;
	private static FileConfiguration config;
	private static String[] commands;

	private BlockParty blockparty;
	private RageMode ragemode;

	public static Games getInstance(){
		return instance;
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
		if(blockparty != null) blockparty.onDisable();
		if(ragemode != null) ragemode.onDisable();
	}

	public void init(){
		DB.init();

		List<String> tmpcmds = this.getConfig().getStringList("commands");
		if(!tmpcmds.isEmpty()) commands = tmpcmds.toArray(new String[tmpcmds.size()]);

		if(RealCraft.getServerType() == ServerType.BLOCKPARTY) blockparty = new BlockParty();
		if(RealCraft.getServerType() == ServerType.RAGEMODE) ragemode = new RageMode();
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