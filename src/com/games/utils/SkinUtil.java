package com.games.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.realcraft.database.DB;

public class SkinUtil {

	public static String getPlayerSkin(Player player){
		return SkinUtil.getPlayerSkin(player.getName());
	}

	public static String getPlayerSkin(String name){
		String skin = null;
		ResultSet rs = DB.query("SELECT user_skin FROM authme WHERE user_name = '"+name.toLowerCase()+"'");
		try {
			if(rs.next()){
				skin = rs.getString("user_skin");
			}
			rs.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
		return skin;
	}

	public static Skin getSkin(String name){
		Skin skin = null;
		ResultSet rs = DB.query("SELECT skin_value FROM skins_cache WHERE skin_name = '"+name.toLowerCase()+"'");
		try {
			if(rs.next()){
				String value = rs.getString("skin_value");
				skin = new Skin(name,"",value,"");
			}
			rs.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
		return skin;
	}

	public static class Skin {

		private String name;
		private String uuid;
		private String value;
		private String signature;

		public Skin(String name,String uuid,String value,String signature){
			this.name = name;
			this.uuid = uuid;
			this.value = value;
			this.signature = signature;
		}

		public String getName(){
			return name;
		}

		public String getUuid(){
			return uuid;
		}

		public String getValue(){
			return value;
		}

		public String getSignature(){
			return signature;
		}
	}
}