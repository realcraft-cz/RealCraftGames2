package com.games.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class ItemUtil {

	private static final String ITEMS_NAMES = "http://minecraft-ids.grahamedgecombe.com/items.json";
	private static HashMap<String,String> names = new HashMap<String,String>();

	public static void init(){
		try {
			HttpURLConnection request = (HttpURLConnection) new URL(ITEMS_NAMES).openConnection();
			request.setConnectTimeout(5000);
			request.setReadTimeout(5000);
			request.setDoOutput(true);
			String line;
			StringBuilder output = new StringBuilder();
			BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
			while((line = in.readLine()) != null) output.append(line);
			in.close();
			JsonElement element = new JsonParser().parse(output.toString());
			if(element.isJsonArray()){
				JsonArray array = element.getAsJsonArray();
				for(int i=0;i<array.size();i++){
					JsonObject data = array.get(i).getAsJsonObject();
					String index = data.get("type").getAsString()+";"+data.get("meta").getAsString();
					names.put(index,data.get("name").getAsString());
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public static ItemStack getHead(String url){
		ItemStack head = new ItemStack(Material.SKULL_ITEM,1,(short)3);
		SkullMeta headMeta = (SkullMeta) head.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(),null);
		profile.getProperties().put("textures",new Property("textures",url));
		Field profileField = null;
		try {
			profileField = headMeta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(headMeta,profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
		head.setItemMeta(headMeta);
		return head;
	}

	public static ItemStack getHead(String name,String url){
		ItemStack head = new ItemStack(Material.SKULL_ITEM,1,(short)3);
		SkullMeta headMeta = (SkullMeta) head.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(),null);
		profile.getProperties().put("textures",new Property("textures",url));
		Field profileField = null;
		try {
			profileField = headMeta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(headMeta,profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
		headMeta.setDisplayName(name);
		head.setItemMeta(headMeta);
		return head;
	}

	public static void removeItems(Inventory inventory,ItemStack item,int amount){
		removeItems(inventory,item,amount,false);
	}

	public static void removeItems(Inventory inventory,ItemStack item,int amount,boolean reverse){
        if(amount <= 0) return;
        int size = inventory.getSize();
        for(int slot=size-1;slot>=0;slot--){
            ItemStack is = inventory.getItem(slot);
            if(is == null) continue;
            if(item.isSimilar(is)){
                int newAmount = is.getAmount() - amount;
                if(newAmount > 0){
                    is.setAmount(newAmount);
                    break;
                } else {
                    inventory.clear(slot);
                    amount = -newAmount;
                    if(amount == 0) break;
                }
            }
        }
    }

	@SuppressWarnings("deprecation")
	public static String getItemName(ItemStack item){
		return names.get(item.getType().getId()+";"+item.getData().getData());
	}

	public static void setLetherColor(ItemStack item,String color){
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(Integer.valueOf(color.substring(1,3),16),Integer.valueOf(color.substring(3,5),16),Integer.valueOf(color.substring(5,7),16)));
		item.setItemMeta(meta);
	}
}