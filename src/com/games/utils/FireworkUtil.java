package com.games.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkUtil {
	private static Random random = new Random();

	public static void spawnFirework(Location location,boolean sparks){
		FireworkUtil.spawnFirework(location,null,null,sparks,random.nextBoolean());
	}

	public static void spawnFirework(Location location,FireworkEffect.Type type,boolean sparks){
		FireworkUtil.spawnFirework(location,type,null,sparks,random.nextBoolean());
	}

	public static void spawnFirework(Location location,FireworkEffect.Type type,boolean sparks,boolean trail){
		FireworkUtil.spawnFirework(location,type,null,sparks,trail);
	}

	public static void spawnFirework(Location location,FireworkEffect.Type type,Color color,boolean sparks,boolean trail){
        Color c1 = null,c2 = Color.WHITE;
        if(color == null){
	        c1 = Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256));
	        c2 = Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }
        else c1 = color;

        if(type == null){
        	int rand = random.nextInt(3);
        	if(rand == 0) type = FireworkEffect.Type.BALL;
        	else if(rand == 1) type = FireworkEffect.Type.BALL_LARGE;
        	else if(rand == 2) type = FireworkEffect.Type.BURST;
        }

        FireworkEffect effect = FireworkEffect.builder().flicker(sparks)
                .withColor(c1).withFade(c2).with(type)
                .trail(trail).build();

        FireworkUtil.detonate(effect,location);
	}

	public static void detonate(FireworkEffect fe, Location loc) {
        Firework f = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta fm = f.getFireworkMeta();
        fm.addEffect(fe);
        f.setFireworkMeta(fm);
        try {
            Class<?> entityFireworkClass = getClass("net.minecraft.server.", "EntityFireworks");
            Class<?> craftFireworkClass = getClass("org.bukkit.craftbukkit.", "entity.CraftFirework");
            Object firework = craftFireworkClass.cast(f);
            Method handle = firework.getClass().getMethod("getHandle");
            Object entityFirework = handle.invoke(firework);
            Field expectedLifespan = entityFireworkClass.getDeclaredField("expectedLifespan");
            Field ticksFlown = entityFireworkClass.getDeclaredField("ticksFlown");
            ticksFlown.setAccessible(true);
            ticksFlown.setInt(entityFirework, expectedLifespan.getInt(entityFirework) - 1);
            ticksFlown.setAccessible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Class<?> getClass(String prefix, String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = prefix + version + nmsClassString;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }
}