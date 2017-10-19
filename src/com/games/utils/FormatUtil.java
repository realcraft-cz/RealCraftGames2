package com.games.utils;

import org.bukkit.ChatColor;

public class FormatUtil {

	public static String format(String string,String[] object){
		string = string.replace("{"+(object[0].toString())+"}",object[1].toString());
		return ChatColor.translateAlternateColorCodes('&',string);
	}

	public static String format(String string,Object ... objects){
		int index = 0;
		for(Object object : objects){
			if(object instanceof String[]) string = string.replace("{"+(((String[])object)[0].toString())+"}",((String[])object)[1].toString());
			else string = string.replace("{"+(index++)+"}",object.toString());
		}
		return ChatColor.translateAlternateColorCodes('&',string);
	}

	public static String timeFormat(int time){
		int min = (int) Math.floor(time / 60);
		int sec = time % 60;
		String minStr = (min < 10) ? "0" + String.valueOf(min) : String.valueOf(min);
		String secStr = (sec < 10) ? "0" + String.valueOf(sec) : String.valueOf(sec);
		return minStr + ":" + secStr;
	}
}