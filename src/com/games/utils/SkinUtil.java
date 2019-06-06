package com.games.utils;

import org.bukkit.entity.Player;
import realcraft.bukkit.database.DB;

import java.sql.ResultSet;
import java.sql.SQLException;

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

	public static class DefaultSkin extends Skin {

		public DefaultSkin(){
			super("FreeWall","014fd96865c5479f88c4656db279469a","eyJ0aW1lc3RhbXAiOjE1MzgzOTQ2Njk3OTQsInByb2ZpbGVJZCI6IjAxNGZkOTY4NjVjNTQ3OWY4OGM0NjU2ZGIyNzk0NjlhIiwicHJvZmlsZU5hbWUiOiJGcmVlV2FsbCIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGM3YjA0NjgwNDRiZmVjYWNjNDNkMDBhM2E2OTMzNWE4MzRiNzM5Mzc2ODgyOTJjMjBkMzk4OGNhZTU4MjQ4ZCJ9fX0=",null);
		}
	}
}