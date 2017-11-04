package com.games.utils;

import java.lang.reflect.Field;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

import com.google.common.collect.Sets;

import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.PathfinderGoalSelector;

public class EntityUtil {

	public static void clearPathfinders(Entity entity){
		net.minecraft.server.v1_12_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		try {
			Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
			bField.setAccessible(true);
			Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
			cField.setAccessible(true);
			((EntityInsentient) ((CraftEntity)entity).getHandle()).getNavigation().a(0);
			bField.set(((EntityInsentient) nmsEntity).goalSelector,Sets.newLinkedHashSet());
			bField.set(((EntityInsentient) nmsEntity).targetSelector,Sets.newLinkedHashSet());
			cField.set(((EntityInsentient) nmsEntity).goalSelector,Sets.newLinkedHashSet());
			cField.set(((EntityInsentient) nmsEntity).targetSelector,Sets.newLinkedHashSet());
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}