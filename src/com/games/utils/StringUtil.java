package com.games.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
	//private static final Pattern URL_PATTERN = Pattern.compile("((?:(?:https?)://)?[\\w-_\\.]{2,}\\.([a-zA-Z]{2,3}(?:/\\S+)?))");
	public static final Pattern URL_PATTERN = Pattern.compile("((?:(?:https?)://)?[\\w-_\\.]{2,}\\.((cz|sk|eu|com|net)(?:/\\S+)?))");
	public static final Pattern IPPATTERN = Pattern.compile("(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5]))");

	public static String blockURL(String input,Pattern[] allowedURLs,Pattern[] blockedWords,ArrayList<String> adverts){
		if(input == null) return null;

		Matcher matcher = URL_PATTERN.matcher(input);
		while(matcher.find()){
			String word = matcher.group();
			boolean allowed = false;
			for(Pattern url : allowedURLs){
			    Matcher m = url.matcher(word);
				if(m.find()){
					allowed = true;
					break;
				}
			}
			if(!allowed){
				input = input.replaceAll(word,"§cBLOKOVÁNO§r");
				adverts.add(word);
			}
			else input = input.replaceAll(word,"§n"+word+"§r");
		}

		for(Pattern word : blockedWords){
		    Matcher m = word.matcher(input);
			if(m.find()){
				input = input.replaceAll(m.group(),"§cBLOKOVÁNO§r");
				adverts.add(m.group());
			}
		}

		matcher = IPPATTERN.matcher(input);
		while(matcher.find()){
			String word = matcher.group();
			input = input.replaceAll(word,"§cBLOKOVÁNO§r");
			adverts.add(word);
		}
		return input;
	}

	public static String inflect(int value,String[] inflections){
		if(value == 1) return inflections[0];
		else if(value >= 2 && value <= 4) return inflections[1];
		return inflections[2];
	}

	public static String getRandomChars(int length){
		String chars = "";
		String alphabet = "abcdefghjkmnopqrstuvwxABCDEFGHJKLMNOPQRSTUVWX0123456789";
		for(int i=0;i<length;i++) chars += alphabet.charAt(RandomUtil.getRandomInteger(0,alphabet.length()-1));
		return chars;
	}
}