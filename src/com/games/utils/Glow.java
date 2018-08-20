package com.games.utils;

import com.games.Games;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public class Glow extends Enchantment {

	private static Glow glow;

	public Glow(NamespacedKey key){
		super(key);
	}

	@Override
	public boolean canEnchantItem(ItemStack arg0){
		return true;
	}

	@Override
	public boolean conflictsWith(Enchantment arg0){
		return false;
	}

	@Override
	public EnchantmentTarget getItemTarget(){
		return EnchantmentTarget.ALL;
	}

	@Override
	public int getMaxLevel(){
		return 0;
	}

	@Override
	public String getName(){
		return this.getKey().getKey();
	}

	@Override
	public int getStartLevel(){
		return 0;
	}

	@Override
	public boolean isCursed(){
		return false;
	}

	@Override
	public boolean isTreasure(){
		return false;
	}

	public static void registerGlow(){
		try {
			Field f = Enchantment.class.getDeclaredField("acceptingNew");
			f.setAccessible(true);
			f.set(null, true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		try {
			NamespacedKey key = new NamespacedKey(Games.getInstance(),"glow"+Math.random());
			if(Enchantment.getByKey(key) == null){
				glow = new Glow(key);
				Enchantment.registerEnchantment(glow);
			}
			else glow = (Glow)Enchantment.getByKey(key);
		}
		catch (IllegalArgumentException e){
			e.printStackTrace();
		}
	}

	public static Glow getGlow(){
		if(glow == null) Glow.registerGlow();
		return glow;
	}
}