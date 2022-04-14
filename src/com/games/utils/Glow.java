package com.games.utils;

import com.games.Games;
import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Set;

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
	public @NotNull Component displayName(int i) {
		return null;
	}

	@Override
	public boolean isTradeable() {
		return false;
	}

	@Override
	public boolean isDiscoverable() {
		return false;
	}

	@Override
	public @NotNull EnchantmentRarity getRarity() {
		return null;
	}

	@Override
	public float getDamageIncrease(int i, @NotNull EntityCategory entityCategory) {
		return 0;
	}

	@Override
	public @NotNull Set<EquipmentSlot> getActiveSlots() {
		return null;
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

	@Override
	public @NotNull String translationKey() {
		return null;
	}
}